package com.somle.esb.job;


import com.somle.eccang.model.EccangOrderVO;
import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

@Component
public class EccangStockDataJob extends EccangDataJob{


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        eccangService.getInventory()
                .forEach(page -> {
                    OssData data = OssData.builder()
                            .database(DATABASE)
                            .tableName("stock")
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