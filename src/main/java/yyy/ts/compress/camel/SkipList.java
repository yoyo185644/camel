package yyy.ts.compress.camel;

import java.util.Random;
import java.util.Stack;

public class SkipList {

    SkiplistNode headNode;//头节点，入口
    int highLevel;//层数
    Random random;// 用于投掷硬币
    final int MAX_LEVEL = 32;//最大的层
    public SkipList(){
        random=new Random();
        headNode=new SkiplistNode(Double.MIN_VALUE);
        highLevel=0;
    }
    public SkiplistNode search(double timeStamp) {
        SkiplistNode team=headNode;
        while (team!=null) {
            if(team.timeStamp==timeStamp)
            {
                return  team;
            }
            else if(team.right==null)//右侧没有了，只能下降
            {
                team=team.down;
            }
            else if(team.right.timeStamp>timeStamp)//需要下降去寻找
            {
                team=team.down;
            }
            else //右侧比较小向右
            {
                team=team.right;
            }
        }
        return null;
    }

    public void delete(int timeStamp)//删除不需要考虑层数
    {
        SkiplistNode team=headNode;
        while (team!=null) {
            if (team.right == null) {//右侧没有了，说明这一层找到，没有只能下降
                team=team.down;
            }
            else if(team.right.timeStamp==timeStamp)//找到节点，右侧即为待删除节点
            {
                team.right=team.right.right;//删除右侧节点
                team=team.down;//向下继续查找删除
            }
            else if(team.right.timeStamp>timeStamp)//右侧已经不可能了，向下
            {
                team=team.down;
            }
            else { //节点还在右侧
                team=team.right;
            }
        }
    }
    public SkiplistNode add(Double value)
    {
        SkiplistNode node = new SkiplistNode(value);
        double timeStamp=node.timeStamp;
        SkiplistNode findNode=search(timeStamp);
        if(findNode!=null)//如果存在这个key的节点
        {
            findNode.timeStamp=node.timeStamp;
            return findNode;
        }

        Stack<SkiplistNode>stack=new Stack<SkiplistNode>();//存储向下的节点，这些节点可能在右侧插入节点
        SkiplistNode team=headNode;//查找待插入的节点   找到最底层的哪个节点。
        while (team!=null) {//进行查找操作
            if(team.right==null)//右侧没有了，只能下降
            {
                stack.add(team);//将曾经向下的节点记录一下
                team=team.down;
            }
            else if(team.right.timeStamp>timeStamp)//需要下降去寻找
            {
                stack.add(team);//将曾经向下的节点记录一下
                team=team.down;
            }
            else //向右
            {
                team=team.right;
            }
        }

        int level=1;//当前层数，从第一层添加(第一层必须添加，先添加再判断)
        SkiplistNode downNode=null;//保持前驱节点(即down的指向，初始为null)
        while (!stack.isEmpty()) {
            //在该层插入node
            team=stack.pop();//抛出待插入的左侧节点
            SkiplistNode nodeTeam=new SkiplistNode(node.timeStamp);//节点需要重新创建
            nodeTeam.down=downNode;//处理竖方向
            downNode=nodeTeam;//标记新的节点下次使用
            if(team.right==null) {//右侧为null 说明插入在末尾
                team.right=nodeTeam;
            }
            //水平方向处理
            else {//右侧还有节点，插入在两者之间
                nodeTeam.right=team.right;
                team.right=nodeTeam;
            }
            //考虑是否需要向上
            if(level>MAX_LEVEL)//已经到达最高级的节点啦
                break;
            double num=random.nextDouble();//[0-1]随机数
            if(num>0.5)//运气不好结束
                break;
            level++;
            if(level>highLevel)//比当前最大高度要高但是依然在允许范围内 需要改变head节点
            {
                highLevel=level;
                //需要创建一个新的节点
                SkiplistNode highHeadNode=new SkiplistNode(Double.MIN_VALUE);
                highHeadNode.down=headNode;
                headNode=highHeadNode;//改变head
                stack.add(headNode);//下次抛出head
            }
        }
        return node;
    }
    public void printList() {
        SkiplistNode teamNode=headNode;
        int index=1;
        SkiplistNode last=teamNode;
        while (last.down!=null){
            last=last.down;
        }
        while (teamNode!=null) {
            SkiplistNode enumNode=teamNode.right;
            SkiplistNode enumLast=last.right;
            System.out.printf("%-8s","head->");
            while (enumLast!=null&&enumNode!=null) {
                if(enumLast.timeStamp==enumNode.timeStamp)
                {
                    System.out.printf("%-5s",enumLast.timeStamp+"->");
                    enumLast=enumLast.right;
                    enumNode=enumNode.right;
                }
                else{
                    enumLast=enumLast.right;
                    System.out.printf("%-5s","");
                }

            }
            teamNode=teamNode.down;
            index++;
            System.out.println();
        }
    }

    public void traverse() {
        SkiplistNode current = headNode; // 从头节点开始
        int i = 1;
        while (current != null) {
            SkiplistNode node = current.right; // 水平移动到下一个节点
            System.out.print("Level " + current.level + ": ");
            if (i == highLevel) {
                System.out.println("point to source data");
                while (node != null) {
                    System.out.print(node.timeStamp + " -> ");
                    // 指向原数据的指针
                    node = node.right; // 继续水平移动
                }
            }
            while (node != null) {
                System.out.print(node.timeStamp + " -> ");
                node = node.right; // 继续水平移动
            }
            SkiplistNode temp = current;
            if (temp.down == null) {
                while (node != null) {
                    // 指向原数据的指针
                    System.out.print(node.timeStamp + " -> ");
                    node = node.right; // 继续水平移动
                }
            }
            System.out.println("null"); // 表示当前层结束
            current = current.down; // 向下移动到下一层
            i++;
        }
    }

    public long traverseSize() {
        long size = 0;
        SkiplistNode current = headNode; // 从头节点开始
        int i = 1;
        while (current != null) {
            SkiplistNode node = current.right; // 水平移动到下一个节点
            while (node != null) {
                // 指向压缩后的值 双向指针
                if (i == highLevel) {
                    size += 64*2;
                }
                size += 64*2;
                node = node.right; // 继续水平移动
            }
            size += 64;
            current = current.down; // 向下移动到下一层
        }
        return size;
    }



    public static void main(String[] args) {
        SkipList list=new SkipList();
        for (int i=0; i< 1000; i++) {
            list.add((double)i);
        }
        list.printList();
        list.traverse();
        double searchValue = 19.0;
        if (list.search(searchValue)!=null) {
            System.out.println(searchValue + " found in the skip list.");
        } else {
            System.out.println(searchValue + " not found in the skip list.");
        }
    }
}
