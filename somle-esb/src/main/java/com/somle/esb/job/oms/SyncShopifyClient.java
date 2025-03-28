package com.somle.esb.job.oms;

import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import com.somle.esb.enums.SalesPlatform;
import com.somle.shopify.model.ShopifyToken;
import com.somle.shopify.model.reps.ShopifyShopRepsDTO;
import com.somle.shopify.service.ShopifyClient;
import com.somle.shopify.service.ShopifyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@Slf4j
@Component
public class SyncShopifyClient extends SyncOmsClient<JSONObject, JSONObject> {


    @Resource
    private ShopifyService shopifyService;

    public SyncShopifyClient() {
        super(SalesPlatform.SHOPIFY);
    }


    private Map<String, JSONObject> shopMap = new HashMap<>();

    /**
     * 获得店铺信息
     **/
    public List getShops() {
        List<ShopifyToken> allShopifyTokens = shopifyService.getAllShopifyTokens();
        List<ShopifyShopRepsDTO> allShops = new ArrayList<>();
        for (ShopifyToken shopifyToken : allShopifyTokens) {
            ShopifyClient shopifyClient = new ShopifyClient(shopifyToken);
            allShops.addAll(shopifyClient.getShops());
        }
        return allShops;
    }
}



