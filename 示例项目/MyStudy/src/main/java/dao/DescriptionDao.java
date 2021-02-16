package dao;

import entity.Description;

public interface DescriptionDao {

    /**
     * 获取最新一条描述
     * @return
     */
    Description getLastDescription();
}
