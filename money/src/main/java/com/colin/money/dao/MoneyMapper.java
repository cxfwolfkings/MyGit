package com.colin.money.dao;

import com.colin.money.model.ConvertibleBond;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author 23907
 */
@Mapper
@Component
public interface MoneyMapper {
    /**
     * 查询可转债集合
     * @param query
     * @return
     */
    List<ConvertibleBond> fetchList(Map<String, Object> query);
}
