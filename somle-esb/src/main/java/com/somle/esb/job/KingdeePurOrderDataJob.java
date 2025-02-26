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
            // 获取当前客户端的采购请求列表
            List<KingdeePurOrder> purOrders = client.getPurOrder(vo);
            // 获取当前时间戳，用于记录请求时间
            long currentTimeMillis = System.currentTimeMillis();
            // 发送采购请求数据到 OSS
            service.send(
                OssData.builder()
                    .database(DATABASE)
                    .tableName("pur_order")
                    .syncType("inc")
                    .requestTimestamp(currentTimeMillis)
                    .folderDate(yesterday)
                    .content(purOrders)
                    .headers(null)
                    .build()
            );

            // 遍历每个采购请求，获取并发送详细信息
            purOrders.forEach(purOrder -> {
                // 发送采购请求详细信息到 OSS
                service.send(
                    OssData.builder()
                        .database(DATABASE)
                        .tableName("pur_order_detail")
                        .syncType("inc")
                        .requestTimestamp(currentTimeMillis)
                        .folderDate(yesterday)
                        .content(client.getPurOrderDetail(purOrder.getBillNo()))
                        .headers(null)
                        .build()
                );
            });
        }

        return "data upload success";
    }
}