package service;

import entity.Description;

public interface DescriptionService {
    /**
     * 获取最新一条描述
     *
     * @return
     */
    Description getLastDescription();
}
