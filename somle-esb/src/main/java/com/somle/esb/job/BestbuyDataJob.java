package com.somle.esb.job;

import com.somle.bestbuy.service.BestbuyService;
import com.somle.esb.model.Domain;
import com.somle.esb.service.EsbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: Wqh
 * @date: 2024/12/13 14:30
 **/
@Component
public class BestbuyDataJob extends DataJob{
    @Autowired
    EsbService service;

    @Autowired
    BestbuyService bestbuyService;

    final String DATABASE = Domain.BESTBUY.toString();


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        throw new Exception("template data job do not execute");
    }
}
