package com.example.spring.dao;

import com.example.spring.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author Colin
 */
@Mapper
@Component
public interface UserMapper {

    User fetchById(Object id);

    List<User> fetchList(Map<String, Object> query);

    int save(User user);

    int deleteById(Object id);

    int update(User user);
}
