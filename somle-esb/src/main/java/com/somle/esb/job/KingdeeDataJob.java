package com.somle.esb.job;


import com.somle.eccang.service.EccangService;
import com.somle.esb.model.Domain;
import com.somle.esb.service.EsbService;
import com.somle.kingdee.service.KingdeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KingdeeDataJob extends DataJob{
    @Autowired
    EsbService service;

    @Autowired
    KingdeeService kingdeeService;

    final String DATABASE = Domain.KINGDEE.toString();


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        throw new Exception("template data job do not execute");
    }
}