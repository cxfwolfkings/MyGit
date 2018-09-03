package Facade;

/**
 * A very simple test
 * 客户角色
 * @author Charles
 * @date   2016年1月21日 上午11:16:15
 */
public class Test  {
    public static void main(String[] args) {
        FacadeRoom room = new FacadeRoom();
        room.CreateRoom();
    }
}

/**
 * 子系统角色
 */
class Wall {
    public Wall() {
        System.out.println("Create a wall !");
    }
}

/**
 * 子系统角色
 */
class Door {
    public Door() {
        System.out.println("Create a door !");
    }
}

/**
 * 门面角色
 * 一个房间(4面墙，一扇门)
 */
class FacadeRoom {
    public void CreateRoom() {
        Wall wall1 = new Wall();
        Wall wall2 = new Wall();
        Wall wall3 = new Wall();
        Wall wall4 = new Wall();
        Door door = new Door();
    }
}