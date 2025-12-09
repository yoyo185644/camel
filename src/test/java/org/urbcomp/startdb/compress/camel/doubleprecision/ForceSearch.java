package org.urbcomp.startdb.compress.camel.doubleprecision;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ForceSearch {
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
////
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

    private static final double TIME_PRECISION = 1000.0;

    @Test
    public void testCompressor() throws IOException {
        for (String filename : FILENAMES) {

            ForceSearch(filename);
        }
    }

    private List ForceSearch(String fileName) throws FileNotFoundException {
        FileReader fileReader = new FileReader(FILE_PATH + fileName);

        double[] values;

        ArrayList res = new ArrayList();
        int block = 0;
        int i = 0;
        double total_precision = 0;
        double precisionArr[] = new double[2000];
        while ((values = fileReader.nextBlock()) != null) {
            long start = System.nanoTime();
            for (double value : values) {

                if (value == 1.00) {
                    precisionArr[i++] =  value;
                }

            }
            long end = System.nanoTime();
            System.out.println((end-start)/1000.0);
        }
//        BigDecimal precision_res = new BigDecimal(total_precision).divide(new BigDecimal(i),10, BigDecimal.ROUND_HALF_UP);
//        System.out.println(fileName + ":" + precision_res.doubleValue());

//        double precision_var = calculateVariance(precisionArr, precision_res.doubleValue());
//        System.out.println(fileName + ":" + precision_res.doubleValue() + ";variance:" + precision_var);
        return res;
//
    }

    // 计算方差
    public static double calculateVariance(double[] data, double mean) {
        double sumSquaredDiff = 0;
        for (double num : data) {
            double diff = num - mean;
            sumSquaredDiff += Math.pow(diff, 2);
        }
        return sumSquaredDiff / (data.length - 1);
    }



    /***
     * /Air-pressure.csv:5.82E-7
     * /Basel-wind.csv:1.00021E-5
     * /Basel-temp.csv:2.42294E-5
     * /Bird-migration.csv:1.8359E-6
     * /Air-sensor.csv:3.86658E-5
     * /POI-lat.csv:1.284628E-4
     * /POI-lon.csv:2.761884E-4
     * /Cpu-usage.csv:3.4495E-5
     * /Disk-usage.csv:1.549E-6
     * /Mem-usage.csv:1.4593E-6
     * 
     * 1. \(5.82E-7\) 转换为十进制数：\(0.000000582\)
     * 2. \(1.00021E-5\) 转换为十进制数：\(0.0000100021\)
     * 3. \(2.42294E-5\) 转换为十进制数：\(0.0000242294\)
     * 4. \(1.8359E-6\) 转换为十进制数：\(0.0000018359\)
     * 5. \(3.86658E-5\) 转换为十进制数：\(0.0000386658\)
     * 6. \(1.284628E-4\) 转换为十进制数：\(0.0001284628\)
     * 7. \(2.761884E-4\) 转换为十进制数：\(0.0002761884\)
     * 8. \(3.4495E-5\) 转换为十进制数：\(0.000034495\)
     * 9. \(1.549E-6\) 转换为十进制数：\(0.000001549\)
     * 10. \(1.4593E-6\) 转换为十进制数：\(0.0000014593\)
     */

}
