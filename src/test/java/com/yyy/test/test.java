package com.yyy.test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class test {
    private static final long[] powers = {10L, 100L, 1000L, 10000L, 10000L};

    public static void main(String[] args) {

//        double value = 145.53; // 示例双精度浮点数
//        long intValue = (long) (value * 1000); // 将浮点数乘以 1,000,000 转换为整数
//        intValue = Math.abs(intValue); // 取绝对值，避免负数的影响
//        int decimal_count = 0;
//        while (intValue > 0) {
//            intValue /= 10;
//            decimal_count++;
//        }
//        decimal_count = decimal_count-((int)Math.log10(value)+1);
//
//        System.out.println("小数部分的位数估算是: " + decimal_count );
//        long xorValue = 0L;
//        long xor = 3;
//        long shiftedValue = xor << (52 - 2);
//        for (int i = 0; i < 64; i++) {
//            xorValue ^= (shiftedValue & (1L << i)); // 使用异或操作符直接计算xorValue
//        }
//
//        // 输出xorValue的64位二进制表示
//        for (int i = 63; i >= 0; i--) {
//            long mask = 1L << i; // 生成一个只有第i位为1的掩码
//            if ((xorValue & mask) != 0) {
//                System.out.print("1");
//            } else {
//                System.out.print("0");
//            }
//        }

//        byte[] arr = intToBinary(16, 5);
//        System.out.println(Arrays.toString(arr));
//
//        System.out.println(binaryToInt(arr));



        String osArch = System.getProperty("os.arch");
        // 判断是32位还是64位
        if (osArch.contains("64")) {
            System.out.println("64位的JVM");
        } else {
            System.out.println("32位的JVM");
        }


//        while (Math.abs(value * factor - Math.round(value * factor)) > epsilon) {
//            System.out.println(value * factor);
//            System.out.println(Math.round(value * factor));
//            factor *= 10.0;
//            decimalPlaces++;
//        }

//         尾数位的长度即是小数部分的位数估算

//        int xor = 2;
//        // 根据leadingZeroSNum和XOR拼接xorVal
//        long shiftedValue = xor << (52 - 1);
//        String xorString = String.format("%64s", Long.toBinaryString(shiftedValue)).replace(' ', '0');
//        System.out.println(xorString);
//        long shiftedValue2 = 0x1234567890ABCDEFL; // 示例值
//        long mask = -1L << (64 - Long.toBinaryString(shiftedValue2).length()); // 创建掩码
//        long paddedValue = shiftedValue2 | mask; // 应用掩码
//
//        // 将64位长的值转换为二进制字符串
//        String binaryString = Long.toBinaryString(paddedValue);
//        System.out.println(binaryString);
    }

    public static byte[] intToBinary(int num, int bitLength) {
        // 创建一个指定长度的字节数组
        byte[] byteArray = new byte[bitLength];

        // 将整数的二进制表示转换为字节数组
        for (int i = bitLength - 1; i >= 0; i--) {
            byteArray[i] = (byte) ((num >> (bitLength - 1 - i)) & 0x01);
        }

        return byteArray;
    }

    public static int binaryToInt(byte[] binaryArray) {
        int result = 0;
        for (int i = 0; i < binaryArray.length; i++) {
            result = (result << 1) | binaryArray[i];
        }
        return result;
    }

}
