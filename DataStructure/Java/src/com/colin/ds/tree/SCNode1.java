package com.colin.ds.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * 双亲表示法
 * @author  侠客
 * @added   2020年5月8日 上午5:56:54
 * @version 1.0.0
 */
public class SCNode1 {
  private Object data;
  private SCNode1 parent;
  
  public SCNode1(Object data, SCNode1 parent) {
    this.data = data;
    this.parent = parent;
  }
  
  /**
   * 结点数据
   * @return
   */
  public Object getData() {
    return data;
  }
  
  public void setData(Object data) {
    this.data = data;
  }
  
  /**
   * 双亲节点
   * @return
   */
  public SCNode1 getParent() {
    return parent;
  }
  
  public void setParent(SCNode1 parent) {
    this.parent = parent;
  } 
  
  /**
   * <a Reef="http://viyitech.cn/public/images/java_tree.PNG">测试树示例</a>
   * @param args
   */
  public static void main(String[] args) {
    List<SCNode1> tree = new ArrayList<SCNode1>();
    SCNode1 nodeA = new SCNode1("A", null);
    SCNode1 nodeB = new SCNode1("B", nodeA);
    SCNode1 nodeC = new SCNode1("C", nodeA);
    SCNode1 nodeD = new SCNode1("A", nodeB);
    SCNode1 nodeE = new SCNode1("A", nodeC);
    SCNode1 nodeF = new SCNode1("A", nodeC);
    SCNode1 nodeG = new SCNode1("A", nodeD);
    SCNode1 nodeH = new SCNode1("A", nodeD);
    SCNode1 nodeI = new SCNode1("A", nodeD);
    SCNode1 nodeJ = new SCNode1("A", nodeE);
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
