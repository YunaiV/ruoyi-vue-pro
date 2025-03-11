package com.somle.esb.job;


import com.somle.eccang.service.EccangService;
import com.somle.eccang.service.EccangWMSService;
import com.somle.esb.model.Domain;
import com.somle.esb.service.EsbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EccangDataJob extends DataJob{
    @Autowired
    EsbService service;

    @Autowired
    EccangService eccangService;

    @Autowired
    EccangWMSService eccangWMSService;

    final String DATABASE = Domain.ECCANG.toString();


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        throw new Exception("template data job do not execute");
    }
}