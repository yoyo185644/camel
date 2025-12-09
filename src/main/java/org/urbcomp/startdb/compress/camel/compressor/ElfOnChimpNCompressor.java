package org.urbcomp.startdb.compress.camel.compressor;

import gr.aueb.delorean.chimp.ChimpN;
import gr.aueb.delorean.chimp.OutputBitStream;
import yyy.ts.compress.camel.BPlusDecimalTree;
import yyy.ts.compress.camel.BPlusTree;
import yyy.ts.compress.camel.BPlusTree2;

public class ElfOnChimpNCompressor extends AbstractElfCompressor{
    private final ChimpN chimpN;

    public ElfOnChimpNCompressor(int previousValues) {
        chimpN = new ChimpN(previousValues);
    }
    @Override protected int writeInt(int n, int len) {
        OutputBitStream os = chimpN.getOutputStream();
        os.writeInt(n, len);
        return len;
    }

    @Override protected int writeBit(boolean bit) {
        OutputBitStream os = chimpN.getOutputStream();
        os.writeBit(bit);
        return 1;
    }

    @Override protected int xorCompress(long vPrimeLong) {
        return chimpN.addValue(vPrimeLong);
    }

    @Override public byte[] getBytes() {
        return chimpN.getOut();
    }

    @Override public void close() {
        // we write one more bit here, for marking an end of the stream.
        writeInt(2, 2); // case 10
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
