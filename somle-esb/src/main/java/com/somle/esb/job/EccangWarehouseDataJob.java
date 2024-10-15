package com.somle.esb.job;


import com.somle.eccang.model.EccangOrderVO;
import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

@Component
public class EccangWarehouseDataJob extends EccangDataJob{


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        service.send(
                OssData.builder()
                        .database(DATABASE)
                        .tableName("warehouse")
                        .syncType("full")
                        .requestTimestamp(System.currentTimeMillis())
                        .folderDate(today)
                        .content(eccangService.getWarehouseList())
                        .headers(null)
                        .build()
        );
        
        return "data upload success";
    }
}