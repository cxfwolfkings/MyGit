package com.colin.observer;

import java.util.LinkedList;
import java.util.Vector;

/**
 * A concrete subject
 * 具体目标角色
 */
public class ConcreteSubject implements Subject {
	private LinkedList<Observer> observerList;
	private Vector<String> strVector;

	public ConcreteSubject() {
		observerList = new LinkedList<Observer>();
		strVector = new Vector<String>();
	}

	public void attach(Observer o) {
		observerList.add(o);
	}

	public void detach(Observer o) {
		observerList.remove(o);
	}

	public void sendNotify() {
		for (int i = 0; i < observerList.size(); i++) {
			((Observer) observerList.get(i)).update(this);
		}
	}

	public void setState(String act, String str) {
		if (act.equals("ADD")) {
			strVector.add(str);
		} else if (act.equals("DEL")) {
			strVector.remove(str);
		}
	}

	public Vector<String> getState() {
		return strVector;
	}
}