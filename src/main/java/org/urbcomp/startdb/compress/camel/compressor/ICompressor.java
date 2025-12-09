package org.urbcomp.startdb.compress.camel.compressor;
import yyy.ts.compress.camel.BPlusDecimalTree;
import yyy.ts.compress.camel.BPlusTree;
import yyy.ts.compress.camel.BPlusTree2;

public interface ICompressor {
    void addValue(double v);
    int getSize();
    byte[] getBytes();
    void close();
    BPlusTree getbPlusTree();
    BPlusTree2 getbPlusTre2();
    BPlusDecimalTree getbPlusDecimalTree();
    default String getKey() {
        return getClass().getSimpleName();
    }
}
