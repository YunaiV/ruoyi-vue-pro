package cn.iocoder.yudao.coreservice.modules.pay.service.merchant;

import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayAppDO;
import cn.iocoder.yudao.framework.common.exception.ServiceException;

/**
 * 支付应用 Core Service 接口
 *
 * @author 芋道源码
 */
public interface PayAppCoreService {

    /**
     * 支付应用的合法性
     *
     * 如果不合法，抛出 {@link ServiceException} 业务异常
     *
     * @param id 应用编号
     * @return 应用信息
     */
    PayAppDO validPayApp(Long id);

}
