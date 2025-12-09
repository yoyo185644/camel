package org.urbcomp.startdb.compress.camel.compressor;

import yyy.ts.compress.camel.*;

public class CamelCompressor implements ICompressor{

    // 不建树 纯压缩
    private final Camel camel;

    // 建树
//    private final CamelTree camel;

    public CamelCompressor() {
        this.camel = new Camel();
//        this.camel = new CamelTree();
    }

    @Override
    public void addValue(double v) {
        camel.addValue(v);
    }

    @Override
    public int getSize() {
        return camel.getSize();
    }


    public BPlusTree getbPlusTree() {
        return camel.getbPlusTree();
    }

    @Override
    public BPlusTree2 getbPlusTre2() {
        return camel.getbPlusTree2();
    }

    public BPlusDecimalTree getbPlusDecimalTree() {
        return camel.getbPlusDecimalTree();
    }


    @Override
    public byte[] getBytes() {
        return camel.getOut();
    }

    @Override
    public void close() {
        this.camel.close();
    }

    @Override
    public String getKey() {
        return ICompressor.super.getKey();
    }
}
