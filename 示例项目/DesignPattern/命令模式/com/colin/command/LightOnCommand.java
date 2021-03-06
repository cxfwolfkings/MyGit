package com.colin.command;

/**
 * 具体命令角色
 * @author Charles
 * @date   2016年1月13日 下午4:04:24
 */
public class LightOnCommand implements Command {
	private Light myLight;
	public LightOnCommand(Light L) {
		myLight = L;
	}
	public void execute() {
		myLight.turnOn();
	}
}
