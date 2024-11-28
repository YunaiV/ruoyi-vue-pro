package com.somle.esb.job;

import com.somle.eccang.model.EccangProduct;
import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @className: EccangProductDataJob
 * @author: Wqh
 * @date: 2024/11/22 13:05
 * @Version: 1.0
 * @description:
 */
@Component
public class EccangProductDataJob extends EccangDataJob{
    @Override
    public String execute(String param) throws Exception {
        setDate(param);
        List<EccangProduct> list = eccangService.getProducts().toList();
        service.send(
                OssData.builder()
                        .database(DATABASE)
                        .tableName("product")
                        .syncType("full")
                        .requestTimestamp(System.currentTimeMillis())
                        .folderDate(today)
                        .content(list)
                        .headers(null)
                        .build()
        );

        return "data upload success";
    }
}
