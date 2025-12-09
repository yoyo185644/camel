package org.urbcomp.startdb.compress.camel.compressor;

import gr.aueb.delorean.chimp.Chimp;
import yyy.ts.compress.camel.BPlusDecimalTree;
import yyy.ts.compress.camel.BPlusTree;
import yyy.ts.compress.camel.BPlusTree2;

public class ChimpCompressor implements ICompressor {
    private final Chimp chimp;
    public ChimpCompressor() {
        chimp = new Chimp();
    }
    @Override public void addValue(double v) {
        chimp.addValue(v);
    }

    @Override public int getSize() {
        return chimp.getSize();
    }

    @Override public byte[] getBytes() {
        return chimp.getOut();
    }

    @Override public void close() {
        chimp.close();
    }

    @Override
    public BPlusTree getbPlusTree() {
        return null;
    }

    @Override
    public BPlusTree2 getbPlusTre2() {
        return null;
    }

    @Override
    public BPlusDecimalTree getbPlusDecimalTree() {
        return null;
    }
}
