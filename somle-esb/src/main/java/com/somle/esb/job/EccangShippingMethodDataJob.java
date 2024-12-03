package com.somle.esb.job;

import com.somle.eccang.model.EccangShippingMethod;
import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: Wqh
 * @date: 2024/11/29 16:09
 */
@Component
public class EccangShippingMethodDataJob extends EccangDataJob{
    @Override
    public String execute(String param) throws Exception {
        setDate(param);
        List<EccangShippingMethod> list = eccangService.getShippingMethod().toList();
        service.send(
                OssData.builder()
                        .database(DATABASE)
                        .tableName("shipping_method")
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
