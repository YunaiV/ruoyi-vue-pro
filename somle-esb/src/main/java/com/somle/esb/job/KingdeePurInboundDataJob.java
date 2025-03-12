package com.somle.esb.job;

import com.somle.esb.model.OssData;
import com.somle.kingdee.model.KingdeePurInbound;
import com.somle.kingdee.model.KingdeePurInboundReqVO;
import com.somle.kingdee.service.KingdeeClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KingdeePurInboundDataJob extends KingdeeDataJob{


    @Override
    public String execute(String param) throws Exception {
        // 设置日期参数
        setDate(param);
        //设置单据状态参数为所有
        var vo = KingdeePurInboundReqVO.builder()
                .billStatus("")
                .build();
        // 获取所有 Kingdee 客户端列表
        for (KingdeeClient client : kingdeeService.getClients()) {
            // 获取当前客户端的采购入库单列表
            List<KingdeePurInbound> purInbounds = client.getPurInbound(vo);
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
                            .content(purInbounds)
                            .headers(null)
                            .build()
            );
        }

        return "data upload success";
    }
}
