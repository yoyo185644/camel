package yyy.ts.compress.camel;

import java.util.List;

/**
 * 块查询
 */
public class SegmentSearch {
    public List<SkiplistNode> searchSegment (BPlusTree bPlusTree) {
        long start_time = System.nanoTime();
        List<SkiplistNode> TSNodes = bPlusTree.levelOrderTraversalList(bPlusTree);
//        System.out.println("segmentSearch:" + (System.nanoTime()-start_time)/1000.0);
        return TSNodes;
    }

    public List<SkiplistNode> searchSegmentDecimal (BPlusDecimalTree bPlusTree) {
        long start_time = System.nanoTime();
        List<SkiplistNode> TSNodes = bPlusTree.getAllLeaf(bPlusTree);
//        System.out.println("segmentSearch:" + (System.nanoTime()-start_time)/1000.0);
        return TSNodes;
    }

}
