package com.colin.money.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 模型类基类
 * @author 23907
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
class Entity {
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
}
