package com.colin.ds.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试类
 * @author 侠客
 */
public class Test {
    /**
     * @param args 输入参数
     */
    public static void main(String[] args) {
        test1();
    }

    /**
     * 双亲表示法示例
     * <a Reef="http://viyitech.cn/public/images/java_tree.PNG">测试树示例</a>
     */
    private static void test1() {
        List<ScTreeNode1> tree = new ArrayList<>();
        ScTreeNode1 node1 = new ScTreeNode1("A", null);
        ScTreeNode1 node2 = new ScTreeNode1("B", node1);
        ScTreeNode1 node3 = new ScTreeNode1("C", node1);
        ScTreeNode1 node4 = new ScTreeNode1("A", node2);
        ScTreeNode1 node5 = new ScTreeNode1("A", node3);
        ScTreeNode1 node6 = new ScTreeNode1("A", node3);
        ScTreeNode1 node7 = new ScTreeNode1("A", node4);
        ScTreeNode1 node8 = new ScTreeNode1("A", node4);
        ScTreeNode1 node9 = new ScTreeNode1("A", node4);
        ScTreeNode1 node10 = new ScTreeNode1("A", node5);
        tree.add(node1);
        tree.add(node2);
        tree.add(node3);
        tree.add(node4);
        tree.add(node5);
        tree.add(node6);
        tree.add(node7);
        tree.add(node8);
        tree.add(node9);
        tree.add(node10);
    }

    private void test2() {

    }
}
