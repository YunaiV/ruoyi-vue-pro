package cn.iocoder.yudao.module.crm.service.message;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.backlog.vo.CrmTodayCustomerPageReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.mysql.customer.CrmCustomerMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * 待办消息 Service 实现类
 *
 * @author dhb52
 */
@Component
@Validated
public class CrmBacklogServiceImpl implements CrmBacklogService {

    @Resource
    private CrmCustomerMapper customerMapper;

    @Override
    public PageResult<CrmCustomerDO> getTodayCustomerPage(CrmTodayCustomerPageReqVO pageReqVO, Long userId) {
        return customerMapper.selectTodayCustomerPage(pageReqVO, userId);
    }

}
