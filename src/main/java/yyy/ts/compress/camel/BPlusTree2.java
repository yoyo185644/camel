package yyy.ts.compress.camel;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static yyy.ts.compress.camel.CamelUtils.integerBinaryToInt;


class IntKeyNode2{
    byte[] key;

    ArrayList<SkiplistNode> tsNodesList;

    public IntKeyNode2(byte[] key, ArrayList<SkiplistNode>  tsNodesList) {
        this.key = key;
        this.tsNodesList = tsNodesList;
    }

    public IntKeyNode2(byte[] key) {
        this.key = key;
    }

}

class BPlusTreeNode2 {
    boolean isLeaf;
    List<IntKeyNode2> keys;
    List<BPlusTreeNode2> children;


    public BPlusTreeNode2(boolean isLeaf) {
        this.isLeaf = isLeaf;
        this.keys = new ArrayList<>();
        this.children = new ArrayList<>();
    }
}
public class BPlusTree2{
    private BPlusTreeNode2 root;

    private int order;

    private SkipList skipList;

    // 用一个指针永远指向下一个值
    public static KeyNode previousTSNode = null;



    public BPlusTree2(int order, SkipList skipList) {
        this.root = new BPlusTreeNode2(true);
        this.order = order;
        this.skipList = skipList;
    }

    public void insert(byte[] key, byte[] compressInt,byte[] decimalCount, byte[] xorFlag, byte[] xorVal, byte[] compressDecimal, double timestamp) {
        // 检查当前树的根节点是否已经达到了其最大容量 如果已经达到最大容量，需要进行分裂操作
        if (root.keys.size() == (2 * order) - 1) {
            BPlusTreeNode2 newRoot = new BPlusTreeNode2(false);
            newRoot.children.add(root);
            splitChild(newRoot, 0);
            root = newRoot;
        }
        // 查询是否存在整数部分，如果存在就插入到小数部分的树中
        IntKeyNode2 intKeyNode = searchKeyNode(root, integerBinaryToInt(compressInt));
        ArrayList<SkiplistNode> tsNodesList;
        if (intKeyNode!=null) {
            // ***** 对于值查询只指向原始数据
            tsNodesList = intKeyNode.tsNodesList;
            if (tsNodesList != null) {
                IntKeyNode2 intKeyNode2 = new IntKeyNode2(compressInt);
                SkiplistNode skiplistNode = skipList.add(timestamp);
                skiplistNode.intKeyNode = intKeyNode2;
                tsNodesList.add(skiplistNode);
            } else {
                ArrayList<SkiplistNode> tsNodeList = new ArrayList<>();
                IntKeyNode2 intKeyNode2 = new IntKeyNode2(compressInt);
                SkiplistNode tsNode = skipList.add(timestamp);
                tsNode.intKeyNode = intKeyNode2;
                tsNodeList.add(tsNode);

            }
            intKeyNode.tsNodesList  = tsNodesList;

            return;
        }
        insertNonFull(root,  key, compressInt, decimalCount, xorFlag, xorVal, compressDecimal, timestamp, skipList);
    }

    private void insertNonFull(BPlusTreeNode2 node, byte[] key, byte[] compressInt,
                               byte[] decimalCount, byte[] xorFlag, byte[] xorVal, byte[] compressDecimal, double timestamp, SkipList skipList) {
        int i = node.keys.size() - 1;

        // 对于第一次出现的整数，新建list插入
        while (i >= 0 && integerBinaryToInt(key) < integerBinaryToInt(node.keys.get(i).key)) {
            i--;
        }
        if (node.isLeaf) {
            ArrayList<SkiplistNode> tsNodeList = new ArrayList<>();
            IntKeyNode2 intKeyNode2 = new IntKeyNode2(compressInt);
            SkiplistNode tsNode = skipList.add(timestamp);
            tsNode.intKeyNode = intKeyNode2;
            tsNodeList.add(tsNode);
            intKeyNode2.tsNodesList = tsNodeList;
            node.keys.add(i + 1, intKeyNode2);
        } else {
            i++;

            try {
                if (node.children.get(i).keys.size() == (2 * order) - 1) {
                    splitChild(node, i);
                    if (integerBinaryToInt(key) > integerBinaryToInt(node.keys.get(i).key)) {
                        i++;
                    }
                }
                insertNonFull(node.children.get(i), key, compressInt, decimalCount, xorFlag, xorVal, compressDecimal, timestamp, skipList);
            }catch (Exception e) {
                System.out.println(e.getMessage());
            }

        }
    }


    private void splitChild(BPlusTreeNode2 parent, int index) {
        BPlusTreeNode2 child = parent.children.get(index);
        BPlusTreeNode2 newChild = new BPlusTreeNode2(child.isLeaf);

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

        // 递归将非叶子节点的值清空

    }

    public boolean search(byte[] key) {
        return searchKey(root, key);
    }

    private boolean searchKey(BPlusTreeNode2 node, byte[] key) {
        int i = 0;
        while (i < node.keys.size() && integerBinaryToInt(key) > integerBinaryToInt(node.keys.get(i).key)) {
            i++;
        }

        if (i < node.keys.size() && key == node.keys.get(i).key) {
            // Key found
            return true;
        }

        if (node.isLeaf) {
            // Key not found
            return false;
        }

        // Recur to the next level
        return searchKey(node.children.get(i), key);
    }

    public IntKeyNode2 searchKeyNode(BPlusTreeNode2 node, int key) {
        int i = 0;
        while (i < node.keys.size() && key > integerBinaryToInt(node.keys.get(i).key)) {
            i++;
        }

        if (node.isLeaf) {
            // Key not found
            List<IntKeyNode2> keyNodes = node.keys;
            IntKeyNode2 keyNode = binarySearchByKey(keyNodes, key);
            return keyNode;
        }

        if (i >= node.children.size()) {
            return searchKeyNode(node.children.get(node.children.size()-1), key);
        }

        // Recur to the next level
        return searchKeyNode(node.children.get(i), key);
    }

    private static IntKeyNode2 binarySearchByKey(List<IntKeyNode2> keys, Integer targetKey) {
        int low = 0;
        int high = keys.size() - 1;

        while (low <= high) {
            int mid = (low + high) / 2;

            int compareResult = targetKey.compareTo(integerBinaryToInt(keys.get(mid).key));

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

    // 层次遍历
    public long levelOrderTraversal(BPlusTree2 tree){
        int size = 0;
        if (tree == null) {
            System.out.println("The tree is empty.");
            return size;
        }

        Queue<BPlusTreeNode2> queue = new LinkedList<>();
        queue.offer(tree.root);

        while (!queue.isEmpty()) {
            BPlusTreeNode2 current = queue.poll();
            int keySize  = current.keys.size();
            for (int i =0; i < keySize; i++) {
                size = size + current.keys.get(i).key.length;
            }

            if (current.children != null) {
                for (BPlusTreeNode2 child : current.children) {
                    if (child != null) {
                        queue.offer(child);
                    }
                }
            }
        }
        return size;
    }

    public long levelOrderTraversalWithPointer(BPlusTree2 tree){
        int size = 0;
        if (tree == null) {
            System.out.println("The tree is empty.");
            return size;
        }

        Queue<BPlusTreeNode2> queue = new LinkedList<>();
        queue.offer(tree.root);

        while (!queue.isEmpty()) {
            BPlusTreeNode2 current = queue.poll();
            if (current != tree.root) {
                // pointer
                size += 64*2;
            }
            int keySize  = current.keys.size();
            for (int i =0; i < keySize; i++) {
                size = size + current.keys.get(i).key.length;
                // 整数部分单独计算一个双向指针
                if (current.isLeaf) {
                    size += current.keys.get(i).tsNodesList.size()*64;
                }
            }

            if (current.children != null) {
                for (BPlusTreeNode2 child : current.children) {
                    if (child != null) {
                        queue.offer(child);
                    }
                }
            }
        }
        return size;
    }

    public BPlusTreeNode2 getRoot(BPlusTree2 tree) {
        return tree.root;
    }


    public static void main(String[] args) {
        // 创建一个B+树，假设阶数为3
        BPlusTree2 bPlusTree = new BPlusTree2(2, new SkipList());
        BPlusDecimalTree deciamlPlusTree = new BPlusDecimalTree(3, new SkipList());
        // 根据位数创建一个小数位数的索引
//        deciamlPlusTree = deciamlPlusTree.buildTree(deciamlPlusTree, 2);
        // 插入一些关键字
        byte[][] keysToInsert = {new byte[]{1,1,1,1}, new byte[]{1,0,0,0,0},  new byte[]{1,0,0,1,0}, new byte[]{1,0,0,0,0},  new byte[]{1,0,0,0,0}, new byte[]{1,1,1,1}, new byte[]{1,1,1,1}, new byte[]{1,1,0,1}};
        byte[][] xorFlag = {new byte[]{1,0,1},new byte[]{10},new byte[]{10},new byte[]{10},new byte[]{10},new byte[]{10},new byte[]{10}};
        byte[][] xorvalue = {new byte[]{10},new byte[]{10},new byte[]{10},new byte[]{10},new byte[]{10},new byte[]{10},new byte[]{10}};
        byte[][] compressDecimal = {new byte[]{10},new byte[]{10},new byte[]{10},new byte[]{10},new byte[]{10},new byte[]{10},new byte[]{10}};

        long[] timestamp = {1,2,3,4,5,6,7,8,9,10,11,12,13,14};
        for (int i=0; i< keysToInsert.length; i++) {
            bPlusTree.insert(keysToInsert[i], keysToInsert[i], new byte[1], xorFlag[i], xorvalue[i], compressDecimal[i], timestamp[i]);
        }
        // 查找关键字并输出结果
        byte[][] keysToSearch = {new byte[]{10},new byte[]{10},new byte[]{10},new byte[]{10},new byte[]{10},new byte[]{10},new byte[]{10}};

        System.out.println(bPlusTree.root.keys);
        for (byte[] key : keysToSearch) {
            boolean found = bPlusTree.search(key);
            System.out.println("Key " + key + " found: " + found);
        }
    }
}

