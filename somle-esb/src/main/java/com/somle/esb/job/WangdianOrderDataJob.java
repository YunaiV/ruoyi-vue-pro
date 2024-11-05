package com.somle.esb.job;


import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class WangdianOrderDataJob extends WangdianDataJob{


    @Override
    public String execute(String param) throws Exception {
        setDate(param);

        Map<String, String> params = new HashMap<String, String>();
        params.put("start_time", yesterdayFirstSecond.toString());
        params.put("end_time", yesterdayLastSecond.toString());
        params.put("page_size", "30");
        params.put("page_no", "0");

        wangdianService.client.execute("trade_query.php", params);

        throw new Exception("template data job do not execute");
    }
}