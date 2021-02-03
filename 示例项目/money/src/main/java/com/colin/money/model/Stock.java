package com.colin.money.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 股票
 * @author 23907
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stock extends Entity {
    /**
     * 主键Id
     */
    private int id;
    /**
     * 编号
     */
    private String stockCode;
    /**
     * 名称
     */
    private String stockName;
    /**
     * 现价
     */
    private double stockPrice;
}
