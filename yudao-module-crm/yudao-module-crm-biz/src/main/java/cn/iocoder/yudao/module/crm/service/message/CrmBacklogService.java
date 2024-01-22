package cn.iocoder.yudao.module.crm.service.message;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.backlog.vo.CrmTodayCustomerPageReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import jakarta.validation.Valid;

/**
 * CRM 待办消息 Service 接口
 *
 * @author dhb52
 */
public interface CrmBacklogService {

    /**
     * 根据【联系状态】、【场景类型】筛选客户分页
     *
     * @param pageReqVO 分页查询
     * @return 分页数据
     */
    PageResult<CrmCustomerDO> getTodayCustomerPage(@Valid CrmTodayCustomerPageReqVO pageReqVO, Long userId);

}
