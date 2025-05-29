package com.somle.esb.job;

import com.somle.eccang.model.req.EccangRmaRefaReqVO;
import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

//ram管理——退件重发列表
@Component
public class EccangRmaRefaDataJob extends EccangDataJob {

    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        var vo = EccangRmaRefaReqVO.builder()
            .page(1)
            .pageSize(100)
            .createDateStart(beforeYesterdayFirstSecond)
            .createDateEnd(beforeYesterdayLastSecond)
            .build();

        eccangService.getRmaRefaList(vo).forEach(page -> {
            OssData data = OssData.builder()
                .database(DATABASE)
                .tableName("rma_refa")
                .syncType("inc")
                .requestTimestamp(System.currentTimeMillis())
                .folderDate(beforeYesterday)
                .content(page)
                .headers(null)
                .build();
            service.send(data);
        });
        return "Data upload success";
    }
}
