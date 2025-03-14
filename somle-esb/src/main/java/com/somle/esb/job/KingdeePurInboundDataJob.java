package com.somle.esb.job;

import com.somle.esb.model.OssData;
import com.somle.kingdee.model.KingdeePurInbound;
import com.somle.kingdee.model.KingdeePurInboundReqVO;
import com.somle.kingdee.service.KingdeeClient;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class KingdeePurInboundDataJob extends KingdeeDataJob {
    @Override
    public String execute(String param) throws Exception {
        // 设置日期参数
        setDate(param);
        for (KingdeeClient client : kingdeeService.getClients()) {
            List<KingdeePurInbound> kingdeePurInbounds = client.getPurInbound(KingdeePurInboundReqVO.builder().build());
            // 获取当前时间戳，用于记录请求时间
            long currentTimeMillis = System.currentTimeMillis();
            // 发送采购请求数据到 OSS
            service.send(
                OssData.builder()
                    .database(DATABASE)
                    .tableName("pur_inbound")
                    .syncType("full")
                    .requestTimestamp(currentTimeMillis)
                    .folderDate(today)
                    .content(kingdeePurInbounds)
                    .headers(null)
                    .build()
            );
        }
        return "data upload success";
    }
}
