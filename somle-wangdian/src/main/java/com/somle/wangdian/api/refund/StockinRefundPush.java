package com.somle.wangdian.api.refund;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.somle.wangdian.api.WdtClient;

public class StockinRefundPush {
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WdtClient client = new WdtClient("", "", "", "");

		Map<String, Object> stockin_refund_info = new HashMap<String, Object>();
		List<Map<String, Object>> detail_list = new ArrayList<Map<String, Object>>();
		
		Map<String, Object> detail_1 = new HashMap<String, Object>();
		
		detail_1.put("spec_no", "ghs201812070212123");
		detail_1.put("stockin_num", 2);
		detail_1.put("stockin_price", 0.01);
		
		detail_list.add(detail_1);
		
		stockin_refund_info.put("refund_no", "TK1812110002");
		stockin_refund_info.put("outer_no","gjs1121");
		stockin_refund_info.put("warehouse_no", "ghs2test");
		stockin_refund_info.put("logistics_code", "1213");
		stockin_refund_info.put("is_created_batch", 1);
		stockin_refund_info.put("detail_list", detail_list);
		
		//通过第三方json解析工具类fastjson将map解析成json
		String stockin_refund_info_json = JSON.toJSONString(stockin_refund_info);
		//System.out.println(goods_list_json);
		
		Map<String, String> params = new HashMap<String, String>(); 
		params.put("stockin_refund_info", stockin_refund_info_json);
		try {
			String response = client.execute("stockin_refund_push.php", params);
			System.out.println(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
