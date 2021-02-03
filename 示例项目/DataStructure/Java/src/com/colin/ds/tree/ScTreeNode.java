package com.colin.ds.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * 二叉树的二叉链表结点结构定义，如果再增加一个指向双亲的结点则称为三叉链表
 * 
 * @author 侠客
 * @added 2020年4月27日 上午5:32:48
 * @version 1.0.0
 */
public class ScTreeNode {
    /**
     * 结点数据
     */
    private Object data;

    /**
     * 左孩子结点
     */
    private ScTreeNode lChild;

    /**
     * 右孩子结点
     */
    private ScTreeNode rChild;

    public Object getData() {
      return data;
    }

    public void setData(Object data) {
      this.data = data;
    }

    public ScTreeNode getlChild() {
      return lChild;
    }

    public void setlChild(ScTreeNode lChild) {
      this.lChild = lChild;
    }

    public ScTreeNode getrChild() {
      return rChild;
    }

    public void setrChild(ScTreeNode rChild) {
      this.rChild = rChild;
    }
}

/**
 * 双亲表示法
 * 查找双亲只要O(1)，查找孩子需要遍历整个结构
 * @author  侠客
 * @added   2020年5月8日 上午5:56:54
 * @version 1.0.0
 */
class ScTreeNode1 {
    private Object data;
    private ScTreeNode1 parent;

    ScTreeNode1(Object data, ScTreeNode1 parent) {
        this.data = data;
        this.parent = parent;
    }

    /**
     * @return 结点数据
     */
    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    /**
     * @return 双亲节点
     */
    public ScTreeNode1 getParent() {
        return parent;
    }

    public void setParent(ScTreeNode1 parent) {
        this.parent = parent;
    }
}

/**
 * 增加左孩子和右兄弟
 */
class ScTreeNode1S1 {
    private Object data;
    private ScTreeNode1S1 parent;
    private ScTreeNode1S1 firstChild;
    private ScTreeNode1S1 rightSib;

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public ScTreeNode1S1 getParent() {
        return parent;
    }

    public void setParent(ScTreeNode1S1 parent) {
        this.parent = parent;
    }

    /**
     * @return 返回左孩子
     */
    public ScTreeNode1S1 getFirstChild() {
        return firstChild;
    }

    public void setFirstChild(ScTreeNode1S1 firstChild) {
        this.firstChild = firstChild;
    }

    /**
     * @return 返回右兄弟
     */
    public ScTreeNode1S1 getRightSib() {
        return rightSib;
    }

    public void setRightSib(ScTreeNode1S1 rightSib) {
        this.rightSib = rightSib;
    }
}
