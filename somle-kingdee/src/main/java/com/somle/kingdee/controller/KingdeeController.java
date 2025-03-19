package com.somle.kingdee.controller;


import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import com.somle.kingdee.model.KingdeeToken;
import com.somle.kingdee.model.vo.KingdeeTokenVO;
import com.somle.kingdee.service.KingdeeClient;
import com.somle.kingdee.service.KingdeeService;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/kingdee")
//金蝶相关对外API
public class KingdeeController {

    @Autowired
    private KingdeeService kingdeeService;

    @Data
    public static class KingdeeRequest {
        private String bizType;
        private String operation;
        private long timestamp;
        private List<KingdeeToken> data;
    }



    @PostMapping("/refreshAuth")
    @ResponseBody
    public void refreshAuth(
    ) {
        kingdeeService.refreshAuths();
    }

    @GetMapping("/listTokens")
    @ResponseBody
    public List<KingdeeTokenVO> listTokens() {
        // 获取金蝶令牌数据
        List<KingdeeToken> kingdeeTokens = kingdeeService.listKingdeeTokens();

        // 使用 Stream 和 map 来简化转换过程
        List<KingdeeTokenVO> voList = kingdeeTokens.stream()
            .map(kingdeeToken -> {
                KingdeeTokenVO vo = new KingdeeTokenVO();
                BeanUtils.copyProperties(kingdeeToken, vo);
                return vo;
            })
            .toList();

        // 返回封装后的 VO 列表
        return voList;
    }





    @PostMapping("/broadcast")
    @ResponseBody
    public void broadcast(
        @RequestBody KingdeeRequest body
    ) {

        RestTemplate restTemplate = new RestTemplate();
        String[] urlList = {
            "http://8.218.43.27:8889/api0/givesign",
            "http://test.esb.somle.com:55002/api/kingdee/updateToken",
            "http://dev.esb.somle.com:55002/api/kingdee/updateToken",
            "http://prod.esb.somle.com:55002/api/kingdee/updateToken",
        };
        for (String url : urlList) {
            try {
                restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body), JSONObject.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}