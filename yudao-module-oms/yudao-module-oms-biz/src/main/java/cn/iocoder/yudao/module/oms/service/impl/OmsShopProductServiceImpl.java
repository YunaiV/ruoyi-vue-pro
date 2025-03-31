package cn.iocoder.yudao.module.oms.service.impl;

import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopProductDO;
import cn.iocoder.yudao.module.oms.dal.mysql.OmsShopProductMapper;
import cn.iocoder.yudao.module.oms.service.OmsShopProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * OMS店铺产品 Service 实现类
 */
@Service
@Validated
public class OmsShopProductServiceImpl extends ServiceImpl<OmsShopProductMapper, OmsShopProductDO> implements OmsShopProductService {

    @Resource
    private OmsShopProductMapper shopProductMapper;


    @Override
    public List<OmsShopProductDO> getByPlatformCode(String platformCode) {
        return shopProductMapper.getByPlatformCode(platformCode);
    }

}