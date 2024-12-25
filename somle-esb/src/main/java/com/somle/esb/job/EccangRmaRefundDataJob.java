package com.somle.esb.job;

import com.somle.eccang.model.EccangRmaRefundVO;
import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

/**
 * @author: Wqh
 * @date: 2024/12/18 8:56
 */
@Component
public class EccangRmaRefundDataJob extends EccangDataJob {
    @Override
    public String execute(String param) throws Exception {
        setDate(param);
        EccangRmaRefundVO eccangRmaRefundVO = new EccangRmaRefundVO();
        eccangRmaRefundVO.setRefundDateForm(beforeYesterdayFirstSecond);
        eccangRmaRefundVO.setRefundDateTo(beforeYesterdayLastSecond);
        eccangService.getRmaRefundList(eccangRmaRefundVO)
            .forEach(page -> {
                OssData data = OssData.builder()
                    .database(DATABASE)
                    .tableName("order_refund")
                    .syncType("inc")
                    .requestTimestamp(System.currentTimeMillis())
                    .folderDate(beforeYesterday)
                    .content(page)
                    .headers(null)
                    .build();
                service.send(data);
            });

        return "data upload success";
    }
}
