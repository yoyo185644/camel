package yyy.ts.compress.camel;

//public class TSNode {
//    byte[] valueInt;
//    byte[] valueDecimal;
//    long timeStamp;
//    TSNode nextTS;
//    TSNode beforeTS;
//
//    public TSNode(byte[] valueInt, byte[] valueDecimal, long timeStamp) {
//        this.valueInt = valueInt;
//        this.valueDecimal = valueDecimal;
//        this.timeStamp = timeStamp;
//        this.nextTS = null;
//    }
//}

import java.util.ArrayList;

public class TSNode {

    /**
     * 分别指向构建树的根节点
     */
    SBPNode sbpNode;

    IntKeyNode2 intKeyNode;

    DecimalNode decimalNode;
    long timeStamp;
    TSNode nextTS;
    TSNode beforeTS;

    public TSNode(long timeStamp) {

        this.timeStamp = timeStamp;
        this.nextTS = null;
    }
}