package org.urbcomp.startdb.compress.camel.decompressor;

import yyy.ts.compress.camel.CamelDecompressor;

import java.util.List;

public class CamelDecompressorOS implements IDecompressor{

    private CamelDecompressor camelDecompressor;

    public CamelDecompressorOS (byte[] bytes) {
        camelDecompressor =  new CamelDecompressor(bytes);
    }

    @Override
    public List<Double> decompress() {
        return camelDecompressor.getValues();
    }
}
