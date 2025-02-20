package com.somle.esb.job;

import cn.iocoder.yudao.framework.common.util.lang.date.LocalDateTimeUtils;
import com.somle.esb.model.OssData;
import com.somle.kingdee.model.KingdeePurRequest;
import com.somle.kingdee.model.KingdeePurRequestReqVO;
import com.somle.kingdee.service.KingdeeClient;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: Wqh
 * @date: 2024/12/25 11:04
 */
@Component
public class KingdeePurRequestDataJob extends KingdeeDataJob {
    @Override
    public String execute(String param) throws Exception {
        setDate(param);
        var vo = KingdeePurRequestReqVO.builder()
            .createStartTime(LocalDateTimeUtils.toTimestamp(yesterdayFirstSecond))
            .createEndTime(LocalDateTimeUtils.toTimestamp(yesterdayLastSecond))
            .build();

        // 获取所有 Kingdee 客户端列表
        for (KingdeeClient client : kingdeeService.getClients()) {
            // 获取当前客户端的采购请求列表
            List<KingdeePurRequest> purRequests = client.getPurRequest(vo);
            // 获取当前时间戳，用于记录请求时间
            long currentTimeMillis = System.currentTimeMillis();
            // 发送采购请求数据到 OSS
            service.send(
                OssData.builder()
                    .database(DATABASE)
                    .tableName("pur_request")
                    .syncType("inc")
                    .requestTimestamp(currentTimeMillis)
                    .folderDate(yesterday)
                    .content(purRequests)
                    .headers(null)
                    .build()
            );

            // 遍历每个采购请求，获取并发送详细信息
            purRequests.forEach(purRequest -> {
                // 发送采购请求详细信息到 OSS
                service.send(
                    OssData.builder()
                        .database(DATABASE)
                        .tableName("pur_request_detail")
                        .syncType("inc")
                        .requestTimestamp(currentTimeMillis)
                        .folderDate(yesterday)
                        .content(client.getPurRequestDetail(purRequest.getBillNo()))
                        .headers(null)
                        .build()
                );
            });
        }

        return "data upload success";
    }
}
