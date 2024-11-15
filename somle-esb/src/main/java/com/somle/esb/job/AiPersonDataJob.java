package com.somle.esb.job;


import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
public class AiPersonDataJob extends AiDataJob {


    @Override
    public String execute(String param) throws Exception {
        setDate(param);


        var createVO = aiService.newCreateVO(yesterday);
        var updateVO = aiService.newUpdateVO(yesterday);

        var newContent = Stream.concat(
            aiService.getPersons(createVO),
            aiService.getPersons(updateVO)
        );

        newContent.forEach(response ->
            service.send(
                OssData.builder()
                    .database(DATABASE)
                    .tableName("person")
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