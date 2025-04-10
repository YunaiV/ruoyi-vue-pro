package cn.iocoder.yudao.module.fms.api.finance;

import cn.iocoder.yudao.module.fms.service.finance.FmsAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FmsAccountApiImpl implements FmsAccountApi {

    @Autowired
    FmsAccountService fmsAccountService;

    @Override
    public void validateAccount(Long id) {
        fmsAccountService.validateAccount(id);
    }
}
