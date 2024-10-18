package com.somle.esb.job;


import com.somle.eccang.model.EccangOrderVO;
import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

@Component
public class EccangOrderPlatformShipDataJob extends EccangDataJob{


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        eccangService.getOrderPlusArchivePages(
                        EccangOrderVO.builder()
                                .platformShipDateStart(yesterdayFirstSecond)
                                .platformShipDateEnd(yesterdayLastSecond)
                                .build(),
                        String.valueOf(yesterday.getYear())
                )
                .forEach(page -> {
                    OssData data = OssData.builder()
                            .database(DATABASE)
                            .tableName("order_platform_ship")
                            .syncType("inc")
                            .requestTimestamp(System.currentTimeMillis())
                            .folderDate(yesterday)
                            .content(page)
                            .headers(null)
                            .build();
                    service.send(data);
                });
        
        return "data upload success";
    }
}