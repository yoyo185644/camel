package yyy.ts.compress.camel;

public class SkiplistNode {

    int key;
    Double timeStamp;
    SkiplistNode right,down;//左右上下四个方向的指针
    public SkiplistNode (Double value) {
        this.timeStamp=value;
    }


    SBPNode sbpNode;

    IntKeyNode2 intKeyNode;

    DecimalNode decimalNode;

    public int level;




}
