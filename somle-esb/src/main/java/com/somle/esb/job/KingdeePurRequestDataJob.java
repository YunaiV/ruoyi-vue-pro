package com.somle.esb.job;

import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import com.somle.esb.model.OssData;
import com.somle.kingdee.model.KingdeePurRequest;
import com.somle.kingdee.model.KingdeePurRequestReqVO;
import com.somle.kingdee.service.KingdeeClient;
import org.springframework.stereotype.Component;

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

            client.streamPurRequest(vo).forEach(

                page -> {
                    service.send(
                        OssData.builder()
                            .database(DATABASE)
                            .tableName("pur_request")
                            .syncType("inc")
                            .requestTimestamp(System.currentTimeMillis())
                            .folderDate(yesterday)
                            .content(page)
                            .headers(null)
                            .build()
                    );

                    // 遍历每个采购请求，获取并发送详细信息
                    page.getRowsList(KingdeePurRequest.class).forEach(purRequest -> {
                        // 发送采购请求详细信息到 OSS
                        service.send(
                            OssData.builder()
                                .database(DATABASE)
                                .tableName("pur_request_detail")
                                .syncType("inc")
                                .requestTimestamp(System.currentTimeMillis())
                                .folderDate(yesterday)
                                .content(client.getPurRequestDetail(purRequest.getBillNo()))
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
