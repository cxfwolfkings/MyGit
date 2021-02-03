package com.colin.money.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 可转债
 * @author 23907
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConvertibleBond extends Entity {
    /**
     * 主键Id
     */
    private int id;
    /**
     * 编号
     */
    private String bondCode;
    /**
     * 名称
     */
    private String bondName;
    /**
     * 现价
     */
    private double bondPrice;
    /**
     * 正股
     */
    private  int stockId;
    /**
     * 市净率
     */
    private double pb;
    /**
     * 转股价
     */
    private double changeStockPrice;
    /**
     * 转股价值
     */
    private double changeStockValue;
    /**
     * 纯债价值
     */
    private double pureBondValue;
    /**
     * 期权价值
     */
    private double optionValue;
    /**
     * 回售触发价
     */
    private double sellBackTrigger;
    /**
     * 持仓机构
     */
    private String holdingOrganization;
    /**
     * 到期时间
     */
    private Date dueDate;
    /**
     * 剩余规模（亿元）
     */
    private double remainingScale;
    /**
     * 成交额（万元）
     */
    private double turnover;
    /**
     * 换手率
     */
    private double turnoverRate;
    /**
     * 到期税前收益率
     */
    private double yieldBeforeMaturity;
    /**
     * 回售收益
     */
    private double sellBackProceed;
    /**
     * 评级
     */
    private String score;
}
