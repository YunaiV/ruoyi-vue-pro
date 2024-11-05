package com.somle.wangdian.api.trade;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.somle.wangdian.api.WdtClient;

public class TradePush {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
WdtClient client = new WdtClient("", "", "", "");
		
		List<Map<String, Object>> trade_list = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> order_list = new ArrayList<Map<String, Object>>();
		
		Map<String, Object> order_1 = new HashMap<String, Object>();
		order_1.put("oid", "ghsTest121101");
		order_1.put("num", 1);
		order_1.put("price", 12);
		order_1.put("status", 30);
		order_1.put("refund_status", 0);
		order_1.put("goods_id", "18344");
		order_1.put("spec_id", "18656");
		order_1.put("goods_no", "ghs2");
		order_1.put("spec_no", "ghs201812070212123");
		order_1.put("goods_name", "123");
		order_1.put("discount", 0);		//子订单折扣
		order_1.put("adjust_amount", 0);	//手工调整,特别注意:正的表示加价,负的表示减价
		order_1.put("share_discount", 0);
		
		
		order_list.add(order_1);
		
		Map<String, Object> trade_1 = new HashMap<String, Object>();
		trade_1.put("tid", "AT201812110002");
		trade_1.put("trade_status", 20);
		trade_1.put("pay_status", "1");
		trade_1.put("delivery_term", 2);
		trade_1.put("trade_time", "2018-12-11 14:21:00");
		trade_1.put("buyer_nick", "三国杀");
		trade_1.put("receiver_province", "河南省");
		trade_1.put("receiver_city", "周口市");
		trade_1.put("receiver_district", "川汇区");
		trade_1.put("receiver_address", "123");
		trade_1.put("logistics_type", 4);
		trade_1.put("post_amount", 12);
		trade_1.put("cod_amount", 2);
		trade_1.put("ext_cod_fee", 0);
		trade_1.put("other_amount", 1);
		trade_1.put("paid", 0);
		trade_1.put("order_list", order_list);
		

		trade_list.add(trade_1);
		
		String trade_list_json = JSON.toJSONString(trade_list);
		//System.out.println(purchase_info_json);
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("shop_no", "ghs2test");
		params.put("trade_list", trade_list_json);
		try {
			String response = client.execute("trade_push.php", params);
			System.out.println(response);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
