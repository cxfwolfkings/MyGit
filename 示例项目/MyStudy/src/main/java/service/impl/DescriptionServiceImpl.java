package service.impl;

import dao.DescriptionDao;
import entity.Description;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.DescriptionService;

//@Service注解一定不能忘记
@Service("descriptionService")
public class DescriptionServiceImpl implements DescriptionService {

    @Autowired
    private DescriptionDao descriptionDao;

    @Override
    public Description getLastDescription() {
        return descriptionDao.getLastDescription();
    }
}
