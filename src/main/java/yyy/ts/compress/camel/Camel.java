package yyy.ts.compress.camel;

import gr.aueb.delorean.chimp.OutputBitStream;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static yyy.ts.compress.camel.CamelUtils.*;
public class Camel {

    private long storedVal = 0;

    // 默认10000 对应 block大小位1000
    private final static int outStreamSize = 100000;
    private boolean first = true;
    private int size;
    private final static long END_SIGN = Double.doubleToLongBits(Double.NaN);

    private final static int DECIMAL_MAX_COUNT = 4;

    // 按照寻找到的m的值进行保存
    public final static int[] mValueBits = {3, 5, 7, 10, 15};
//    public final static BigDecimal[]  threshold = {BigDecimal.valueOf(0.5), BigDecimal.valueOf(0.25), BigDecimal.valueOf(0.125), BigDecimal.valueOf(0.0625)};
    public final static long[]  threshold = {5, 25, 125, 625};

    private static final long[] powers = {1L, 10L, 100L, 1000L, 10000L, 100000L};
    public static Map<String, byte[]> compressVal = new HashMap<>();

    private final OutputBitStream out;

    private boolean TreeFlag = true;


    private static BPlusTree bPlusTree;

    private static BPlusTree2 bPlusTree2;

    private static BPlusDecimalTree bPlusDecimalTree;



    // We should have access to the series?
    public Camel() {
        out = new OutputBitStream(new byte[outStreamSize]);  // for elf, we need one more bit for each at the worst case
        size = 0;
    }

    public OutputBitStream getOutputStream() {
        return this.out;
    }

    /**
     * Adds a new long value to the series. Note, values must be inserted in order.
     *
     * @param value next floating point value in the series
     */
    public int addValue(long value) {
        if(first) {
            return writeFirst(value);
        } else {
            return compressValue(value);
        }
    }

    /**
     * Adds a new double value to the series. Note, values must be inserted in order.
     *
     * @param value next floating point value in the series
     */
    public int addValue(double value) {
        if(first) {
            Long[] decimal = cal_decimal_count(value);
            int decimal_count= Math.toIntExact(decimal[0]);
            value = (value < 0 ? -1: 1) * ((double) ((int)Math.abs(value)  * powers[decimal_count] + decimal[1]))/powers[decimal_count];
            return writeFirst(Double.doubleToRawLongBits(value));
        } else {
            return compressValue(value);
        }
    }

    // 写入第一个数据
    private int writeFirst(long value) {
        first = false;
        // 保存第一个数字的整数进行差值计算
        storedVal = (int) Double.longBitsToDouble(value);
        out.writeLong(value, 64);
        size += 64;
//        compressVal.put("compressInt", convertToBinary((int) value, 64));
        return size;
    }

    /**
     * Closes the block and writes the remaining stuff to the BitOutput.
     */
    public void close() {
//        addValue(END_SIGN);
        out.writeBit(false);
        out.flush();
    }

    // 数据压缩
    private int compressValue(double value) {
        // 压缩小数位 默认小数位是1.**
        int intSignal = value < 0 ? 0 : 1;
        size = compressIntegerValue((int)value, intSignal);
        Long[] decimal = cal_decimal_count(value);
        size = compressDecimalValue(decimal[1], Math.toIntExact(decimal[0]));

        return size;
    }

    public Long[] cal_decimal_count(double value) {
        double factor = 1;
        int decimal_count = 0;
        value = Math.abs(value);
        double epsilon = 0.0000001; // 设置一个很小的阈值
        while (Math.abs(value * factor - Math.round(value * factor)) > epsilon) {
            factor *= 10.0;
            decimal_count++;
        }
        long decimal_value = 0;
        if (decimal_count == 0) {
            decimal_count = 1;
        }
        if (decimal_count > 0 && decimal_count<= DECIMAL_MAX_COUNT) {
            decimal_value =  Math.round (value * powers[decimal_count]) % powers[decimal_count];
        }else {
            decimal_value = Math.round (value * powers[DECIMAL_MAX_COUNT]) % powers[DECIMAL_MAX_COUNT];
            decimal_count = DECIMAL_MAX_COUNT;
        }
        Long[] res = new Long[2];
        res[0] = (long)decimal_count;
        res[1] = decimal_value;
        return res;
    }


    // 压缩小数部分
    private int compressDecimalValue(long decimal_value, int decimal_count) {
        // 计算小数位数
        if (decimal_count == 0) return  this.size;
        out.writeInt(decimal_count-1, 2); // 保存字节数 00-1 01-2 10-3 11-4
        size += 2;
        // 计算m的值
        long thread = threshold[decimal_count-1];
        int m = (int) decimal_value;
        size += 1;
        if (decimal_value - thread >= 0) {  // 计算m的值
            // 标志位：是否计算m的值
            out.writeBit(true);
            m = (int) (decimal_value % thread);
            // 对于m进行XOR操作
            long xor = (Double.doubleToLongBits((double)decimal_value/powers[decimal_count]+1)) ^ Double.doubleToLongBits(((double) m/powers[decimal_count]+1));
            // 保存小数位数长度的centerBits 保存decimal_count （四位最多就是1000）
            out.writeLong(xor >>> 52 - decimal_count, decimal_count);
            size += decimal_count;// Store the meaningful bits of XOR

        } else {  // m就为原来的值
            out.writeBit(false);
        }

        // 保存m的值
        if (decimal_count == 1) { // 如果是1 直接往后读decimal_count+1位
            out.writeInt(m, 3);
            size += 3;
        } else if (decimal_count ==2) {
            if (m < 8) {
                out.writeInt(0, 1);
                out.writeInt(m, 3);
                size += 4;
            }  else {
                out.writeInt(1, 1);
                out.writeInt(m, 5);
                size += 6;
            }

        } else if (decimal_count == 3) {
            if (m < 2) {
                out.writeInt(0, 2);
                out.writeInt(m, 1);
                size += 3;
            }else if (m < 8){
                out.writeInt(1, 2);
                out.writeInt(m, 3);
                size += 5;
            }else if (m < 32) {
                out.writeInt(2, 2);
                out.writeInt(m, 5);
                size += 7;
            }else {
                out.writeInt(3, 2);
                out.writeInt(m, mValueBits[decimal_count-1]);
                size += 2;
                size += mValueBits[decimal_count-1];
            }

        } else {
            if (m < 16) {
                out.writeInt(0, 2);
                out.writeInt(m, 4);
                size += 6;
            }else if (m < 64){
                out.writeInt(1, 2);
                out.writeInt(m, 6);
                size += 8;
            }else if (m < 256) {
                out.writeInt(2, 2);
                out.writeInt(m, 8);
                size += 10;
            }else {
                out.writeInt(3, 2);
                out.writeInt(m, mValueBits[decimal_count-1]);
                size += 2;
                size += mValueBits[decimal_count-1];
            }

        }

        return this.size;
    }

    // 压缩整数部分
    private int compressIntegerValue(long int_value, int intSignal) {

        int diff = (int)(int_value - storedVal);

        // 写入符号为 主要为了区分-0和+0
        out.writeInt(intSignal, 1);

        size += 2;
        if (diff >= -1 && diff <= 1) {
            out.writeInt((diff + 1), 2); // Map -1 to 0, 0 to 1, 1 to 2 respectively
        } else{
            out.writeInt(3, 2); // //11
            if (diff < 0){
                out.writeBit(false);
                diff = -diff;
            } else {
                out.writeBit(true);
            }
            size += 1;
            if (diff >=2 && diff < 8) { // [4,8)
                out.writeInt(0, 1); // 0
                out.writeInt(diff, 3);
                size += 4;
            } else {
                out.writeInt(1, 1); //1  // [8,...)
                out.writeInt(diff, 16); // 暂用16个字节表示
                size += 17;
            }

        }
        storedVal = int_value;

        return this.size;


    }

    public BPlusTree getbPlusTree() {
        return bPlusTree;
    }

    public void setbPlusTree(BPlusTree bPlusTree) {
        Camel.bPlusTree = bPlusTree;
    }

    public BPlusTree2 getbPlusTree2() {
        return bPlusTree2;
    }

    public void setbPlusTree2(BPlusTree2 bPlusTree) {
        Camel.bPlusTree2 = bPlusTree2;
    }

    public BPlusDecimalTree getbPlusDecimalTree() {
        return bPlusDecimalTree;
    }

    public void setbPlusDecimalTree(BPlusDecimalTree bPlusDecimalTree) {
        Camel.bPlusDecimalTree = bPlusDecimalTree;
    }

    public int getSize() {
        return size;
    }

    public byte[] getOut() {
        return out.getBuffer();
    }
}
