package com.somle.esb.job;


import com.somle.esb.model.Domain;
import com.somle.esb.model.OssData;
import com.somle.esb.service.EsbService;
import com.somle.matomo.service.MatomoService;
import com.somle.walmart.service.WalmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.IntStream;

@Component
public class WalmartDataJob extends DataJob {
    @Autowired
    EsbService service;

    @Autowired
    WalmartService  walmartService;

    final String DATABASE = Domain.WALMART.toString();


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        throw new Exception("template data job do not execute");
    }
}