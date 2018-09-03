package com.colin.observer;

import java.util.*;
/**
 *  Subject interface
 *  In this interface , we can only declare top 3 function, 
 *  other function we can define in an abstract class which implements this interface
 *  抽象目标角色
 */
public interface Subject  {
    public abstract void attach(Observer o);
    public abstract void detach(Observer o);
    public abstract void sendNotify();

    public abstract Vector<String> getState();
    public abstract void setState(String act, String str);
}