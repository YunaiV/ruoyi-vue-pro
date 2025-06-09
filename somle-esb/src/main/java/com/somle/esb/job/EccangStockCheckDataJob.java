package com.somle.esb.job;


import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

@Component
public class EccangStockCheckDataJob extends EccangDataJob {


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        eccangService.getStockCheck()
                .forEach(page -> {
                    OssData data = OssData.builder()
                            .database(DATABASE)
                        .tableName("stockCheck")
                            .syncType("full")
                            .requestTimestamp(System.currentTimeMillis())
                            .folderDate(today)
                            .content(page)
                            .headers(null)
                            .build();
                    service.send(data);
                });
        
        return "data upload success";
    }
}