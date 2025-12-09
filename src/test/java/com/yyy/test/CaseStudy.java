package com.yyy.test;


import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CaseStudy {
    public static String getIEEE754(Double doubleValue) {
//        float floatValue = 12.345f; // 你要转换的浮点数

        // 使用Double.doubleToRawLongBits()方法将双精度浮点数转换为64位长整数
        long longValue = Double.doubleToRawLongBits(doubleValue);

        // 将长整数表示为64位二进制字符串
        String binaryString = Long.toBinaryString(longValue);

        // 补足到64位
        while (binaryString.length() < 64) {
            binaryString = "0" + binaryString;
        }

        // 打印二进制表示
//        System.out.println("双精度浮点数 " + doubleValue + " 的IEEE-754二进制表示: " + binaryString);
        System.out.println("双精度浮点数 " + doubleValue + " 的IEEE-754二进制表示: " + binaryString.charAt(0) + " " +
                binaryString.substring(1, 12)+ " " +
                binaryString.substring(12, 64));
        return binaryString;
    }

    // 执行XOR操作
    public static String xorBinaryStrings(String binary1, String binary2) {
        // 确保两个二进制字符串长度一致
        int maxLength = Math.max(binary1.length(), binary2.length());
        binary1 = String.format("%" + maxLength + "s", binary1).replace(' ', '0');
        binary2 = String.format("%" + maxLength + "s", binary2).replace(' ', '0');

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < maxLength; i++) {
            if (binary1.charAt(i) == binary2.charAt(i)) {
                result.append('0');
            } else {
                result.append('1');
            }
        }
        return result.toString();
    }

    public static void main(String[] args) {
//        int originalValue = 1;
//        int count = 1;
//        int leading = 12;
//        // 左移15位并用0补全到64位
//        long shiftedValue = (long)originalValue << (64-count-leading);
//        String s = String.format("%64s", Long.toBinaryString(shiftedValue)).replace(' ', '0');
//        long v = Long.parseLong(s,2);
//        long v_prime = Double.doubleToLongBits(1.4);
//        String s_prime = Long.toBinaryString(v_prime);
//        double d  = v ^ v_prime;
//        String res = xorBinaryStrings(s, s_prime);
//        System.out.println(d);
//        // 使用 Long.parseLong 将二进制字符串转换为 long
//        long longValue = Long.parseLong(res, 2);


        // 使用 Double.longBitsToDouble 将 long 转换为 double
//        double doubleValue = Double.longBitsToDouble(longValue);
//
//        Double[] floatValueArr=new Double[]{-0.0041837485, 88.51872, 0.5, 12.5, 0.625,
//                34.599998, 34.6000000000000014, 34.6, 34.5};
//        for (Double floatValue : floatValueArr){
//            getIEEE754(floatValue);
//        }
//        Double double1 = 1.4853863503867337;
//        Double double2 = 1.0000042703086087;
//        Double double1 = 1.2;
//        Double double2 = 1.7;
//        Double double1 = 0.2;
//        Double double2 = Double.NaN;
//        Double double3 = 34.04;
//        String xor1 = getIEEE754(double1);
//        String xor2 = getIEEE754(double2);
//        String xor3 = getIEEE754(double3);
//        String xorRes = xorBinaryStrings(xor1, xor2);
        long start = System.nanoTime();
        long xorRes = Double.doubleToLongBits(1.942) ^ Double.doubleToLongBits(1.067);
//        System.out.println((System.nanoTime()-start)/1000.0);
//        int leadingZeros = Long.numberOfLeadingZeros(xorRes);
        int trailingZeros = Long.numberOfTrailingZeros(xorRes);
        System.out.println(trailingZeros);
//        int significantBits = 64 - leadingZeros - trailingZeros;
//        int tmp = (int) (xorRes>>>trailingZeros);
//        out.writeLong(xorRes >>> trailingZeros, significantBits);
//        BigDecimal d1 = BigDecimal.valueOf(1.99999999999999999);
//        BigDecimal d2 = BigDecimal.valueOf(0.00000762939453125  * 131027);
//        System.out.println(d1.subtract(d2));
//        System.out.println(d2);
//        System.out.println(0.48538635038673383 - 0.00000762939453125 * 63620);
//        System.out.println("------" + double1 + " " + xor1 + "------");
//        System.out.println("------" + double2 + " " + xor2 + "------");
//        System.out.println("------" + " XOR " + xorRes + "------");
//        int res = 1^3;
//        System.out.println("int xor : "+ res);
//        System.out.println(((511&255)&127&63&31&15&7&3&1)|2|4|8|16|32|64|128|256);
//        System.out.println((345&255&63&15&7)|8|16|64|256);
//        System.out.println((3&1));



    }

}

