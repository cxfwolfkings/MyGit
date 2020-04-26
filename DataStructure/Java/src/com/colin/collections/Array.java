package com.colin.collections;

/**
 * 数组
 * 
 * @author 侠客
 * @added 2020年4月27日 上午5:08:26
 * @version 1.0.0
 */
public class Array {
  private int[] data;
  private int size;

  // 构造函数，传入数组的容量capacity构造Array
  public Array(int capacity) {
    data = new int[capacity];
    size = 0;
  }

  // 无参数的构造函数，默认数组的容量capacity=10
  public Array() {
    this(10);
  }

  // 获取数组的容量
  public int getCapacity() {
    return data.length;
  }

  // 获取数组中的元素个数
  public int getSize() {
    return size;
  }

  // 返回数组是否为空
  public boolean isEmpty() {
    return size == 0;
  }
}
