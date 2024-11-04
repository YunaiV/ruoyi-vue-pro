package com.somle.esb.job;

import com.somle.esb.service.EsbService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @className: SyncProductToKingdeeJob
 * @author: Wqh
 * @date: 2024/10/31 13:23
 * @Version: 1.0
 * @description: 同步产品到kingdee
 */
@Slf4j
@Component
public class SyncProductToKingdeeJob extends DataJob{
    @Autowired
    EsbService service;
    @Override
    public String execute(String param) throws Exception {
        service.handleProducts();
        return "sync success";
    }
}
