package com.wangdian.api.purchase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.wangdian.api.WdtClient;

public class PurchaseOrderPush {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WdtClient client = new WdtClient("", "", "", "");
		
		Map<String, Object> purchase_info = new HashMap<String, Object>();
		List<Map<String, Object>> details_list = new ArrayList<Map<String, Object>>();
		
		Map<String, Object> detail_1 = new HashMap<String, Object>();
		detail_1.put("spec_no", "qqq");
		detail_1.put("num", "2");
		detail_1.put("price", 1);
		detail_1.put("discount", 2);
		detail_1.put("tax", "0.1");
		detail_1.put("tax_price", "1.2");
		detail_1.put("remark", "API测试");
		detail_1.put("prop1", "011");
		detail_1.put("prop2", "022");
		
		details_list.add(detail_1);
		
		purchase_info.put("provider_no", "001");
		purchase_info.put("warehouse_no", "aiyi2test");
		purchase_info.put("outer_no", "ghs2018121001");
		purchase_info.put("is_check", "1");
		purchase_info.put("contact", "啊啊啊");
		purchase_info.put("details_list", details_list);
		
		String purchase_info_json = JSON.toJSONString(purchase_info);
		//System.out.println(purchase_info_json);
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("purchase_info", purchase_info_json);
		try {
			String response = client.execute("purchase_order_push.php", params);
			System.out.println(response);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
