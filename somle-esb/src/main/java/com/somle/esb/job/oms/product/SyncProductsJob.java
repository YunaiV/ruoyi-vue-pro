package com.somle.esb.job.oms.product;

import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopProductDO;
import com.somle.esb.client.oms.SyncOmsClient;
import com.somle.esb.converter.oms.SalesPlatformToOmsConverter;
import com.somle.esb.enums.SalesPlatform;
import com.somle.esb.enums.SyncOmsType;
import com.somle.esb.enums.TenantId;
import com.somle.esb.job.oms.SyncOmsDataJob;
import com.somle.esb.model.OmsProfileDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SyncProductsJob extends SyncOmsDataJob {


    public void syncShopProductProfile(SalesPlatform salesPlatform, Map<SalesPlatform, SyncOmsClient> shopProductProfileClientMap) {
        // 设置租户为默认租户
        TenantContextHolder.setTenantId(TenantId.DEFAULT.getId());
        // 按平台获取店铺资料对接的客户端类型
        SyncOmsClient<?, ?> shopProductProfileClient = shopProductProfileClientMap.get(salesPlatform);
        if (shopProductProfileClient == null) {
            log.error(salesPlatform.name() + " Oms同步客户端未实现，请参考 SyncOmsClient 实现");
            return;
        }

        // 获取店铺产品信息
        List<?> shopProducts = shopProductProfileClient.getProducts();

        if (CollectionUtils.isEmpty(shopProducts)) {
            throw new RuntimeException("缺少店铺产品信息");
        }

        // 转换VO
        OmsProfileDTO<List<?>> omsProfileDTO = new OmsProfileDTO<>(salesPlatform, SyncOmsType.PRODUCT, shopProducts);
        List<OmsShopProductDO> shopProductVOs = SalesPlatformToOmsConverter.convert(omsProfileDTO, shopProductProfileClient);

        // 同步店铺产品信息
        syncShops(salesPlatform, shopProductVOs);
    }


    /**
     * 保存店铺产品信息
     **/
    public void syncShops(SalesPlatform salesPlatform, List<OmsShopProductDO> shopProductVOs) {

        if (CollectionUtils.isEmpty(shopProductVOs)) {
            throw new RuntimeException("缺少店铺产品信息");
        }

        List<OmsShopProductDO> createShopProducts = new ArrayList<>();
        List<OmsShopProductDO> updateShopProducts = new ArrayList<>();

        List<OmsShopProductDO> existShopProducts = omsShopProductService.getByPlatformCode(salesPlatform.name());

        // 使用Map存储已存在的店铺，key=sourceId, value=OmsShopProductDO
        Map<String, OmsShopProductDO> existShopProductMap = Optional.ofNullable(existShopProducts)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsShopProductDO -> omsShopProductDO.getSourceId(), omsShopProductDO -> omsShopProductDO));

        shopProductVOs.forEach(shopProductDO -> {
            OmsShopProductDO existShopProDuctDO = existShopProductMap.get(shopProductDO.getSourceId());
            if (existShopProDuctDO != null) {
                shopProductDO.setId(existShopProDuctDO.getId());
                updateShopProducts.add(shopProductDO);
            } else {
                // 新增
                createShopProducts.add(shopProductDO);
            }
        });

        if (CollectionUtil.isNotEmpty(createShopProducts)) {
            omsShopProductService.saveBatch(createShopProducts);
        }

        if (CollectionUtil.isNotEmpty(updateShopProducts)) {
            omsShopProductService.updateBatchById(updateShopProducts);
        }
        log.info("sync shop product profile success,salesPlatform:{},shopProductCount:{}", salesPlatform.name(), shopProductVOs.size());
    }
}
