package cn.iocoder.yudao.module.crm.service.message;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.message.vo.CrmTodayCustomerPageReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import jakarta.validation.Valid;

/**
 * CRM 代办消息 Service 接口
 *
 * @author dhb52
 */
public interface CrmMessageService {

    /**
     *
     * @param pageReqVO
     * @return
     */
    PageResult<CrmCustomerDO> getTodayCustomerPage(@Valid CrmTodayCustomerPageReqVO pageReqVO, Long userId);

}
