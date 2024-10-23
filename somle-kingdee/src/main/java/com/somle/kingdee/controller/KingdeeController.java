package com.somle.kingdee.controller;


import com.somle.framework.common.util.json.JSONObject;
import com.somle.kingdee.model.KingdeeCustomField;
import com.somle.kingdee.model.KingdeeResponse;
import com.somle.kingdee.model.KingdeeToken;
import com.somle.kingdee.service.KingdeeClient;
import com.somle.kingdee.service.KingdeeService;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequestMapping("/api/kingdee")
public class KingdeeController {

    @Autowired
    private KingdeeService kingdeeService;

    private KingdeeClient kingdeeClient;

    @PostConstruct
    public void init() {
        this.kingdeeClient = kingdeeService.getClientList().get(0);
        log.info(kingdeeClient.getToken().getAccountName());
    }

    @Data
    public static class KingdeeRequest {
        private String bizType;
        private String operation;
        private long timestamp;
        private List<KingdeeToken> data;
    }

    @GetMapping("/getAppToken")
    public Object getAppToken() {
        return kingdeeClient.getAppToken1(kingdeeClient.getToken());
    }













    @PostMapping("/refreshAuth")
    @ResponseBody
    public void refreshAuth(
    ) {
        kingdeeService.refreshAuths();
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


    @GetMapping("/getSupplier")
    @ResponseBody
    public KingdeeResponse getSupplier(
    ) {
        return kingdeeClient.getSupplier();
    }

    @GetMapping("/getVoucher")
    @ResponseBody
    public KingdeeResponse getVoucher(
        @RequestParam String date
    ) {
        return kingdeeClient.getVoucher(LocalDate.parse(date));
    }

    @GetMapping("/getVoucherDetail")
    @ResponseBody
    public KingdeeResponse getVoucherDetail(
        @RequestParam String id
    ) {
        return kingdeeClient.getVoucherDetail(id);
    }

    @GetMapping("/getCustomField")
    @ResponseBody
    public Stream<KingdeeCustomField> getCustomField(
        @RequestParam String entity_number
    ) {
        return kingdeeClient.getCustomField(entity_number);
    }

    @GetMapping("/list")
    @ResponseBody
    public List<KingdeeResponse> list(
        @RequestParam String endpoint
    ) {
        return kingdeeClient.list(endpoint).toList();
    }

    @PostMapping("/post")
    @ResponseBody
    public KingdeeResponse post(
        @RequestParam String endpoint,
        @RequestBody JSONObject payload
    ) {
        log.debug("delegate to service");
        return kingdeeClient.post(endpoint, payload);
    }

}