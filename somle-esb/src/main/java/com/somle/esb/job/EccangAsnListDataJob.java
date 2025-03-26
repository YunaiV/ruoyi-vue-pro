package com.somle.esb.job;

import com.somle.eccang.model.req.EccangAsnListReqVo;
import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

/**
 * @Description: 易仓WMS获取入库单列表
 * @Author: gumaomao
 * @Date: 2025/03/26
 */

@Component
public class EccangAsnListDataJob extends EccangDataJob {

    @Override
    public String execute(String param) throws Exception {
        setDate(param);
        eccangWMSService.streamAsnList(EccangAsnListReqVo.builder()
            .page(1)
            .pageSize(100)
            .build()).forEach(page -> {
            OssData data = OssData.builder()
                .database(DATABASE)
                .tableName("asn_list")
                .syncType("full")
                .requestTimestamp(System.currentTimeMillis())
                .folderDate(today)
                .content(page)
                .headers(null)
                .build();
            service.send(data);
        });
        return "data upload success";
    }
}
