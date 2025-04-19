package com.somle.esb.job;

import com.dingtalk.api.response.OapiV2UserGetResponse;
import com.somle.esb.model.OssData;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @className: KingTalkUserDataJob
 * @author: Wqh
 * @date: 2024/11/28 10:44
 * @Version: 1.0
 * @description:
 */
@Component
public class DingTalkUserDataJob extends DingTalkDataJob {
    @Override
    public String execute(String param) throws Exception {
        setDate(param);
        List<OapiV2UserGetResponse.UserGetResponse> list = dingTalkService.getUserDetailStream().toList();
        
        service.send(
            OssData.builder()
                .database(DATABASE)
                .tableName("user")
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
