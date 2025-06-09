package com.somle.esb.job;

import com.somle.eccang.model.req.EccangProductInventoryReqVo;
import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

/**
 * @className: EccangProductInventoryDataJob
 * @author: gumaomao
 * @date: 2025/04/08
 * @Version: 1.0
 * @description: 易仓WMS获取产品库存
 */
@Component
public class EccangProductInventoryDataJob extends EccangDataJob {

    @Override
    public String execute(String param) throws Exception {
        setDate(param);
        var vo = EccangProductInventoryReqVo.builder()
            .page(1)
            .pageSize(100)
            .build();
        eccangWMSService.streamProductInventory(vo).forEach(page -> {
                OssData data = OssData.builder()
                    .database(DATABASE)
                    .tableName("product_inventory")
                    .syncType("full")
                    .requestTimestamp(System.currentTimeMillis())
                    .folderDate(today)
                    .content(page)
                    .headers(null)
                    .build();
                service.send(data);
            }
        );
        return "data upload success";
    }
}
