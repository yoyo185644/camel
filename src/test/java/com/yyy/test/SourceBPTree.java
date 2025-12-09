package com.yyy.test;

import org.apache.jena.base.Sys;
import org.urbcomp.startdb.compress.camel.doubleprecision.FileReader;
import yyy.ts.compress.camel.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class SourceBPTree {
    private static final String FILE_PATH = "src/test/resources/ElfTestData";

    private static final String[] FILENAMES = {
//            "/init.csv",    //First run a dataset to ensure the relevant hbase settings of the zstd and snappy compressors
//            "/City-temp.csv", //
//            "/IR-bio-temp.csv",
            "/Wind-Speed.csv",
//            "/PM10-dust.csv",
//            "/Stocks-UK.csv",
//            "/Stocks-USA.csv",
//            "/Stocks-DE.csv",
//            "/Dew-point-temp.csv",
//            "/Bitcoin-price.csv",
//            "/Air-pressure.csv",
//            "/Basel-wind.csv",
//            "/Basel-temp.csv",
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
//
//            "/Cpu-usage.csv",
//            "/Disk-usage.csv",
//            "/Mem-usage.csv",


    };
    private static final String STORE_RESULT = "src/test/resources/result/result_source_index.csv";
    private static void sourceBPtree(String fileName) throws IOException {
        FileReader fileReader = new FileReader(FILE_PATH + fileName);

        float totalBlocks = 0;
        double[] values;
        long treeSize = 0l;

        while ((values = fileReader.nextBlock()) != null) {
            totalBlocks += 1;
            SourceBTree btree = new SourceBTree(3, new SkipList());
            for (double value : values) {
                btree.insert(value, 1);
//                bplustree.printTree();
            }
//             计算treeSize
            treeSize += btree.levelOrderTraversalWithPointer(btree);
//            btree.searchKeyNode(btree.getRoot(btree), 0.85);
            long start = System.nanoTime();
//            ArrayList<SkiplistNode> res = btree.levelOrderTraversal(btree);
            btree.searchKeyNode(btree.getRoot(btree), 0.85);
            System.out.println("=========="+ (System.nanoTime()-start)/1000.0);

        }
//        storeResult(fileName, (double)treeSize, (double)treeSize/1024/1024/8);

    }

    public static void storeResult(String fileName, double treeSizeB, double treeSizeM) throws IOException {
        String filePath = STORE_RESULT;
        File file = new File(filePath).getParentFile();
        if (!file.exists() && !file.mkdirs()) {
            throw new IOException("Create directory failed: " + file);
        }
        // 以追加模式打开文件
        try (FileWriter fileWriter = new FileWriter(filePath, true)) {
            String text = "fileName: " + fileName + "; treeSize(bit):" + treeSizeB + "; treeSize(M):" + treeSizeM + "\n";
            fileWriter.write(text);
            System.out.println("Append to file successful.");
        } catch (IOException e) {
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        for (String filename : FILENAMES){
            sourceBPtree(filename);
        }

    }

}
