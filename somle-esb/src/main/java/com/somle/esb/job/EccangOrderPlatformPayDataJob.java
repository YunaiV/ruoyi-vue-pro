package com.somle.esb.job;


import com.somle.eccang.model.EccangOrderVO;
import com.somle.eccang.service.EccangService;
import com.somle.esb.model.Domain;
import com.somle.esb.model.OssData;
import com.somle.esb.service.EsbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EccangOrderPlatformPayDataJob extends EccangDataJob{


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        eccangService.getOrderPlusArchivePages(
                EccangOrderVO.builder()
                    .platformPaidDateStart(beforeYesterdayFirstSecond)
                    .platformPaidDateEnd(beforeYesterdayLastSecond)
                    .build(),
                beforeYesterday.getYear()
            )
            .forEach(page -> {
                OssData data = OssData.builder()
                    .database(DATABASE)
                    .tableName("order_platform_pay")
                    .syncType("inc")
                    .requestTimestamp(System.currentTimeMillis())
                    .folderDate(beforeYesterday)
                    .content(page)
                    .headers(null)
                    .build();
                service.send(data);
            });

        
        
        return "data upload success";
    }
}