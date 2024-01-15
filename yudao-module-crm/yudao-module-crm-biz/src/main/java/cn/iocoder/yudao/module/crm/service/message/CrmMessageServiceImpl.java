package cn.iocoder.yudao.module.crm.service.message;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.message.vo.CrmTodayCustomerPageReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.mysql.customer.CrmCustomerMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

// TODO @dbh52：注释要写下
@Component
@Validated
public class CrmMessageServiceImpl implements CrmMessageService {

    @Resource
    private CrmCustomerMapper customerMapper;

    @Override
    public PageResult<CrmCustomerDO> getTodayCustomerPage(CrmTodayCustomerPageReqVO pageReqVO, Long userId) {
        return customerMapper.selectTodayCustomerPage(pageReqVO, userId);
    }

}
