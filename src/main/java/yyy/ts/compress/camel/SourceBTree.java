package yyy.ts.compress.camel;//package yyy.ts.compress.camel;

import org.apache.thrift.protocol.TList;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


class SBPNode {
    Double key;

    ArrayList<SkiplistNode> tsNodesList;

    public SBPNode(double key, ArrayList<SkiplistNode> tsNodesList) {
        this.key = key;
        this.tsNodesList = tsNodesList;
    }

    public SBPNode(double key) {
        this.key = key;
    }
}


class SourceTreeNode {
        boolean isLeaf;
        List<SBPNode> keys;
        List<SourceTreeNode> children;


        public SourceTreeNode(boolean isLeaf) {
            this.isLeaf = isLeaf;
            this.keys = new ArrayList<>();
            this.children = new ArrayList<>();
        }
}

public class SourceBTree {
        private SourceTreeNode root;

        private int order;


        private SkipList skipList;
        // 用一个指针永远指向下一个值
//        public static KeyNode previousTSNode = null;


        public SourceBTree(int order, SkipList skipList) {
            this.root = new SourceTreeNode(true);
            this.order = order;
            this.skipList = skipList;
        }

        public void insert(double value, long timestamp) {
            // 检查当前树的根节点是否已经达到了其最大容量 如果已经达到最大容量，需要进行分裂操作
            if (root.keys.size() == (2 * order) - 1) {
                SourceTreeNode newRoot = new SourceTreeNode(false);
                newRoot.children.add(root);
                splitChild(newRoot, 0);
                root = newRoot;
            }
            insertNonFull(root, value, timestamp, skipList);
        }

        private void insertNonFull(SourceTreeNode node, double value,  long timestamp, SkipList skipList) {
            int i = node.keys.size() - 1;

            // 对于第一次出现的整数，新建list插入
            while (i >= 0 &&value < node.keys.get(i).key) {
                i--;
            }
            if (node.isLeaf) {
                ArrayList<SkiplistNode> tsNodeList = new ArrayList<>();
                SBPNode sbpNode = new SBPNode(value);
                SkiplistNode tsNode = skipList.add(value);
                tsNode.sbpNode = sbpNode;
                tsNodeList.add(tsNode);
                sbpNode.tsNodesList = tsNodeList;
                node.keys.add(i + 1, sbpNode);
            } else {
                i++;

                try {
                    if (node.children.get(i).keys.size() == (2 * order) - 1) {
                        splitChild(node, i);
                        if (value > node.keys.get(i).key) {
                            i++;
                        }
                    }
                    insertNonFull(node.children.get(i), value, timestamp, skipList);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

            }
        }


        private void splitChild(SourceTreeNode parent, int index) {
            SourceTreeNode child = parent.children.get(index);
            SourceTreeNode newChild = new SourceTreeNode(child.isLeaf);

            parent.keys.add(index, child.keys.get(order - 1));

            for (int j = 0; j < order - 1; j++) {
                newChild.keys.add(child.keys.remove(order));
            }

            if (!child.isLeaf) {
                for (int j = 0; j < order; j++) {
                    if (order >= child.children.size()) {
                        break;
                    }
                    newChild.children.add(child.children.remove(order));
                }
            }

            parent.children.add(index + 1, newChild);

            // 递归将非叶子节点的值清空

        }

        public boolean search(double value) {
            return searchKey(root, value);
        }

        private boolean searchKey(SourceTreeNode node, double value) {
            int i = 0;
            while (i < node.keys.size() && value > node.keys.get(i).key) {
                i++;
            }

            if (i < node.keys.size() && value == node.keys.get(i).key) {
                // Key found
                return true;
            }

            if (node.isLeaf) {
                // Key not found
                return false;
            }

            // Recur to the next level
            return search(value);
        }

        public SBPNode searchKeyNode(SourceTreeNode node, double key) {
            int i = 0;
            while (i < node.keys.size() && key > node.keys.get(i).key) {
                i++;
            }

            if (node.isLeaf) {
                // Key not found
                List<SBPNode> keyNodes = node.keys;
                SBPNode keyNode = binarySearchByKey(keyNodes, key);
                return keyNode;
            }

            if (i >= node.children.size()) {
                return searchKeyNode(node.children.get(node.children.size() - 1), key);
            }

            // Recur to the next level
            return searchKeyNode(node.children.get(i), key);
        }

        private  SBPNode binarySearchByKey(List<SBPNode> keys, double targetKey) {
            int low = 0;
            int high = keys.size() - 1;

            while (low <= high) {
                int mid = (low + high) / 2;

                if (targetKey  == keys.get(mid).key) {
                    return keys.get(mid); // 找到关键字对应的值
                } else if (targetKey < keys.get(mid).key) {
                    high = mid - 1;
                } else {
                    low = mid + 1;
                }
            }

            return null; // 未找到关键字对应的值
        }

        // 层次遍历
        public long levelOrderTraversalWithPointer(SourceBTree tree) {
            int size = 0;
            if (tree == null) {
                System.out.println("The tree is empty.");
                return size;
            }

            Queue<SourceTreeNode> queue = new LinkedList<>();
            queue.offer(tree.root);

            while (!queue.isEmpty()) {
                SourceTreeNode current = queue.poll();
                if (current != tree.root) {
                    // pointer
                    size += 64;
                }
                int keySize = current.keys.size();
                for (int i = 0; i < keySize; i++) {
                    size = size + 64*2;
                }

                if (current.isLeaf) {
                    for (int i = 0; i < keySize; i++) {
                        ArrayList<SkiplistNode> tsNodes = current.keys.get(i).tsNodesList;
                        // timestamp占64bit + pointer*4 (指向数据的双向指针和指向跳表的双向指针) + 跳表结构还有一个level
                        size = size + tsNodes.size() * 64 * 5 + 32;
                    }
                }
                if (current.children != null) {
                    for (SourceTreeNode child : current.children) {
                        if (child != null) {
                            queue.offer(child);
                        }
                    }
                }
            }
            size += tree.skipList.traverseSize();
            return size;
        }

    public ArrayList<SkiplistNode> levelOrderTraversal(SourceBTree tree) {
        int size = 0;
        ArrayList<SkiplistNode> res = new ArrayList<>();
        if (tree == null) {
            System.out.println("The tree is empty.");
            return null;
        }

        Queue<SourceTreeNode> queue = new LinkedList<>();
        queue.offer(tree.root);

        while (!queue.isEmpty()) {
            SourceTreeNode current = queue.poll();
            int keySize = current.keys.size();

            if (current.isLeaf) {
                for (int i = 0; i < keySize; i++) {
                    ArrayList<SkiplistNode> tsNodes = current.keys.get(i).tsNodesList;
                    res.addAll(tsNodes);
                }
            }
            if (current.children != null) {
                for (SourceTreeNode child : current.children) {
                    if (child != null) {
                        queue.offer(child);
                    }
                }
            }
        }
        return res;
    }

        public SourceTreeNode getRoot(SourceBTree tree) {
            return tree.root;
        }
    }
