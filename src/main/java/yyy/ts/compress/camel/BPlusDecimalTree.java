package yyy.ts.compress.camel;


import java.nio.ByteBuffer;
import java.util.*;

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
    ArrayList<TSNode> tsNodeList;
    public FlagFalseNode(byte flag, ArrayList<TSNode> tsNodeList){
        this.flag = flag;
        this.tsNodeList = tsNodeList;
    }

}
// 最后一个节点
class DecimalNode{

    byte[] key;
    int count;

    ArrayList<TSNode> tsNodeList;

    public DecimalNode(byte[] key, int count, ArrayList<TSNode> tsNodeList){
        this.key = key;
        this.count = count;
        this.tsNodeList = tsNodeList;

    }
}

class TSNode {
    byte[] valueInt;
    byte[] valueDecimal;
    long timeStamp;
    TSNode nextTS;
    TSNode beforeTS;

    public TSNode(byte[] valueInt, byte[] valueDecimal,  long timeStamp) {
        this.valueInt = valueInt;
        this.valueDecimal = valueDecimal;
        this.timeStamp = timeStamp;
        this.nextTS = null;
    }
}

class KeyNode{
    byte[] key;
    FlagFalseNode flagFalseNode;
    FlagTrueNode flagTrueNode;

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

    private static boolean buildFlag = false;

    private static TSNode previousTSNode = null;

    // 按照寻找到的m的值进行保存
    public final static int[] mValueBits = {3, 5, 7, 10, 15};
    public BPlusDecimalTree(int order) {
        this.root = new BPlusDecimalTreeNode(true);
        this.order = order;
    }

    public void insert(byte flag, byte[] xorVal, byte[] compressInt, byte[] compressDecimal, long timestamp) {
        // 寻找小数部分
        if (buildFlag) {
            KeyNode node = searchKeyNode(binaryToInt(compressDecimal));
            if (node != null) {
                TSNode tsNode = new TSNode(compressInt, compressDecimal, timestamp);
                if (previousTSNode != null) {
                    previousTSNode.nextTS = tsNode;
                    tsNode.beforeTS = previousTSNode;
                }
                    previousTSNode = tsNode;
                // 如果flag=1
                if (flag == 1) {
                    // 查询flagTrueNode是否存在
                    if (node.flagTrueNode != null) {
                        LinkedList<DecimalNode> decimalNodes = node.flagTrueNode.decimalNodes;
                        DecimalNode decimalNode= searchDecimalNode(decimalNodes, xorVal);
                        if (decimalNode != null) {
                            decimalNode.count ++;
                            decimalNode.tsNodeList.add(tsNode);
                        } else {
                            // 说明第一次指定异或完的值
                            ArrayList<TSNode> tsNodeList = new ArrayList<>();
                            tsNodeList.add(tsNode);
                            DecimalNode decimalNodeNew = new DecimalNode(xorVal, 1, tsNodeList);
                            LinkedList<DecimalNode> decimalNodesList = new LinkedList<>();
                            decimalNodesList.add(decimalNodeNew);
                        }
                    } else {
                        // 构建一个TSNode
                        ArrayList<TSNode> tsNodeList = new ArrayList<>();
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
                        ArrayList<TSNode> tsNodeList = node.flagFalseNode.tsNodeList;
                        tsNodeList.add(tsNode);
                    }else {
                        ArrayList<TSNode> tsNodeList = new ArrayList<>();
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

    public BPlusDecimalTree buildTree(BPlusDecimalTree bPlusTree, int decimalCount) {
        int[] range = new int[]{4, 25, 125, 625};
        for (int key = 1; key < range[decimalCount-1]; key ++) {

//      // 获取是否通过XOR的flag
        byte[] compressDecimal = compressDecimal(decimalCount, key);
        byte flag = compressDecimal[2];
        byte[] xorVal = null;
        byte[] decimalVal = null;
//
        if (flag == 1) { // 通过XOR之后的centerbits和保存的值
            xorVal = Arrays.copyOfRange(compressDecimal, 3, (int) (3 + decimalCount));
            decimalVal = Arrays.copyOfRange(compressDecimal, (int) (3 + decimalCount), compressDecimal.length);
        } else { // 没有通过XOR的保存的值
            decimalVal = Arrays.copyOfRange(compressDecimal, 3, compressDecimal.length);
        }
        // todo 参数compressDecimal修改成 compressInt
        bPlusTree.insert(flag, xorVal, null, decimalVal, 1l);
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

    public static int binaryToInt(byte[] binaryArray) {
        int result = 0;
        for (int i = 0; i < binaryArray.length; i++) {
            result = (result << 1) | binaryArray[i];
        }
        return result;
    }


    //解压小数部分
    public static int decompressDecimal(byte[] decimalCompress) {
        return binaryToInt(decimalCompress);

    }

    //压缩小数部分
    public static byte[] compressDecimal(int decimalCount, int key) {
        if (decimalCount <= 1) { // 如果是1 直接往后读decimal_count+1位
            return convertToBinary(key, decimalCount+1);
        } else if (decimalCount ==2) {
            if (key < 8) {
                return convertToBinary(key, 3);
            }else {
                return convertToBinary(key, 5);
            }

        } else if (decimalCount == 3) {
            if (key < 4) {
                return convertToBinary(key, 2);
            }else if (key < 8){
                return convertToBinary(key, 3);
            }else if (key < 16) {
                return convertToBinary(key, 4);
            }else {
                return convertToBinary(key, mValueBits[decimalCount-1]);
            }

        } else {
            if (key < 16) {
                return convertToBinary(key, 4);
            }else if (key < 64){
                return convertToBinary(key, 6);
            }else if (key < 256) {
                return convertToBinary(key, 8);
            }else {
                return convertToBinary(key, mValueBits[decimalCount-1]);
            }

        }
    }

    private static byte[] convertToBinary(int number, int desiredLength) {
        // 将整数转换为二进制字符串
        String binaryString = Integer.toBinaryString(number);
        // 获取当前二进制字符串的长度
        int currentLength = binaryString.length();

        // 如果当前长度小于指定长度，则在左侧补零，直到达到指定长度
        if (currentLength < desiredLength) {
            StringBuilder paddedBinary = new StringBuilder();
            for (int i = 0; i < desiredLength - currentLength; i++) {
                paddedBinary.append('0');
            }
            paddedBinary.append(binaryString);
            return binaryStringToByteArray(paddedBinary.toString());
        }

        // 如果当前长度大于等于指定长度，则直接返回当前二进制字符串
        return binaryStringToByteArray(binaryString);
    }

    private static byte[] binaryStringToByteArray(String binaryString) {
        int numBytes = (binaryString.length() + 7) / 8; // 计算所需的字节数
        ByteBuffer buffer = ByteBuffer.allocate(numBytes);
        for (int i = 0; i < numBytes; i++) {
            int startIndex = i * 8;
            int endIndex = Math.min(startIndex + 8, binaryString.length());
            String byteString = binaryString.substring(startIndex, endIndex);
            buffer.put((byte) Integer.parseInt(byteString, 2));
        }

        return buffer.array();
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

//        BPlusDecimalTree bPlusTree = new BPlusDecimalTree(3);
//        // 根据位数创建一个树索引
//        bPlusTree = bPlusTree.buildTree(bPlusTree, 2);
//        bPlusTree.insert(1);
//        bPlusTree.insert(1);
//        bPlusTree.insert(1);
//        bPlusTree.insert(2);
//        bPlusTree.insert(2);
//        KeyNode node = bPlusTree.searchKeyNode(23);
//        System.out.println("node" + node);




    }

}

