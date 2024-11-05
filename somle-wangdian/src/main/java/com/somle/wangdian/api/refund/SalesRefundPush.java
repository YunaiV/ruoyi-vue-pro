package com.somle.wangdian.api.refund;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.somle.wangdian.api.WdtClient;

public class SalesRefundPush {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WdtClient client = new WdtClient("", "", "", "");
		
		List<Map<String, Object>> api_refund_list = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> order_list = new ArrayList<Map<String, Object>>();
		
		Map<String, Object> order_1 = new HashMap<String, Object>();
		order_1.put("oid", "AD201812110009");
		order_1.put("num", "1");
		order_list.add(order_1);
		
		Map<String, Object> api_refund_1 = new HashMap<String, Object>();
		api_refund_1.put("tid", "AT201812110001");
		api_refund_1.put("platform_id", "127");
		api_refund_1.put("shop_no", "ghs2test");
		api_refund_1.put("refund_no", "sgsh1221g124");
		api_refund_1.put("type", 3);
		api_refund_1.put("status", "success");
		api_refund_1.put("buyer_nick", "ghs");
		api_refund_1.put("refund_time", "1212121");
		api_refund_1.put("order_list", order_list);
		

		api_refund_list.add(api_refund_1);
		
		String api_refund_list_json = JSON.toJSONString(api_refund_list);
		//System.out.println(purchase_info_json);
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("api_refund_list", api_refund_list_json);
		try {
			String response = client.execute("sales_refund_push.php", params);
			System.out.println(response);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
