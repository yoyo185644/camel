package org.urbcomp.startdb.compress.camel.compressor;

import fi.iki.yak.ts.compression.gorilla.CompressorOS;
import gr.aueb.delorean.chimp.OutputBitStream;
import yyy.ts.compress.camel.BPlusDecimalTree;
import yyy.ts.compress.camel.BPlusTree;
import yyy.ts.compress.camel.BPlusTree2;

public class ElfOnGorillaCompressorOS extends AbstractElfCompressor{
    private final CompressorOS gorilla;

    public ElfOnGorillaCompressorOS(){
        gorilla = new CompressorOS();
    }

    @Override protected int writeInt(int n, int len) {
        OutputBitStream os = gorilla.getOutputStream();
        os.writeInt(n, len);
        return len;
    }

    @Override protected int writeBit(boolean bit) {
        OutputBitStream os = gorilla.getOutputStream();
        os.writeBit(bit);
        return 1;
    }

    @Override protected int xorCompress(long vPrimeLong) {
        return gorilla.addValue(vPrimeLong);
    }

    @Override public byte[] getBytes() {
        return gorilla.getOutputStream().getBuffer();
    }

    @Override public void close() {
        // we write one more bit here, for marking an end of the stream.
        writeInt(2, 2); // case 10
        gorilla.close();
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
