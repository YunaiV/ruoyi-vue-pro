package com.somle.esb.job;

import com.dingtalk.api.response.OapiV2UserGetResponse;
import com.somle.dingtalk.service.DingTalkService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Slf4j
@Component
public class SyncUserJob extends DataJob {

    @Autowired
    DingTalkService dingTalkService;

    @Resource
    MessageChannel dingtalkUserOutputChannel;

    @Override
    public String execute(String param) throws Exception {
        Stream<OapiV2UserGetResponse.UserGetResponse> userDetailStream = dingTalkService.getUserDetailStream();
        userDetailStream.forEach(userDetail -> dingtalkUserOutputChannel.send(MessageBuilder.withPayload(userDetail).build()));
        return "sync users success";
    }
}