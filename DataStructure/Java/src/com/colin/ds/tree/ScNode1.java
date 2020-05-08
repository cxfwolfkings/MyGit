package com.colin.ds.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * 双亲表示法
 * 查找双亲只要O(1)，查找孩子需要遍历整个结构
 * @author  侠客
 * @added   2020年5月8日 上午5:56:54
 * @version 1.0.0
 */
public class ScNode1 {
  private Object data;
  private ScNode1 parent;
  
  private ScNode1(Object data, ScNode1 parent) {
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
  public ScNode1 getParent() {
    return parent;
  }
  
  public void setParent(ScNode1 parent) {
    this.parent = parent;
  } 
  
  /**
   * <a Reef="http://viyitech.cn/public/images/java_tree.PNG">测试树示例</a>
   * @param args 输入参数
   */
  public static void main(String[] args) {
    List<ScNode1> tree = new ArrayList<>();
    ScNode1 nodeA = new ScNode1("A", null);
    ScNode1 nodeB = new ScNode1("B", nodeA);
    ScNode1 nodeC = new ScNode1("C", nodeA);
    ScNode1 nodeD = new ScNode1("A", nodeB);
    ScNode1 nodeE = new ScNode1("A", nodeC);
    ScNode1 nodeF = new ScNode1("A", nodeC);
    ScNode1 nodeG = new ScNode1("A", nodeD);
    ScNode1 nodeH = new ScNode1("A", nodeD);
    ScNode1 nodeI = new ScNode1("A", nodeD);
    ScNode1 nodeJ = new ScNode1("A", nodeE);
    tree.add(nodeA);
    tree.add(nodeB);
    tree.add(nodeC);
    tree.add(nodeD);
    tree.add(nodeE);
    tree.add(nodeF);
    tree.add(nodeG);
    tree.add(nodeH);
    tree.add(nodeI);
    tree.add(nodeJ);
  }
}

/**
 * 增加左孩子和右兄弟
 */
class ScNode1s {
  private Object data;
  private ScNode1s parent;
  private ScNode1s firstChild;
  private ScNode1s rightSib;

  public Object getData() {
    return data;
  }

  public void setData(Object data) {
    this.data = data;
  }

  public ScNode1s getParent() {
    return parent;
  }

  public void setParent(ScNode1s parent) {
    this.parent = parent;
  }

  /**
   * @return 返回左孩子
   */
  public ScNode1s getFirstChild() {
    return firstChild;
  }

  public void setFirstChild(ScNode1s firstChild) {
    this.firstChild = firstChild;
  }

  /**
   * @return 返回右兄弟
   */
  public ScNode1s getRightSib() {
    return rightSib;
  }

  public void setRightSib(ScNode1s rightSib) {
    this.rightSib = rightSib;
  }
}
