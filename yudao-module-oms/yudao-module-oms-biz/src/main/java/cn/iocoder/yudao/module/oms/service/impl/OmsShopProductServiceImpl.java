package cn.iocoder.yudao.module.oms.service.impl;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopProductDO;
import cn.iocoder.yudao.module.oms.dal.mysql.OmsShopProductMapper;
import cn.iocoder.yudao.module.oms.service.OmsShopProductService;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import java.util.*;

/**
 * OMS店铺产品 Service 实现类
 *
 * @author 索迈管理员
 */
@Service
@Validated
public class OmsShopProductServiceImpl implements OmsShopProductService {


    @Resource
    MessageChannel eccangSkuRelationOutputChannel;



    @Resource
    private OmsShopProductMapper shopProductMapper;



    @Override
    public void batchCreate(List<OmsShopProductDO> listToCreate) {
        shopProductMapper.insertBatch(listToCreate);
    }

    @Override
    public void batchUpdate(List<OmsShopProductDO> listToUpdate) {
        shopProductMapper.updateBatch(listToUpdate);
    }

    @Override
    public List<OmsShopProductDO> selectByShopId(Long id) {
        return shopProductMapper.selectList(new LambdaQueryWrapperX<OmsShopProductDO>()
                .eq(OmsShopProductDO::getShopId, id));
    }



}