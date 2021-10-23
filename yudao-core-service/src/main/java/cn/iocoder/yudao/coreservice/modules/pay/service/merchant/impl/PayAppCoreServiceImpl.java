package cn.iocoder.yudao.coreservice.modules.pay.service.merchant.impl;

import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayAppDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.mysql.merchant.PayAppCoreMapper;
import cn.iocoder.yudao.coreservice.modules.pay.service.merchant.PayAppCoreService;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.coreservice.modules.pay.enums.PayErrorCodeCoreConstants.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 支付应用 Core Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Valid
@Slf4j
public class PayAppCoreServiceImpl implements PayAppCoreService {

    @Resource
    private PayAppCoreMapper payAppCoreMapper;

    @Override
    public PayAppDO validPayApp(Long id) {
        PayAppDO app = payAppCoreMapper.selectById(id);
        // 校验是否存在
        if (app == null) {
            throw exception(PAY_APP_NOT_FOUND);
        }
        // 校验是否禁用
        if (CommonStatusEnum.DISABLE.getStatus().equals(app.getStatus())) {
            throw exception(PAY_APP_IS_DISABLE);
        }
        return app;
    }

}
