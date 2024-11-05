package com.somle.wangdian.api.stock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.somle.wangdian.api.WdtClient;

public class StockoutOrderPush {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WdtClient client = new WdtClient("", "", "", "");
		
		Map<String, Object> stockout_info = new HashMap<String, Object>();
		List<Map<String, Object>> detail_list = new ArrayList<Map<String, Object>>();
		
		Map<String, Object> detail_1 = new HashMap<String, Object>();
		detail_1.put("spec_no", "ghs201812070212123");
		detail_1.put("num", "1");
		detail_1.put("price", 11);
		
		detail_list.add(detail_1);
		
		stockout_info.put("warehouse_no", "ghs2test");
		stockout_info.put("num", "2");
		stockout_info.put("remark", "测试新增其他出库单");
		stockout_info.put("outer_no", "ghs201812101205");
		//stockin_info.put("logistics_code", "ZJS001");
		stockout_info.put("detail_list", detail_list);
		
		String stockout_info_json = JSON.toJSONString(stockout_info);
		//System.out.println(purchase_info_json);
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("stockout_info", stockout_info_json);
		try {
			String response = client.execute("stockout_order_push.php", params);
			System.out.println(response);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
