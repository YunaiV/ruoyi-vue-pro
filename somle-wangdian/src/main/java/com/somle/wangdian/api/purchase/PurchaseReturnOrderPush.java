package com.somle.wangdian.api.purchase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.somle.wangdian.api.WdtClient;

public class PurchaseReturnOrderPush {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WdtClient client = new WdtClient("", "", "", "");
		
		Map<String, Object> purchase_return_info = new HashMap<String, Object>();
		List<Map<String, Object>> details_list = new ArrayList<Map<String, Object>>();
		
		Map<String, Object> details_1 = new HashMap<String, Object>();
		details_1.put("spec_no", "kangxiwen1");
		details_1.put("num", 2);
		details_1.put("position_no", "A121");
		details_1.put("price", 9.9);
		
		details_list.add(details_1);
		
		purchase_return_info.put("return_no", "CR201610110002");
		purchase_return_info.put("outer_no", "ghs123");
		purchase_return_info.put("logistics_code", "TTTT");
		purchase_return_info.put("detail_list", details_list);
		
		String purchase_return_info_json = JSON.toJSONString(purchase_return_info);
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("purchase_return_info", purchase_return_info_json);
		try {
			String response = client.execute("purchase_return_order_push.php", params);
			System.out.println(response);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
