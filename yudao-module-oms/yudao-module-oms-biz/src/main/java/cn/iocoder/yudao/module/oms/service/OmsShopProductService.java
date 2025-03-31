package cn.iocoder.yudao.module.oms.service;

import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopProductDO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * OMS店铺产品 Service 接口
 *
 * @author 索迈管理员
 */
public interface OmsShopProductService extends IService<OmsShopProductDO> {


    /**
     * @param platformCode 平台代码
     * @Description: 通过平台代码获取店铺产品集合
     * @return: @return {@link List }<{@link OmsShopProductDO }>
     */
    List<OmsShopProductDO> getByPlatformCode(String platformCode);

}