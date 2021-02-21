package com.colin.study.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.colin.study.dao.DescriptionDao;
import com.colin.study.entity.Description;
import com.colin.study.service.DescriptionService;

@Service("descriptionService")
public class DescriptionServiceImpl implements DescriptionService {

    @Autowired
    private DescriptionDao descriptionDao;

    @Override
    public Description getLastDescription() {
        return descriptionDao.getLastDescription();
    }
}
