package cn.iocoder.yudao.module.oms.service;

import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopDO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface OmsShopService extends IService<OmsShopDO>{

    /**
     * @Author: gumaomao
     * @Date: 2025/03/28
     * @return: @return {@link List }<{@link OmsShopDO }>
     */
    List<OmsShopDO> getByPlatformCode(String platformCode);

}
