package org.urbcomp.startdb.compress.camel.compressor;

import org.urbcomp.startdb.compress.camel.utils.Elf64Utils;

public abstract class AbstractElfCompressor implements ICompressor {
    private int size = 0;

    private int lastBetaStar = Integer.MAX_VALUE;

    public void addValue(double v) {
        // 二进制转成Long类型数
        long vLong = Double.doubleToRawLongBits(v);
        long vPrimeLong;

        if (v == 0.0 || Double.isInfinite(v)) {
            size += writeInt(2, 2); // case 10
            vPrimeLong = vLong;
        } else if (Double.isNaN(v)) {
            size += writeInt(2, 2); // case 10
            vPrimeLong = 0xfff8000000000000L & vLong;
        } else {
            // C1: v is a normal or subnormal
            int[] alphaAndBetaStar = Elf64Utils.getAlphaAndBetaStar(v, lastBetaStar);
            // 提取位于第 53 到 63 位之间的部分，并将其作为一个整数返回——提取指数部分
            int e = ((int) (vLong >> 52)) & 0x7ff;
            int gAlpha = Elf64Utils.getFAlpha(alphaAndBetaStar[0]) + e - 1023;
            int eraseBits = 52 - gAlpha;
            // 清零一个long类型的数的最高eraseBits位，保留其余位
            long mask = 0xffffffffffffffffL << eraseBits;
            // 保留 vLong中与 mask 中1对应的位，将其他位清零，
            long delta = (~mask) & vLong;
            // 保存betaStar
            if (delta != 0 && eraseBits > 4) {  // C2
                if(alphaAndBetaStar[1] == lastBetaStar) {
                    size += writeBit(false);    // case 0
                } else {
                    // 0x30 的二进制表示为 00110000
                    size += writeInt(alphaAndBetaStar[1] | 0x30, 6);  // case 11, 2 + 4 = 6
                    lastBetaStar = alphaAndBetaStar[1];
                }
                vPrimeLong = mask & vLong;
            } else {
                size += writeInt(2, 2); // case 10
                vPrimeLong = vLong;
            }
        }
        // 擦除之后的v
        Double vPrime = Double.longBitsToDouble(vPrimeLong);
//        System.out.println(vPrime);
        // 保存betaStar
        size += xorCompress(vPrimeLong);
    }

    public int getSize() {
        return size;
    }

    protected abstract int writeInt(int n, int len);

    protected abstract int writeBit(boolean bit);

    protected abstract int xorCompress(long vPrimeLong);

}
