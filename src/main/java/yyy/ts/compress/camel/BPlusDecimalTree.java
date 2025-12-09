package yyy.ts.compress.camel;


import java.nio.ByteBuffer;
import java.util.*;

import  static yyy.ts.compress.camel.CamelUtils.*;

class FlagTrueNode{
    byte flag;
    LinkedList<DecimalNode> decimalNodes;

    public FlagTrueNode(byte flag, LinkedList<DecimalNode> decimalNodes){
        this.flag = flag;
        this.decimalNodes = decimalNodes;
    }

}

class FlagFalseNode{
    byte flag;
    ArrayList<SkiplistNode> tsNodeList;
    public FlagFalseNode(byte flag, ArrayList<SkiplistNode> tsNodeList){
        this.flag = flag;
        this.tsNodeList = tsNodeList;
    }

}
// 最后一个节点
class DecimalNode{

    //  用BitSet代替byte;
    byte[] key;

//    int count;

    ArrayList<SkiplistNode> tsNodeList;

    public DecimalNode(byte[] key, int count, ArrayList<SkiplistNode> tsNodeList){
        this.key = key;
//        this.count = count;
        this.tsNodeList = tsNodeList;

    }
}

class KeyNode{
    byte[] key;
    FlagFalseNode flagFalseNode;
    FlagTrueNode flagTrueNode;

    KeyNode prev;
    KeyNode next;


    public KeyNode(byte[] key, FlagTrueNode flagTrueNode, FlagFalseNode flagFalseNode) {
        this.key = key;
        this.flagFalseNode = flagFalseNode;
        this.flagTrueNode = flagTrueNode;

    }
}


class BPlusDecimalTreeNode {
    boolean isLeaf;
    List<KeyNode> keys;
    List<BPlusDecimalTreeNode> children;

    public BPlusDecimalTreeNode(boolean isLeaf) {
        this.isLeaf = isLeaf;
        this.keys = new ArrayList<>();
        this.children = new ArrayList<>();
    }
}

public class BPlusDecimalTree {
    private BPlusDecimalTreeNode root;
    private int order;

    private SkipList skipList;
    private static boolean buildFlag = false;




    // 按照寻找到的m的值进行保存

    public BPlusDecimalTree(int order, SkipList skipList) {
        this.root = new BPlusDecimalTreeNode(true);
        this.order = order;
        this.skipList = skipList;
    }

    public void insert(byte[] flag, byte[] xorVal, byte[] compressInt, byte[] compressDecimal, double timestamp) {
        // 寻找小数部分
        if (buildFlag) {
            KeyNode node = searchKeyNode(binaryToInt(compressDecimal));
            if (node != null) {
                SkiplistNode tsNode =  skipList.add(timestamp);
                // 如果flag=1
                if (binaryToInt(flag) == 1) {
                    // 查询flagTrueNode是否存在
                    if (node.flagTrueNode != null) {
                        LinkedList<DecimalNode> decimalNodes = node.flagTrueNode.decimalNodes;
                        DecimalNode decimalNode= searchDecimalNode(decimalNodes, xorVal);
                        if (decimalNode != null) {
//                            decimalNode.count ++;
                            decimalNode.tsNodeList.add(tsNode);
                        } else {
                            // 说明第一次指定异或完的值
                            ArrayList<SkiplistNode> tsNodeList = new ArrayList<>();
                            tsNodeList.add(tsNode);
                            DecimalNode decimalNodeNew = new DecimalNode(xorVal, 1, tsNodeList);
                            LinkedList<DecimalNode> decimalNodesList = new LinkedList<>();
                            decimalNodesList.add(decimalNodeNew);
                        }
                    } else {
                        // 构建一个TSNode
                        ArrayList<SkiplistNode> tsNodeList = new ArrayList<>();
                        tsNodeList.add(tsNode);
                        // 插入不同的byte值
                        DecimalNode decimalNode = new DecimalNode(xorVal, 1, tsNodeList);
                        LinkedList<DecimalNode> decimalNodesList = new LinkedList<>();
                        decimalNodesList.add(decimalNode);
                        byte flagNew = 1;
                        node.flagTrueNode = new FlagTrueNode(flagNew, decimalNodesList);
                    }

                } else {
                    if (node.flagFalseNode != null) {
                        ArrayList<SkiplistNode> tsNodeList = node.flagFalseNode.tsNodeList;
                        tsNodeList.add(tsNode);
                    }else {
                        ArrayList<SkiplistNode> tsNodeList = new ArrayList<>();
                        tsNodeList.add(tsNode);
                        byte flagNew = 0;
                        node.flagFalseNode = new FlagFalseNode(flagNew, tsNodeList);
                    }
                }
                if (root.keys.size() == (2 * order) - 1) {
                    BPlusDecimalTreeNode newRoot = new BPlusDecimalTreeNode(false);
                    newRoot.children.add(root);
                    splitChild(newRoot, 0);
                    root = newRoot;
                }
                return;
            }

        }
         // 检查当前树的根节点是否已经达到了其最大容量 如果已经达到最大容量，需要进行分裂操作
        if (root.keys.size() == (2 * order) - 1) {
            BPlusDecimalTreeNode newRoot = new BPlusDecimalTreeNode(false);
            newRoot.children.add(root);
            splitChild(newRoot, 0);
            root = newRoot;
        }
        insertNonFull(root, compressDecimal);
    }

    // 在linkedlist里面查找是否存在，存在则返回这个linkedlist
    public DecimalNode searchDecimalNode(LinkedList<DecimalNode> decimalNodes, byte[] decimalKey) {
        // 获取列表迭代器
        ListIterator<DecimalNode> iterator = decimalNodes.listIterator();

        // 遍历链表并查找目标元素
        while (iterator.hasNext()) {
            if (binaryToInt(iterator.next().key) == binaryToInt(decimalKey)) {
                // 找到目标元素，可以通过 iterator 获取当前节点
                return iterator.previous();
            }
        }
        return null;

    }

    private void insertNonFull(BPlusDecimalTreeNode node, byte[] compressDecimal) {
        int i = node.keys.size() - 1;

        if (node.isLeaf) {
            while (i >= 0 && decompressDecimal(compressDecimal) < decompressDecimal(node.keys.get(i).key)) {
                i--;
            }
            KeyNode keyNode = new KeyNode(compressDecimal, null, null);
            node.keys.add(i + 1, keyNode);
            // 更新叶子节点的双向链表
            if (i + 2 < node.keys.size()) {
                node.keys.get(i + 1).next = node.keys.get(i + 2);
            }
            if (i >= 0) {
                node.keys.get(i + 1).prev = node.keys.get(i);
                node.keys.get(i).next = node.keys.get(i + 1); // 更新前一个节点的 next 指针
            }
        } else {
            while (i >= 0 && decompressDecimal(compressDecimal) < decompressDecimal(node.keys.get(i).key)) {
                i--;
            }
            i++;

            if (node.children.get(i).keys.size() == (2 * order) - 1) {
                splitChild(node, i);
                if (decompressDecimal(compressDecimal) > decompressDecimal(node.keys.get(i).key)) {
                    i++;
                }
            }
            insertNonFull(node.children.get(i), compressDecimal);
        }
    }

    private void splitChild(BPlusDecimalTreeNode parent, int index) {
        BPlusDecimalTreeNode child = parent.children.get(index);
        BPlusDecimalTreeNode newChild = new BPlusDecimalTreeNode(child.isLeaf);

        parent.keys.add(index, child.keys.get(order - 1));

        for (int j = 0; j < order - 1; j++) {
            newChild.keys.add(child.keys.remove(order));
        }

        if (!child.isLeaf) {
            for (int j = 0; j < order; j++) {
                if (order >= child.children.size()){
                    break;
                }
                newChild.children.add(child.children.remove(order));
            }
        }

        parent.children.add(index + 1, newChild);
    }

    // 在B+树中查找指定键值是否存在
    public boolean search(byte[] compressDecimal) {
        return searchKey(root, compressDecimal);
    }

    private boolean searchKey(BPlusDecimalTreeNode node, byte[] compressDecimal) {
        int i = 0;
        while (i < node.keys.size() && decompressDecimal(compressDecimal) > decompressDecimal(node.keys.get(i).key)) {
            i++;
        }

        if (i < node.keys.size() && decompressDecimal(compressDecimal) == decompressDecimal(node.keys.get(i).key)) {
            // Key found
            return true;
        }

        if (node.isLeaf) {
            // Key not found
            return false;
        }

        // Recur to the next level
        return searchKey(node.children.get(i), compressDecimal);
    }

    public BPlusDecimalTree buildTree(BPlusDecimalTree bPlusTree,  byte[] decimalCount, byte[] xorFlag, byte[] xorVal) {
        int[] range = new int[]{0, 5, 25, 125, 625};
        int decimal_Count = binaryToInt(decimalCount);
        SkipList skipList = new SkipList();
        for (int key = 0; key < range[decimal_Count]; key ++) {
            // todo 参数compressDecimal修改成 compressInt
            bPlusTree.insert(xorFlag, xorVal, null, compressDecimal(decimal_Count, key), 1);
        }
        buildFlag = true;
        return bPlusTree;
    }

    // 在B+树中查找指定键值的叶子节点
    public KeyNode searchKeyNode(int key) {
        return searchKeyNode(root, key);
    }

    private KeyNode searchKeyNode(BPlusDecimalTreeNode node, int key) {
        int i = 0;
        while (i < node.keys.size() && key > binaryToInt(node.keys.get(i).key)) {
            i++;
        }

        if (node.isLeaf) {
            // Key not found
            List<KeyNode> keyNodes = node.keys;
            KeyNode keyNode = binarySearchByKey(keyNodes, key);
            return keyNode;
        }

        // Recur to the next level
        return searchKeyNode(node.children.get(i), key);
    }

    private static KeyNode binarySearchByKey(List<KeyNode> keys, Integer targetKey) {
        int low = 0;
        int high = keys.size() - 1;

        while (low <= high) {
            int mid = (low + high) / 2;

            int compareResult = targetKey.compareTo(decompressDecimal(keys.get(mid).key));

            if (compareResult == 0) {
                return keys.get(mid); // 找到关键字对应的值
            } else if (compareResult < 0) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }

        return null; // 未找到关键字对应的值
    }


    //解压小数部分
    public static int decompressDecimal(byte[] decimalCompress) {
        return binaryToInt(decimalCompress);

    }


    public long levelOrderTraversal(BPlusDecimalTree tree){
        int size = 0;
        if (tree == null) {
            System.out.println("The tree is empty.");
            return size;
        }

        Queue<BPlusDecimalTreeNode> queue = new LinkedList<>();
        queue.offer(tree.root);

        while (!queue.isEmpty()) {
            BPlusDecimalTreeNode current = queue.poll();
            int keySize  = current.keys.size();
            for (int i =0; i < keySize; i++) {
                size = size + current.keys.get(i).key.length;
                if (current.keys.get(i).flagFalseNode!=null) {
                    size = size + 1;
                }
                if (current.keys.get(i).flagTrueNode!=null) {
                    int xorSize = current.keys.get(i).flagTrueNode.decimalNodes.size();
                    for (int j = 0; j < xorSize; j++) {
                        size += current.keys.get(i).flagTrueNode.decimalNodes.get(j).key.length;
                        // 计数单位
//                        size += 32;
                    }
                }
            }

            if (current.children != null) {
                for (BPlusDecimalTreeNode child : current.children) {
                    if (child != null) {
                        queue.offer(child);
                    }
                }
            }
        }
        return size;
    }


    public long levelOrderTraversalWithPointer(BPlusDecimalTree tree){
        long size = 0;
        if (tree == null) {
            System.out.println("The tree is empty.");
            return size;
        }

        Queue<BPlusDecimalTreeNode> queue = new LinkedList<>();
        queue.offer(tree.root);

        while (!queue.isEmpty()) {
            BPlusDecimalTreeNode current = queue.poll();
            if (current != tree.root) {
                // pointer
                size += 64*2;
            }
            int keySize  = current.keys.size();
            for (int i =0; i < keySize; i++) {
                size = size + current.keys.get(i).key.length;
                if (current.keys.get(i).flagFalseNode!=null) {
                    // 加上2个双向pointer + skipList中的数据和双向指针
                    size = size + 1;
                }
                if (current.keys.get(i).flagTrueNode!=null) {
                    int xorSize = current.keys.get(i).flagTrueNode.decimalNodes.size();
                    for (int j = 0; j < xorSize; j++) {
                        size += current.keys.get(i).flagTrueNode.decimalNodes.get(j).key.length;
                    }
                }
            }

            if (current.children != null) {
                for (BPlusDecimalTreeNode child : current.children) {
                    if (child != null) {
                        queue.offer(child);
                    }
                }
            }
        }
        size += tree.skipList.traverseSize();
        return size;
    }


    public List<SkiplistNode> getAllLeaf(BPlusDecimalTree tree){
        List<SkiplistNode> res = new ArrayList<>();
        if (tree == null) {
            System.out.println("The tree is empty.");
            return null;
        }

        Queue<BPlusDecimalTreeNode> queue = new LinkedList<>();
        queue.offer(tree.root);

        while (!queue.isEmpty()) {
            BPlusDecimalTreeNode current = queue.poll();
            int keySize  = current.keys.size();
            for (int i =0; i < keySize; i++) {
                if (current.isLeaf) {
                    if (current.keys.get(i).flagFalseNode!=null) {
                        res.addAll(current.keys.get(i).flagFalseNode.tsNodeList);
                    }
                    if (current.keys.get(i).flagTrueNode!=null) {
                        for (int j = 0; j< current.keys.get(i).flagTrueNode.decimalNodes.size(); j++) {
                            res.addAll(current.keys.get(i).flagTrueNode.decimalNodes.get(j).tsNodeList);
                        }
                    }
                }

            }

            if (current.children != null) {
                for (BPlusDecimalTreeNode child : current.children) {
                    if (child != null) {
                        queue.offer(child);
                    }
                }
            }
        }
        return res;
    }


    public static void main(String[] args) {
        // 创建一个B+树，假设阶数为3

//        BPlusDecimalTree bPlusTree = new BPlusDecimalTree(3);
//        // 插入一些关键字
//        int[] keysToInsert = {10, 5, 20, 6, 12, 30, 7, 17};
//        for (int key : keysToInsert) {
//            bPlusTree.insert(key);
//        }
//
//        // 查找关键字并输出结果
//        int[] keysToSearch = {5, 12, 7, 25};
//        for (int key : keysToSearch) {
//            boolean found = bPlusTree.search(key);
//            System.out.println("Key " + key + " found: " + found);
//        }

        BPlusDecimalTree bPlusTree = new BPlusDecimalTree(3, new SkipList());
        // 根据位数创建一个树索引
        bPlusTree = bPlusTree.buildTree(bPlusTree, new byte[]{1,0}, new byte[]{1,0}, new byte[]{1,0});
//        bPlusTree.insert(1);
//        bPlusTree.insert(1);
//        bPlusTree.insert(1);
//        bPlusTree.insert(2);
//        bPlusTree.insert(2);
//        KeyNode node = bPlusTree.searchKeyNode(23);
//        System.out.println("node" + node);




    }

}

