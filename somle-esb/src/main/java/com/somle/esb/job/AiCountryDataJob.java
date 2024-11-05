package com.somle.esb.job;


import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class AiCountryDataJob extends AiDataJob {


    @Override
    public String execute(String param) throws Exception {
        setDate(param);


        service.send(
                OssData.builder()
                        .database(DATABASE)
                        .tableName("country")
                        .syncType("full")
                        .requestTimestamp(System.currentTimeMillis())
                        .folderDate(LocalDate.now())
                        .content(aiService.getCountries().toList())
                        .headers(null)
                        .build()
        );
        return "data upload success";

    }
}