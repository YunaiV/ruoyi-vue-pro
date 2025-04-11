package com.somle.esb.job;


import com.somle.eccang.model.EccangUser;
import com.somle.eccang.model.req.EccangShipBatchReqVo;
import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

/**
 * @author: gumaomao
 * @date: 2025/04/10
 * @description: 易仓获取头程出货单数据
 */
@Component
public class EccangShipBatchDataJob extends EccangDataJob {

    @Override
    public String execute(String param) throws Exception {
        setDate(param);
        EccangUser user = eccangService.getUser();
        var vo = EccangShipBatchReqVo.builder()
            .userId(String.valueOf(user.getUserId()))
            .dateFor(beforeYesterday)
            .dateTo(yesterday)
            .page(1)
            .pageSize(100)
            .build();
        eccangService.streamShipBatch(vo).forEach(page -> {
                OssData data = OssData.builder()
                    .database(DATABASE)
                    .tableName("ship_batch")
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
