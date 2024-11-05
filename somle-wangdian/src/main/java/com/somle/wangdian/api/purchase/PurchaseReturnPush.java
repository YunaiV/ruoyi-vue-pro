package com.somle.wangdian.api.purchase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.somle.wangdian.api.WdtClient;

public class PurchaseReturnPush {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WdtClient client = new WdtClient("", "", "", "");
		
		Map<String, Object> return_info = new HashMap<String, Object>();
		List<Map<String, Object>> detail_list = new ArrayList<Map<String,Object>>();
		
		Map<String, Object> detail_1 = new HashMap<String, Object>();
		detail_1.put("spec_no", "qqq");
		detail_1.put("num", 6);
		detail_1.put("price", 123);
		detail_1.put("discount", 0);
		
		detail_list.add(detail_1);
		
		return_info.put("provider_no", "ghs123");
		return_info.put("outer_no", "ghs23214");
		return_info.put("warehouse_no", "ghs2test");
		return_info.put("detail_list", detail_list);
		
		String return_info_json = JSON.toJSONString(return_info);
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("return_info", return_info_json);
		try {
			String response = client.execute("purchase_return_push.php", params);
			System.out.println(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
