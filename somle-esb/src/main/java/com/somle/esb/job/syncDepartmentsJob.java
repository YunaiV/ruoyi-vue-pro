package com.somle.esb.job;

import com.somle.esb.service.EsbService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @className: syncDepartmentsJob
 * @author: Wqh
 * @date: 2024/10/29 17:24
 * @Version: 1.0
 * @description:
 */
@Slf4j
@Component
public class syncDepartmentsJob extends DataJob{
    @Autowired
    EsbService service;


    @Override
    public String execute(String param) throws Exception {
        service.syncDepartments();
        return "sync users success";
    }
}
