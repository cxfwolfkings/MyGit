package com.colin.study.service;

import com.colin.study.entity.Description;

public interface DescriptionService {
    /**
     * 获取最新一条描述
     *
     * @return
     */
    Description getLastDescription();
}
