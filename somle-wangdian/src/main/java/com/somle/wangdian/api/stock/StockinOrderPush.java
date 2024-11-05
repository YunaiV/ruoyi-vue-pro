package com.somle.wangdian.api.stock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.somle.wangdian.api.WdtClient;

public class StockinOrderPush {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WdtClient client = new WdtClient("", "", "", "");
		
		Map<String, Object> stockin_info = new HashMap<String, Object>();
		List<Map<String, Object>> goods_list = new ArrayList<Map<String, Object>>();
		
		Map<String, Object> goods_1 = new HashMap<String, Object>();
		goods_1.put("spec_no", "qqq");
		goods_1.put("stockin_num", "2");
		goods_1.put("stockin_price", 1);
		goods_1.put("src_price", 1.2);
		
		goods_list.add(goods_1);
		
		stockin_info.put("warehouse_no", "ghs2test");
		stockin_info.put("outer_no", "ghs201812101201");
		stockin_info.put("goods_list", goods_list);
		
		String stockin_info_json = JSON.toJSONString(stockin_info);

		Map<String, String> params = new HashMap<String, String>();
		params.put("stockin_info", stockin_info_json);
		try {
			String response = client.execute("stockin_order_push.php", params);
			System.out.println(response);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
