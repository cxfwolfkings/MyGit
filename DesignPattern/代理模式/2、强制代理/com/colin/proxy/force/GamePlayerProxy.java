package com.colin.proxy.force;

public class GamePlayerProxy implements IGamePlayer{
	private IGamePlayer gamePlayer = null;
	// 通过构造函数传递要对谁进行代练
	public GamePlayerProxy(IGamePlayer _gamePlayer) {
		// TODO Auto-generated constructor stub
		this.gamePlayer = _gamePlayer;
	}
	@Override
	public void login(String user, String password) {
		// TODO Auto-generated method stub
		this.gamePlayer.login(user, password);
	}

	@Override
	public void killBoss() {
		// TODO Auto-generated method stub
		this.gamePlayer.killBoss();
	}

	@Override
	public void upgrade() {
		// TODO Auto-generated method stub
		this.gamePlayer.upgrade();
	}
	// 代理的代理暂时没有，指向自己
	@Override
	public IGamePlayer getProxy() {
		// TODO Auto-generated method stub
		return this;
	}

	
}
