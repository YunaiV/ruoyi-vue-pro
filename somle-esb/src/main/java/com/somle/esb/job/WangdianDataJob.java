package com.somle.esb.job;


import com.somle.esb.model.Domain;
import com.somle.esb.service.EsbService;
import com.wangdian.service.WangdianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WangdianDataJob extends DataJob{
    @Autowired
    EsbService service;

    @Autowired
    WangdianService wangdianService;

    final String DATABASE = Domain.ECCANG.toString();


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        throw new Exception("template data job do not execute");
    }
}