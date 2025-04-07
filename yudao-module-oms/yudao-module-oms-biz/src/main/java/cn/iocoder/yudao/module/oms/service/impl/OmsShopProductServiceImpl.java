package cn.iocoder.yudao.module.oms.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductSaveReqDTO;
import cn.iocoder.yudao.module.oms.convert.OmsShopProductConvert;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopProductDO;
import cn.iocoder.yudao.module.oms.dal.mysql.OmsShopProductMapper;
import cn.iocoder.yudao.module.oms.service.OmsShopProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.OMS_SYNC_SHOP_PRODUCT_LACK;

/**
 * OMS店铺产品 Service 实现类
 */
@Service
@Slf4j
public class OmsShopProductServiceImpl extends ServiceImpl<OmsShopProductMapper, OmsShopProductDO> implements OmsShopProductService {

    @Resource
    private OmsShopProductMapper shopProductMapper;


    @Override
    public List<OmsShopProductDO> getByShopId(Long shopId) {
        return shopProductMapper.selectList(OmsShopProductDO::getShopId, shopId);
    }

    @Override
    public void createOrUpdateShopProductByPlatform(List<OmsShopProductSaveReqDTO> saveReqDTOs) {
        if (CollectionUtils.isEmpty(saveReqDTOs)) {
            throw exception(OMS_SYNC_SHOP_PRODUCT_LACK);
        }

        List<OmsShopProductDO> shopProductDOs = OmsShopProductConvert.INSTANCE.toOmsShopProductDO(saveReqDTOs);

        List<OmsShopProductDO> createShopProducts = new ArrayList<>();
        List<OmsShopProductDO> updateShopProducts = new ArrayList<>();
        //同批次的产品都是一个店铺，所以取第一个即可
        List<OmsShopProductDO> existShopProducts = getByShopId(shopProductDOs.get(0).getShopId());

        // 使用Map存储已存在的店铺产品信息，key=sourceId, value=OmsShopProductDO
        Map<String, OmsShopProductDO> existShopProductMap = Optional.ofNullable(existShopProducts)
            .orElse(Collections.emptyList())
            .stream()
            .collect(Collectors.toMap(omsShopProductDO -> omsShopProductDO.getSourceId(), omsShopProductDO -> omsShopProductDO));

        shopProductDOs.forEach(shopProductDO -> {
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
            saveBatch(createShopProducts);
        }

        if (CollectionUtil.isNotEmpty(updateShopProducts)) {
            updateBatchById(updateShopProducts);
        }
        log.info("sync shop product success,salesShopId:{},shopProductCount:{}", shopProductDOs.get(0).getShopId(), shopProductDOs.size());
    }

}