package com.somle.esb.job;


import com.somle.eccang.model.req.EccangSpecialOrdersReqVo;
import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;


/**
 * @className: EccangGetSpecialOrdersDataJob
 * @author: gumaomao
 * @date: 2025/03/03
 * @Version: 1.0
 * @description: 易仓WMS获取退件列表
 */
@Component
public class EccangSpecialOrdersDataJob extends EccangDataJob {

    @Override
    public String execute(String param) throws Exception {
        setDate(param);
        eccangWMSService.streamSpecialOrders(EccangSpecialOrdersReqVo.builder()
            .spoAddTimeFrom(beforeYesterdayFirstSecond)
            .spoAddTimeTo(beforeYesterdayLastSecond)
            .page(1)
            .pageSize(100)
            .build()).forEach(page -> {
                OssData data = OssData.builder()
                    .database(DATABASE)
                    .tableName("special_orders")
                    .syncType("inc")
                    .requestTimestamp(System.currentTimeMillis())
                    .folderDate(beforeYesterday)
                    .content(page)
                    .headers(null)
                    .build();
                service.send(data);
            }
        );

        return "data upload success";
    }
}
