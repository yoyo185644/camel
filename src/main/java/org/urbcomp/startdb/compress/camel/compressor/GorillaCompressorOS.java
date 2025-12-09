package org.urbcomp.startdb.compress.camel.compressor;

import fi.iki.yak.ts.compression.gorilla.CompressorOS;
import yyy.ts.compress.camel.BPlusDecimalTree;
import yyy.ts.compress.camel.BPlusTree;
import yyy.ts.compress.camel.BPlusTree2;

public class GorillaCompressorOS implements ICompressor {
    private final CompressorOS gorilla;
    public GorillaCompressorOS() {
        this.gorilla = new CompressorOS();
    }

    @Override public void addValue(double v) {
        this.gorilla.addValue(v);
    }

    @Override public int getSize() {
        return this.gorilla.getSize();
    }

    @Override public byte[] getBytes() {
        return this.gorilla.getOutputStream().getBuffer();
    }

    @Override public void close() {
        this.gorilla.close();
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
