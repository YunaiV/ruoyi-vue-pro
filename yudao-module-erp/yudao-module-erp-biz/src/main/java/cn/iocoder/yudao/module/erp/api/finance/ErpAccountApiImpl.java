package cn.iocoder.yudao.module.erp.api.finance;

import cn.iocoder.yudao.module.erp.service.finance.ErpAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ErpAccountApiImpl implements ErpAccountApi {

    @Autowired
    ErpAccountService erpAccountService;

    @Override
    public void validateAccount(Long id) {
        erpAccountService.validateAccount(id);
    }
}
