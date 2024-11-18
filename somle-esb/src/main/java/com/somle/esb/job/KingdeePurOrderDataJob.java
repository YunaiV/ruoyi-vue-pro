package com.somle.esb.job;


import com.somle.esb.model.OssData;
import com.somle.framework.common.util.date.LocalDateTimeUtils;
import com.somle.kingdee.model.KingdeePurOrderReqVO;
import org.springframework.stereotype.Component;

@Component
public class KingdeePurOrderDataJob extends KingdeeDataJob {


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        var vo = KingdeePurOrderReqVO.builder()
            .createStartTime(LocalDateTimeUtils.toTimestamp(yesterdayFirstSecond))
            .createEndTime(LocalDateTimeUtils.toTimestamp(yesterdayLastSecond))
            .build();

        kingdeeService.getClientList().stream()
            .map(client -> client.getResponse("/jdy/v2/scm/pur_order", vo))
            .forEach(response->{
                service.send(
                    OssData.builder()
                        .database(DATABASE)
                        .tableName("pur_order")
                        .syncType("inc")
                        .requestTimestamp(System.currentTimeMillis())
                        .folderDate(yesterday)
                        .content(response)
                        .headers(null)
                        .build()
                );
            });


        
        return "data upload success";
    }
}