package com.somle.esb.job;


import com.somle.ai.model.AiReqVO;
import com.somle.ai.service.AiService;
import com.somle.esb.model.Domain;
import com.somle.esb.model.OssData;
import com.somle.esb.service.EsbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.stream.Stream;

@Component
public class AiAddressDataJob extends AiDataJob {

    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        var createVO = aiService.newCreateVO(yesterday);
        var updateVO = aiService.newUpdateVO(yesterday);

        var newContent = Stream.concat(
            aiService.getAddresses(createVO),
            aiService.getAddresses(updateVO)
        );

        newContent.forEach(response ->
            service.send(
                OssData.builder()
                    .database(DATABASE)
                    .tableName("address")
                    .syncType("inc")
                    .requestTimestamp(System.currentTimeMillis())
                    .folderDate(yesterday)
                    .content(response)
                    .headers(null)
                    .build()
            )
        );


        return "data upload success";
    }
}