package com.somle.wangdian.api.basic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.somle.wangdian.api.WdtClient;

public class PurchaseProviderCreate {
	public static void main(String[] args) {
        //sid appkey secret url
		WdtClient client = new WdtClient("", "", "", "");
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("provider_no", "ghs123");
		params.put("provider_name", "sfa");
		params.put("min_purchase_num", "1");
		params.put("purchase_cycle_days", "1");
		params.put("arrive_cycle_days", "1");
		params.put("last_purchase_time", "2018-10-10 00:00:00");
		try {
			String response = client.execute("purchase_provider_create.php", params);
			System.out.println(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}