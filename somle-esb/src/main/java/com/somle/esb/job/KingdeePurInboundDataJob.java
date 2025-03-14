package com.somle.esb.job;

import com.somle.esb.model.OssData;
import com.somle.kingdee.model.KingdeePurInboundReqVO;
import com.somle.kingdee.service.KingdeeClient;
import org.springframework.stereotype.Component;

@Component
public class KingdeePurInboundDataJob extends KingdeeDataJob {
    @Override
    public String execute(String param) throws Exception {
        // 设置日期参数
        setDate(param);
        var vo = KingdeePurInboundReqVO.builder().build();
        for (KingdeeClient client : kingdeeService.getClients()) {
            client.streamPurInbound(vo).forEach(
                page -> {
                    service.send(
                        OssData.builder()
                            .database(DATABASE)
                            .tableName("pur_inbound")
                            .syncType("full")
                            .requestTimestamp(System.currentTimeMillis())
                            .folderDate(today)
                            .content(page)
                            .headers(null)
                            .build()
                    );
                }
            );
        }
        return "data upload success";
    }
}
