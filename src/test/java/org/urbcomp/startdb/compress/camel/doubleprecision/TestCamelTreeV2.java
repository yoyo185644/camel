package org.urbcomp.startdb.compress.camel.doubleprecision;

import org.junit.jupiter.api.Test;
import org.urbcomp.startdb.compress.camel.compressor.CamelCompressor;
import org.urbcomp.startdb.compress.camel.compressor.ICompressor;
import yyy.ts.compress.camel.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestCamelTreeV2 {
    private static final String FILE_PATH = "src/test/resources/ElfTestData";

    private static final String[] FILENAMES = {
//            "/init.csv",    //First run a dataset to ensure the relevant hbase settings of the zstd and snappy compressors
//            "/City-temp.csv", //
//            "/IR-bio-temp.csv",
//            "/Wind-Speed.csv",
//            "/PM10-dust.csv",
//            "/Stocks-UK.csv",
//            "/Stocks-USA.csv",
            "/Stocks-DE.csv",
            "/Dew-point-temp.csv",
//            "/Bitcoin-price.csv",
//            "/Air-pressure.csv",
//            "/Basel-wind.csv",
//            "/Basel-temp.csv",
            "/Bird-migration.csv",
//            "/Air-sensor.csv",
//////
            "/Food-price.csv",
//            "/electric_vehicle_charging.csv",
//            "/Blockchain-tr.csv",
//            "/SSD-bench.csv",
//            "/City-lat.csv",
//            "/City-lon.csv",
//            "/POI-lat.csv",
//            "/POI-lon.csv",
//
//            "/Cpu-usage.csv",
//            "/Disk-usage.csv",
//            "/Mem-usage.csv",


    };
    private static final String STORE_RESULT = "src/test/resources/result/result_compress_index.csv";


    @Test
    public void testCompressor() throws IOException {
        for (String filename : FILENAMES) {
            Map<String, List<ResultStructure>> result = new HashMap<>();
            testCamelCompressor(filename, result);
        }
    }


    private void testCamelCompressor(String fileName, Map<String, List<ResultStructure>> resultCompressor) throws IOException {
        FileReader fileReader = new FileReader(FILE_PATH + fileName);

        float totalBlocks = 0;
        double[] values;
        long treeSize2 = 0l;
        long treeSize = 0l;
        long size = 0l;
        while ((values = fileReader.nextBlock()) != null) {
            totalBlocks += 1;
            ICompressor[] compressors = new ICompressor[]{
                    new CamelCompressor(),
            };
            for (int i = 0; i < compressors.length; i++) {
                long start = System.nanoTime();
                ICompressor compressor = compressors[i];
                for (double value : values) {
                    compressor.addValue(value);
                }

                compressor.close();
                // index
                // merge-index
                BPlusTree bPlusTree = compressor.getbPlusTree();
                // integer tree
                BPlusTree2 bPlusTree2 = compressor.getbPlusTre2();
                // decimal tree
                BPlusDecimalTree bPlusDecimalTree = compressor.getbPlusDecimalTree();
                long mergeTreeSize = bPlusTree.levelOrderTraversalWithPointer(bPlusTree);
                long intTreeSize2 = bPlusTree2.levelOrderTraversalWithPointer(bPlusTree2);
                long decimalSize2 = bPlusDecimalTree.levelOrderTraversalWithPointer(bPlusDecimalTree);

                treeSize = treeSize + mergeTreeSize;
                treeSize2 = treeSize2 + intTreeSize2 + decimalSize2;
                size = size + compressor.getSize();

                // 查询
//                ValueSearch vs = new ValueSearch();
//                long search_time = System.nanoTime();
//                List<SkiplistNode> list = vs.searchValue(bPlusDecimalTree, bPlusTree2, 0.21);
////                System.out.println("valueSearch:" + (System.nanoTime()-search_time)/1000.0);
//                System.out.print((System.nanoTime()-search_time)/1000.0 + ",");
//                RangeSearch rs = new RangeSearch();
//                search_time = System.nanoTime();
//                rs.searchRangeValue(bPlusTree, 0.21,  0.97);
////                System.out.println("rangeSearch:" + (System.nanoTime()-search_time)/1000.0);
//                System.out.print((System.nanoTime()-search_time)/1000.0 + ",");
//
//                SegmentSearch ss = new SegmentSearch();
//                search_time = System.nanoTime();
////                ss.searchSegment(bPlusTree);
//                ss.searchSegmentDecimal(bPlusDecimalTree);
//                System.out.print((System.nanoTime()-search_time)/1000.0 + ",");
//
//                search_time = System.nanoTime();
//                sl.search(1260);
//                System.out.println((System.nanoTime()-search_time)/1000.0 + ",");
            }


        }

        // 与压缩完的值进行对比
//        double treeRatio = (double) treeSize / size;
//        double treeRatio2 = (double) treeSize2 / size;
//        System.out.println(fileName + " " + "sourceSize:" + totalBlocks * FileReader.DEFAULT_BLOCK_SIZE * 64.0);
//        System.out.println(fileName + " " + "treeSize:" + treeSize);
//        System.out.println(fileName + " " + "treeRatio:" + treeRatio);
//        System.out.println(fileName + " " + "treeSize2:" + treeSize2);
//        System.out.println(fileName + " " + "treeRatio2:" + treeRatio2);

        // 与原始数值进行对比
        storeResult(fileName, treeSize, (double)treeSize/1024/1024/8, treeSize2, (double)treeSize2/1024/1024/8);
//        System.out.println(fileName + " " + "treeSize(bit):" + treeSize + " " + "treeSize(M):" + (double)treeSize/1024/1024/8);
//        System.out.println(fileName + " " + "treeSize2(bit):" + treeSize2 + " " + "treeSize(M):" + (double)treeSize2/1024/1024/8);


    }

    public static void storeResult(String fileName, double mergeTreeSizeB, double mergeTreeSizeM,  double splitTreeSizeB, double splitTreeSizeM) throws IOException {
        String filePath = STORE_RESULT;
        File file = new File(filePath).getParentFile();
        if (!file.exists() && !file.mkdirs()) {
            throw new IOException("Create directory failed: " + file);
        }
        // 以追加模式打开文件
        try (FileWriter fileWriter = new FileWriter(filePath, true)) {
            String text = "fileName: " + fileName + "; mergeTreeSize(bit):" + mergeTreeSizeB + "; mergeTreeSize(M):" + mergeTreeSizeM +
            "; splitTreeSize(bit):" + splitTreeSizeB + "; splitTreeSize(M):" + splitTreeSizeM+ "\n";
            fileWriter.write(text);
            System.out.println("Append to file successful.");
        } catch (IOException e) {
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }


}
