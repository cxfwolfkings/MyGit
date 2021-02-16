package entity;

import java.io.Serializable;
import java.sql.Date;

/**
 *
 * @author 13
 * @date 2018/6/27
 */
public class Description implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 描述
     */
    private String description;

    /**
     * 添加时间
     */
    private Date createTime;
}
