package org.urbcomp.startdb.compress.camel.doubleprecision;

import org.junit.jupiter.api.Test;
import org.urbcomp.startdb.compress.camel.compressor.CamelCompressor;
import org.urbcomp.startdb.compress.camel.compressor.ICompressor;
import yyy.ts.compress.camel.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCamelTree {
    private static final String FILE_PATH = "src/test/resources/ElfTestData";

    private static final String[] FILENAMES = {
//            "/init.csv",    //First run a dataset to ensure the relevant hbase settings of the zstd and snappy compressors
            "/City-temp.csv", //
//            "/IR-bio-temp.csv",
//            "/Wind-Speed.csv",
//            "/PM10-dust.csv",
//            "/Stocks-UK.csv",
//            "/Stocks-USA.csv",
//            "/Stocks-DE.csv",
//            "/Dew-point-temp.csv",
//            "/Air-pressure.csv",
//            "/Basel-wind.csv",
//            "/Basel-temp.csv",
//            "/Bitcoin-price.csv",
//            "/Bird-migration.csv",
//            "/Air-sensor.csv",
//////
//            "/Food-price.csv",
//            "/electric_vehicle_charging.csv",
//            "/Blockchain-tr.csv",
//            "/SSD-bench.csv",
//            "/City-lat.csv",
//            "/City-lon.csv",
//            "/POI-lat.csv",
//            "/POI-lon.csv",

//            "/Cpu-usage.csv",
//            "/Disk-usage.csv",
//            "/Mem-usage.csv",


    };
    private static final String STORE_RESULT = "src/test/resources/result/result_camel.csv";

    private static final double TIME_PRECISION = 1000.0;
    List<Map<String, ResultStructure>> allResult = new ArrayList<>();

    @Test
    public void testCompressor() throws IOException {
        for (String filename : FILENAMES) {
            Map<String, List<ResultStructure>> result = new HashMap<>();
            testCamelCompressor(filename, result);
        }
    }


    private void testCamelCompressor(String fileName, Map<String, List<ResultStructure>> resultCompressor) throws FileNotFoundException {
        FileReader fileReader = new FileReader(FILE_PATH + fileName);

        float totalBlocks = 0;
        double[] values;
        long treeSize2 = 0l;
        long treeSize = 0l;
        long size = 0l;
        double time = 0;
        double timestamp = 0;
        while ((values = fileReader.nextBlock()) != null) {
            totalBlocks += 1;
            timestamp++;
            ICompressor[] compressors = new ICompressor[]{
                    new CamelCompressor(),
            };
            for (int i = 0; i < compressors.length; i++) {
                double encodingDuration;
                double decodingDuration;
                long start = System.nanoTime();
                ICompressor compressor = compressors[i];
                for (double value : values) {
                    compressor.addValue(value);
                }

                compressor.close();
                encodingDuration = System.nanoTime() - start;
                // index
                BPlusTree bPlusTree = compressor.getbPlusTree();
                BPlusTree2 bPlusTree2 = compressor.getbPlusTre2();
                BPlusDecimalTree bPlusDecimalTree = compressor.getbPlusDecimalTree();
                long intTreeSize = bPlusTree.levelOrderTraversal(bPlusTree);
                long intTreeSize2 = bPlusTree2.levelOrderTraversal(bPlusTree2);
                long decimalSize2 = bPlusDecimalTree.levelOrderTraversal(bPlusDecimalTree);
                treeSize = treeSize + intTreeSize;
                treeSize2 = treeSize2 + intTreeSize2 + decimalSize2;
                size = size + compressor.getSize();
                time += encodingDuration / TIME_PRECISION;

                // 查询
//                ValueSearch vs = new ValueSearch();
//                long search_time = System.nanoTime();
//                List<TSNode> list = vs.searchValue(bPlusDecimalTree, bPlusTree2, 64.2);
//                System.out.println("valueSearch:" + (System.nanoTime()-search_time)/1000.0);
//
//                RangeSearch rs = new RangeSearch();
//                search_time = System.nanoTime();
//                rs.searchRangeValue(bPlusTree, 66.7,  72.6);
//                System.out.println("rangeSearch:" + (System.nanoTime()-search_time)/1000.0);
//
//                SegmentSearch ss = new SegmentSearch();
//                search_time = System.nanoTime();
//                ss.searchSegment(bPlusTree);
//                System.out.println("segmentSearch:" + (System.nanoTime()-search_time)/1000.0);
//
//                search_time = System.nanoTime();
//                sl.search(4);
//                System.out.println("timeSearch:" + (System.nanoTime()-search_time)/1000.0);
            }


        }

        double treeRatio = (double) treeSize / size;
        double treeRatio2 = (double) treeSize2 / size;
//        long compress_time = (long) (time / TIME_PRECISION);
        System.out.println(fileName + " " + "sourceSize:" + totalBlocks * FileReader.DEFAULT_BLOCK_SIZE * 64.0);
//        System.out.println(fileName + " " + "compressSize:" + size);
//        System.out.println(fileName + " " + "compressRatio:" + ratio);
        System.out.println(fileName + " " + "treeSize:" + treeSize);
        System.out.println(fileName + " " + "treeRatio:" + treeRatio);
        System.out.println(fileName + " " + "treeSize2:" + treeSize2);
        System.out.println(fileName + " " + "treeRatio2:" + treeRatio2);
//        System.out.println(fileName + " " + compress_time);


//
    }



}
