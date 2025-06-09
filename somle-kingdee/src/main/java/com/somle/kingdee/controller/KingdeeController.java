package com.somle.kingdee.controller;


import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import com.somle.kingdee.model.KingdeeToken;
import com.somle.kingdee.model.supplier.KingdeeSupplierSaveVO;
import com.somle.kingdee.model.vo.KingdeeSupplierQueryReqVO;
import com.somle.kingdee.model.vo.KingdeeTokenVO;
import com.somle.kingdee.service.KingdeeService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/kingdee")
//金蝶相关对外API
public class KingdeeController {

    @Autowired
    private KingdeeService kingdeeService;

    @Data
    public static class KingdeeRequest {
        private String bizType;
        private String operation;
        private long timestamp;
        private List<KingdeeToken> data;
    }


    @PostMapping("/refreshAuth")
    @ResponseBody
    public void refreshAuth(
    ) {
        kingdeeService.refreshAuths();
    }

    @GetMapping("/listTokens")
    @ResponseBody
    public List<KingdeeTokenVO> listTokens() {
        // 获取金蝶令牌数据
        List<KingdeeToken> kingdeeTokens = kingdeeService.listKingdeeTokens();
        return kingdeeTokens.stream().map(kingdeeToken -> BeanUtils.toBean(kingdeeToken, KingdeeTokenVO.class))
            .toList();
    }


    @PostMapping("/broadcast")
    @ResponseBody
    public void broadcast(
        @RequestBody KingdeeRequest body
    ) {

        RestTemplate restTemplate = new RestTemplate();
        String[] urlList = {
            "http://8.218.43.27:8889/api0/givesign",
            "http://test.esb.somle.com:55002/api/kingdee/updateToken",
            "http://dev.esb.somle.com:55002/api/kingdee/updateToken",
            "http://prod.esb.somle.com:55002/api/kingdee/updateToken",
        };
        for (String url : urlList) {
            try {
                restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body), JSONObject.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    // 获取所有供应商列表
    @GetMapping("/getAllSupplierList")
    @ResponseBody
    public CommonResult<Map<String, KingdeeSupplierSaveVO>> getAllSupplierList() {
        KingdeeSupplierQueryReqVO queryReqVO = new KingdeeSupplierQueryReqVO();
        return CommonResult.success(kingdeeService.getAllSupplierList(queryReqVO));
    }

    /**
     * 删除供应商缓存
     *
     * @return 删除的缓存数量
     */
    @DeleteMapping("/supplier/cache")
    @ResponseBody
    public CommonResult<Integer> deleteSupplierCache() {
        return CommonResult.success(kingdeeService.deleteSupplierCache());
    }

    /**
     * 增加供应商-测试
     */
    @PostMapping("/addSupplier")
    @ResponseBody
    public CommonResult<KingdeeSupplierSaveVO> addSupplier(
        @RequestBody
        KingdeeSupplierSaveVO kingdeeSupplierSaveVO
    ) {
        kingdeeService.addSupplier(kingdeeSupplierSaveVO);
        return CommonResult.success(null);
    }
}