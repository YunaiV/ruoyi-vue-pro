package cn.iocoder.yudao.module.oms.service;

import java.util.*;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopProductDO;

/**
 * OMS店铺产品 Service 接口
 *
 * @author 索迈管理员
 */
public interface OmsShopProductService {

    /**
    * 批量创建
    **/
    void batchCreate(List<OmsShopProductDO> listToCreate);

    /**
    * 批量更新
    **/
    void batchUpdate(List<OmsShopProductDO> listToUpdate);

    /**
     * 根据店铺编号查询产品
     *
     * @param id 店铺编号
     * @return 产品
     */
    List<OmsShopProductDO> selectByShopId(Long id);

}