package org.urbcomp.startdb.compress.camel.doubleprecision;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestQuantization {

    private static final String FILE_PATH = "src/test/resources/ElfTestData";

    private static final String[] FILENAMES = {
//            "/init.csv",    //First run a dataset to ensure the relevant hbase settings of the zstd and snappy compressors
//            "/City-temp.csv", //
//            "/IR-bio-temp.csv",
//            "/Wind-Speed.csv",
//            "/PM10-dust.csv",
//            "/Stocks-UK.csv",
//            "/Stocks-USA.csv",
//            "/Stocks-DE.csv",
//            "/Dew-point-temp.csv"
//            "/Bitcoin-price.csv",


            "/Air-pressure.csv",
            "/Basel-wind.csv",
            "/Basel-temp.csv",
//
//            "/Bird-migration.csv",
//            "/Air-sensor.csv",
//
//            "/Food-price.csv",
//            "/electric_vehicle_charging.csv",
//            "/Blockchain-tr.csv",
//            "/SSD-bench.csv",
//            "/City-lat.csv",
//            "/City-lon.csv",
//            "/POI-lat.csv",
//            "/POI-lon.csv",
////
            "/Cpu-usage.csv",
//            "/Disk-usage.csv",
//            "/Mem-usage.csv",



    };
    private static final String STORE_RESULT = "src/test/resources/result/result_camel.csv";

    public static void main(String[] args) throws IOException {
        for (String fileName : FILENAMES) {
            java.io.FileReader fileReader = new FileReader(FILE_PATH + fileName);
            BufferedReader reader = new BufferedReader(fileReader);
            String value;
            List<Double> datalist = new ArrayList<>();
            while ((value = reader.readLine()) != null) {
                if ("\"\"".equals(value) || value.isEmpty()) {
                    continue;
                }
                double doubleValue = Double.parseDouble(value);
                datalist.add(doubleValue);
            }
            long quantMin = -2147483648L;
//            long quantMin = -32768L;
//
            // int 16 max: 32767 min:-32768
            // int 32 max:2147483647L min: -2147483648L
//            long quantMax = 32767L;
            long quantMax = 2147483647L;
            double[] data = Arrays.stream(datalist.toArray())
                    .mapToDouble(obj -> Double.parseDouble(obj.toString()))
                    .toArray();
            // Min-Max Quantization
            double beginTimeCp = System.nanoTime();
            double dataMin = Arrays.stream(data).min().orElseThrow(() -> new IllegalArgumentException("Empty array"));
            double dataMax = Arrays.stream(data).max().orElseThrow(() -> new IllegalArgumentException("Empty array"));
            int[] quantizedData = minMaxQuantization(data, quantMin, dataMin, dataMax, quantMax);
            double endTimeCp = System.nanoTime();

            // Inverse Min-Max Quantization
            double beginTimeDecp = System.nanoTime();
            double[] inverseData = inverseMinMaxQuantization(quantizedData, dataMin, dataMax, quantMin, quantMax);
            double endTimeDecp = System.nanoTime();


            double[] divides = divideArrays(inverseData, data);

            double mean = calculateMean(divides);
            // 计算Mb/s
            double cpTime = (endTimeCp - beginTimeCp)/1000000000.0;
            double decpTime = (endTimeDecp - beginTimeDecp)/1000000000.0;
            long dataSize = datalist.size() * 64L;
//            System.out.println(dataSize);
//            System.out.println((endTimeCp - beginTimeCp)/1000);
            double cpTimeRate = dataSize/8.0/1024.0/1024.0/cpTime;
            double decpTimeRate = dataSize/8.0/1024.0/1024.0/decpTime;
//            System.out.println(fileName+ "精度: "+ mean +  ";压缩时间：" + cpTime + ";解压缩时间：" +
//                    decpTime + ";压缩速率：" + cpTimeRate + ";解压缩速率：" + decpTimeRate);

            System.out.println(fileName+ ", "+ mean + "," + cpTimeRate + "," + decpTimeRate);


        }




    }

    public static int[] minMaxQuantization(double[] data, double quantMin, double dataMin, double dataMax, double quantMax) {

        int[] quantizedData = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            quantizedData[i] = (int)(quantMin + ((data[i] - dataMin) * (quantMax - quantMin)) / (dataMax - dataMin));
        }
        return quantizedData;
    }

    public static double[] inverseMinMaxQuantization(int[] quantizedData, double dataMin, double dataMax, double quantMin, double quantMax) {
//        double dataMin = Arrays.stream(originalData).min().orElseThrow(() -> new IllegalArgumentException("Empty array"));
//        double dataMax = Arrays.stream(originalData).max().orElseThrow(() -> new IllegalArgumentException("Empty array"));

        double[] originalDataRestored = new double[quantizedData.length];
        for (int i = 0; i < quantizedData.length; i++) {
            originalDataRestored[i] = dataMin + ((quantizedData[i] - quantMin) * (dataMax - dataMin)) / (quantMax - quantMin);
        }
        return originalDataRestored;
    }

    public static double[] divideArrays(double[] current, double[] original) {
        if (current.length != original.length) {
            throw new IllegalArgumentException("数组长度不一致");
        }

        double[] divisionResults = new double[original.length];
        for (int i = 0; i < original.length; i++) {
            if (original[i] == 0) {
                continue;
            }
//            double divide = Math.abs(Math.abs(original[i]) - Math.abs(original[i]-current[i])) / Math.abs(original[i]);
//            double divide = Math.abs(current[i]) / Math.abs(original[i]);

            // 计算绝对误差
            double absoluteError = Math.abs(original[i] - current[i]);

            // 计算相对误差
            double relativeError = absoluteError / Math.abs(original[i]);

            // 计算精度，确保在0到1之间
            double precision = Math.max(0, 1 - relativeError);


            divisionResults[i] =  precision;
//            if (divide < 0) {
//                System.out.println(divide);
//            }
//            if (divide>1) {
//                divisionResults[i] =  divide;
//            } else {
//                divisionResults[i] = 1-(Math.abs(original[i]-current[i])) / original[i];
//            }

        }
        return divisionResults;
    }

    public static double calculateMean(double[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("数组为空");
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (double num : array) {
                sum = sum.add(BigDecimal.valueOf(num));

        }
        DecimalFormat df = new DecimalFormat("#.##############");

        BigDecimal average = sum.divide(BigDecimal.valueOf(array.length),  15, BigDecimal.ROUND_HALF_UP);

        return average.doubleValue();
    }
}
