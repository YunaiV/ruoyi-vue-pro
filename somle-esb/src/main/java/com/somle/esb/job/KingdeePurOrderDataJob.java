package com.somle.esb.job;


import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import com.somle.esb.model.OssData;
import com.somle.kingdee.model.KingdeePurOrder;
import com.somle.kingdee.model.KingdeePurOrderReqVO;
import com.somle.kingdee.service.KingdeeClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KingdeePurOrderDataJob extends KingdeeDataJob {


    @Override
    public String execute(String param) throws Exception {
        // 设置日期参数
        setDate(param);

        // 构建请求对象
        var vo = KingdeePurOrderReqVO.builder()
            .createStartTime(LocalDateTimeUtils.toTimestamp(yesterdayFirstSecond))
            .createEndTime(LocalDateTimeUtils.toTimestamp(yesterdayLastSecond))
            .build();

        // 获取所有 Kingdee 客户端列表
        for (KingdeeClient client : kingdeeService.getClients()) {
            client.streamPurOrder(vo).forEach(

                page -> {
                    service.send(
                        OssData.builder()
                            .database(DATABASE)
                            .tableName("pur_order")
                            .syncType("inc")
                            .requestTimestamp(System.currentTimeMillis())
                            .folderDate(yesterday)
                            .content(page)
                            .headers(null)
                            .build()
                    );


                    // 遍历每个采购请求，获取并发送详细信息
                    page.getRowsList(KingdeePurOrder.class).forEach(purOrder -> {

                        service.send(
                            OssData.builder()
                                .database(DATABASE)
                                .tableName("pur_order_detail")
                                .syncType("inc")
                                .requestTimestamp(System.currentTimeMillis())
                                .folderDate(yesterday)
                                .content(client.getPurOrderDetail(purOrder.getBillNo()))
                                .headers(null)
                                .build()
                        );
                    });
                }
            );
        }
        return "data upload success";
    }
}