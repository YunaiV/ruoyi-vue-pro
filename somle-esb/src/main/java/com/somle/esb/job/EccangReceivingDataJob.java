package com.somle.esb.job;

import com.somle.eccang.model.req.EccangReceivingReqVo;
import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;


/**
 * @author: gumaomao
 * @date: 2025/03/13  查询入库单信息上传到易仓
 */
@Component
public class EccangReceivingDataJob extends EccangDataJob {
    @Override
    public String execute(String param) throws Exception {
        setDate(param);
        var vo = EccangReceivingReqVo.builder()
            .dateFor(beforeYesterdayFirstSecond)
            .dateTo(beforeYesterdayLastSecond)
            .searchDateType("receiving_add_time")
            .page(1)
            .pageSize(100)
            .build();
        eccangService.streamReceiving(vo).forEach(page -> {
                OssData data = OssData.builder()
                    .database(DATABASE)
                    .tableName("receiving")
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
