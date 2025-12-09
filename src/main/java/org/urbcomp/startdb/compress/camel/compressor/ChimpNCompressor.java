package org.urbcomp.startdb.compress.camel.compressor;

import gr.aueb.delorean.chimp.ChimpN;
import yyy.ts.compress.camel.BPlusDecimalTree;
import yyy.ts.compress.camel.BPlusTree;
import yyy.ts.compress.camel.BPlusTree2;

public class ChimpNCompressor implements ICompressor {
    private final ChimpN chimpN;

    public ChimpNCompressor(int previousValues) {
        chimpN = new ChimpN(previousValues);
    }

    @Override public void addValue(double v) {
        chimpN.addValue(v);
    }

    @Override public int getSize() {
        return chimpN.getSize();
    }

    @Override public byte[] getBytes() {
        return chimpN.getOut();
    }

    @Override public void close() {
        chimpN.close();
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
