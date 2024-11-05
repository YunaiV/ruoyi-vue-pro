package com.wangdian.api.stock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.wangdian.api.WdtClient;

public class StockSyncByPD {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WdtClient client = new WdtClient("", "", "", "");
		
		List<Map<String, Object>> goods_list = new ArrayList<Map<String, Object>>();
		
		Map<String, Object> goods_1 = new HashMap<String, Object>();
		goods_1.put("spec_no", "ghs2018120701123");
		goods_1.put("stock_num", "1000");
		
		goods_list.add(goods_1);
		
		String goods_list_json = JSON.toJSONString(goods_list);
		//System.out.println(purchase_info_json);
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("warehouse_no", "ghs2test");
		params.put("outer_no", "ghs201812101203");
		params.put("is_adjust_stock", "0");
		params.put("goods_list", goods_list_json);
		try {
			String response = client.execute("stock_sync_by_pd.php", params);
			System.out.println(response);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
