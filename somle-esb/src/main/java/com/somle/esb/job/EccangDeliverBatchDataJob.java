package com.somle.esb.job;

import com.somle.eccang.model.req.EccangDeliverBatchReqVO;
import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

/**
 * @author: gumaomao
 * @date: 2025/04/09  查询FBA发货管理数据(待发货)上传到易仓
 */
@Component
public class EccangDeliverBatchDataJob extends EccangDataJob {

    @Override
    public String execute(String param) throws Exception {
        setDate(param);
        var vo = EccangDeliverBatchReqVO.builder()
            .createdStart(beforeYesterdayFirstSecond.toString())
            .createdEnd(beforeYesterdayLastSecond.toString())
            .pageSize("20")
            .page("1")
            .build();
        eccangService.streamDeliverBatch(vo).forEach(page -> {
                OssData data = OssData.builder()
                    .database(DATABASE)
                    .tableName("deliver_batch")
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
