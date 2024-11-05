package com.somle.wangdian.api.stock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.somle.wangdian.api.WdtClient;

public class StockoutTransferPush {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WdtClient client = new WdtClient("", "", "", "");
		
		Map<String, Object> stockout_info = new HashMap<String, Object>();
		List<Map<String, Object>> goods_list = new ArrayList<Map<String, Object>>();
		
		Map<String, Object> goods_1 = new HashMap<String, Object>();
		goods_1.put("spec_no", "ghs201812070212123");
		goods_1.put("num", "1");
		
		goods_list.add(goods_1);
		
		stockout_info.put("warehouse_no", "ghs2test");
		stockout_info.put("src_order_type", "2");
		stockout_info.put("src_order_no", "TF201812110002");
		stockout_info.put("goods_list", goods_list);
		
		String stockout_info_json = JSON.toJSONString(stockout_info);
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("stockout_info", stockout_info_json);
		try {
			String response = client.execute("stockout_transfer_push.php", params);
			System.out.println(response);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
