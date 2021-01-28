package com.example.spring.service.impl;

import com.example.spring.service.IBaseService;

import java.util.List;
import java.util.Map;

public class BaseServiceImpl<T> implements IBaseService<T> {
    @Override
    public T fetchById(Object id) {
        return null;
    }

    @Override
    public boolean save(T t) {
        return false;
    }

    @Override
    public boolean deleteById(Object id) {
        return false;
    }

    @Override
    public List<T> fetchList(Map query) {
        return null;
    }

    @Override
    public boolean update(T t) {
        return false;
    }
}
