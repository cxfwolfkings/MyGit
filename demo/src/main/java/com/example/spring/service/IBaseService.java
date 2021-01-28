package com.example.spring.service;

import java.util.List;
import java.util.Map;

public interface IBaseService<T> {
    public abstract T fetchById(Object id);

    public abstract List<T> fetchList(Map<String, Object> query);

    public abstract boolean save(T t);

    public abstract boolean update(T t);

    public abstract boolean deleteById(Object id);
}
