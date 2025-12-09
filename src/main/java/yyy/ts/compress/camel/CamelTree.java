package yyy.ts.compress.camel;

import gr.aueb.delorean.chimp.OutputBitStream;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import static yyy.ts.compress.camel.CamelUtils.binaryLongToBinary;
import static yyy.ts.compress.camel.CamelUtils.convertToBinary;

public class CamelTree {

    private long storedVal = 0;

    private long firstVal = 0;

    // 默认10000 对应 block大小位1000
    private final static int outStreamSize = 100000;
    private boolean first = true;
    private int size;
    private final static long END_SIGN = Double.doubleToLongBits(Double.NaN);

    private final static int DECIMAL_MAX_COUNT = 4;

    // 按照寻找到的m的值进行保存
    public final static int[] mValueBits = {3, 5, 7, 10, 15};

    public static Map<String, byte[]> compressVal = new HashMap<>();

    private final OutputBitStream out;

    private boolean TreeFlag = true;


    private static BPlusTree bPlusTree;

    private static BPlusTree2 bPlusTree2;

    private static BPlusDecimalTree bPlusDecimalTree;



    // We should have access to the series?
    public CamelTree() {
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
            return writeFirst(Double.doubleToRawLongBits(value));
        } else {
            return compressValue(Double.doubleToRawLongBits(value));
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
    private int compressValue(long value_bits) {
        double value = Double.longBitsToDouble(value_bits);
        // 压缩整数位
        size = compressIntegerValue((int)value);

        // 压缩小数位 默认小数位是1.**
        BigDecimal big_value = BigDecimal.valueOf(value);

        BigDecimal decimal_value = big_value.subtract(BigDecimal.valueOf(big_value.intValue()));

        size = compressDecimalValue(BigDecimal.valueOf(Math.abs(decimal_value.doubleValue())));
        System.out.println(value + ": " + size);

        if (TreeFlag) {
            TreeFlag = false;
            // ***** 范围 *****
            bPlusTree = new BPlusTree(3);
            // ***** 值 *****
            bPlusTree2 = new BPlusTree2(3);
            bPlusDecimalTree = new BPlusDecimalTree(3);
            bPlusDecimalTree = bPlusDecimalTree.buildTree(bPlusDecimalTree, compressVal.get("decimalCount"), compressVal.get("xorFlag"), compressVal.get("xorVal"));


        } else {
//            System.out.println("compressInt" + Arrays.toString(compressVal.get("compressInt")) + ";" + "decimalCount" + Arrays.toString(compressVal.get("decimalCount")) +
//                    "xorFlag" + Arrays.toString(compressVal.get("xorFlag")) + "xorVal" + Arrays.toString(compressVal.get("xorVal")) + "compressDecimal" + Arrays.toString(compressVal.get("compressDecimal")));
            // ***** 范围 ***** 对于范围查询就是每个整数后面加一颗树
            bPlusTree.insert(new BPlusDecimalTree(3), compressVal.get("compressInt"), compressVal.get("compressInt"), compressVal.get("decimalCount"),
                    compressVal.get("xorFlag"), compressVal.get("xorVal"), compressVal.get("compressDecimal"), 1);
            // ***** 值 ***** 对于值查询就是建立两颗树
            bPlusTree2.insert(compressVal.get("compressInt"), compressVal.get("compressInt"), compressVal.get("decimalCount"),
                    compressVal.get("xorFlag"), compressVal.get("xorVal"), compressVal.get("compressDecimal"), 1);
            bPlusDecimalTree.insert(compressVal.get("xorFlag"), compressVal.get("xorVal"), compressVal.get("compressInt"), compressVal.get("compressDecimal"), 1);

        }
        // ***** 范围 *****
        this.setbPlusTree(bPlusTree);
//         ***** 对于值查询就是统一成一颗树
        this.setbPlusTree2(bPlusTree2);
        this.setbPlusDecimalTree(bPlusDecimalTree);

        return size;
    }


    public int countDecimalPlaces(BigDecimal value) {
        String valueStr = value.toString();
        int decimalPointIndex = valueStr.indexOf('.');

        if (decimalPointIndex >= 0) {
            return valueStr.length() - decimalPointIndex - 1;
        } else {
            // No decimal point, so there are no decimal places
            return 0;
        }
    }


    // 压缩小数部分
    private int compressDecimalValue(BigDecimal decimal_value) {
        // 计算小数位数
        int decimal_count = countDecimalPlaces(decimal_value);
        BigDecimal decimal_value_prime = decimal_value;

        // 如果小数位数大于4 只保留4位的小数
        if (decimal_count > DECIMAL_MAX_COUNT) {
            decimal_value_prime = decimal_value.setScale(DECIMAL_MAX_COUNT, RoundingMode.HALF_UP);
            decimal_count = DECIMAL_MAX_COUNT;
        }
        if (decimal_count == 0) {
            decimal_count = 1;
        }

        out.writeInt(decimal_count-1, 2); // 保存字节数 00-1 01-2 10-3 11-4
        compressVal.put("decimalCount", convertToBinary(decimal_count, 2));
        size += 2;


        // 计算m的值
        BigDecimal threshold = BigDecimal.valueOf(Math.pow(2, -decimal_count)) ;
        BigDecimal m = decimal_value_prime;

        if (decimal_value_prime.compareTo(threshold) >= 0) {  // 计算m的值
            // 标志位：是否计算m的值
            out.writeBit(true);
            size += 1;
            m = decimal_value_prime.subtract(threshold.multiply(decimal_value_prime.divide(threshold, 0, BigDecimal.ROUND_DOWN)));
            // 对于m进行XOR操作
            long xor = Double.doubleToLongBits(decimal_value_prime.add(BigDecimal.valueOf(1.0)).doubleValue()) ^
                    Double.doubleToLongBits(m.add(BigDecimal.valueOf(1.0)).doubleValue());
//            int trailingZeros = Long.numberOfTrailingZeros(xor);
            int trailingZeros = 52 - decimal_count;
            // 保存小数位数长度的centerBits 保存decimal_count （四位最多就是1000）
            out.writeLong(xor >>> trailingZeros, decimal_count);
            size += decimal_count;// Store the meaningful bits of XOR
            compressVal.put("xorFlag", convertToBinary(1, 1));
            compressVal.put("xorVal", binaryLongToBinary(xor, 52-trailingZeros));
        } else {  // m就为原来的值
            out.writeBit(false);
            size += 1;
            compressVal.put("xorFlag", convertToBinary(0, 1));
        }

        int m_int = (m.multiply(BigDecimal.valueOf(Math.pow(10, decimal_count)))).intValue();
        // 保存m的值
        if (decimal_count <= 1) { // 如果是1 直接往后读decimal_count+1位
            out.writeInt(m_int, decimal_count+1);
            size += decimal_count+1;
            compressVal.put("compressDecimal", convertToBinary(m_int, decimal_count+1));
        } else if (decimal_count ==2) {
            if (m_int < 8) {
                out.writeInt(0, 1);
                out.writeInt(m_int, 3);
                size += 4;
                compressVal.put("compressDecimal", convertToBinary(m_int, 3));
            }  else {
                out.writeInt(1, 1);
                out.writeInt(m_int, 5);
                size += 6;
                compressVal.put("compressDecimal", convertToBinary(m_int, 5));
            }

        } else if (decimal_count == 3) {
            if (m_int < 2) {
                out.writeInt(0, 2);
                out.writeInt(m_int, 1);
                size += 3;
                compressVal.put("compressDecimal", convertToBinary(m_int, 2));
            }else if (m_int < 8){
                out.writeInt(1, 2);
                out.writeInt(m_int, 3);
                size += 5;
                compressVal.put("compressDecimal", convertToBinary(m_int, 3));
            }else if (m_int < 32) {
                out.writeInt(2, 2);
                out.writeInt(m_int, 5);
                size += 7;
                compressVal.put("compressDecimal", convertToBinary(m_int, 4));
            }else {
                out.writeInt(3, 2);
                out.writeInt(m_int, mValueBits[decimal_count-1]);
                size += 2;
                size += mValueBits[decimal_count-1];
                compressVal.put("compressDecimal", convertToBinary(m_int, mValueBits[decimal_count-1]));
            }

        } else {
            if (m_int < 16) {
                out.writeInt(0, 2);
                out.writeInt(m_int, 4);
                size += 6;
                compressVal.put("compressDecimal", convertToBinary(m_int, 4));
            }else if (m_int < 64){
                out.writeInt(1, 2);
                out.writeInt(m_int, 6);
                size += 8;
                compressVal.put("compressDecimal", convertToBinary(m_int, 6));
            }else if (m_int < 256) {
                out.writeInt(2, 2);
                out.writeInt(m_int, 8);
                size += 10;
                compressVal.put("compressDecimal", convertToBinary(m_int, 8));
            }else {
                out.writeInt(3, 2);
                out.writeInt(m_int, mValueBits[decimal_count-1]);
                size += 2;
                size += mValueBits[decimal_count-1];
                compressVal.put("compressDecimal", convertToBinary(m_int, mValueBits[decimal_count-1]));
            }

        }

        return this.size;
    }

    // 压缩整数部分
    private int compressIntegerValue(long int_value) {

        long diff_value =  int_value - storedVal;
        // 用于建索引
        long first_diff_value = int_value - firstVal;
        if (Math.abs(first_diff_value) >=0 && Math.abs(first_diff_value) < 2)
        {
            compressVal.put("compressInt", convertToBinary((int) first_diff_value, 2));
        }else if (Math.abs(first_diff_value) >=2 && Math.abs(first_diff_value) < 4){
            compressVal.put("compressInt", convertToBinary((int) first_diff_value, 3));
        } else if (Math.abs(first_diff_value) >=4 && Math.abs(first_diff_value) < 8){
            compressVal.put("compressInt", convertToBinary((int) first_diff_value, 4));
        } else {
            compressVal.put("compressInt", convertToBinary((int) first_diff_value, 16));
        }

        int diff = (int) diff_value;
        if (diff ==0){ // [0,2)
            out.writeInt(0, 2); // 00
            size += 2;
        } else if (diff == 1){
            out.writeInt(1, 2); //01
            size += 2;
        } else if (diff == -1){
            out.writeInt(2, 2); //10
            size += 2;
        } else{
            out.writeInt(3, 2); // //11
            size += 2;
            if (diff < 0){
                out.writeBit(false);
                diff = -diff;
            } else {
                out.writeBit(true);
            }
            size += 1;
            if (diff >=2 && diff < 8) { // [4,8)
                out.writeInt(0, 1); // 10
                out.writeInt((int) diff, 3);
                size += 4;
            } else {
                out.writeInt(1, 1); //11  // [8,...)
                out.writeInt((int) diff, 16); // 暂用16个字节表示
                size += 17;
            }

        }
        // 针对BP数据集
//        if (diff_value >=0 && diff_value < 16){ // [0,2)
//            out.writeInt(0, 2); // 00
//            out.writeInt((int) diff_value, 1);
//            compressVal.put("compressInt", convertToBinary((int) first_diff_value, 4));
//            size += 1;
//        } else if (diff_value >=16 && diff_value < 128) { // [2,4)
//            out.writeInt(1, 2); // 01
//            out.writeInt((int) diff_value, 2);
//            compressVal.put("compressInt", convertToBinary((int) first_diff_value, 7));
//            size += 2;
//        } else if (diff_value >=128 && diff_value < 1024) { // [4,8)
//            out.writeInt(2, 2); // 10
//            out.writeInt((int) diff_value, 3);
//            compressVal.put("compressInt", convertToBinary((int) first_diff_value, 10));
//            size += 3;
//        } else {
//            out.writeInt(3, 2); //11  // [8,...)
//            out.writeInt((int) diff_value, 16); // 暂用16个字节表示
//            compressVal.put("compressInt", convertToBinary((int) first_diff_value, 13));
//            size += 16;
//        }
        storedVal = int_value;

        return this.size;


    }

    public BPlusTree getbPlusTree() {
        return bPlusTree;
    }

    public void setbPlusTree(BPlusTree bPlusTree) {
        CamelTree.bPlusTree = bPlusTree;
    }

    public BPlusTree2 getbPlusTree2() {
        return bPlusTree2;
    }

    public void setbPlusTree2(BPlusTree2 bPlusTree) {
        CamelTree.bPlusTree2 = bPlusTree2;
    }

    public BPlusDecimalTree getbPlusDecimalTree() {
        return bPlusDecimalTree;
    }

    public void setbPlusDecimalTree(BPlusDecimalTree bPlusDecimalTree) {
        CamelTree.bPlusDecimalTree = bPlusDecimalTree;
    }

    public int getSize() {
        return size;
    }

    public byte[] getOut() {
        return out.getBuffer();
    }
}
