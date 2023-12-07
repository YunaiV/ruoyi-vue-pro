package cn.iocoder.yudao.module.crm.service.contact;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.CrmContactCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.CrmContactPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.CrmContactUpdateReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * CRM 联系人 Service 接口
 *
 * @author 芋道源码
 */
public interface CrmContactService {

    /**
     * 创建联系人
     *
     * @param createReqVO 创建信息
     * @param userId 用户编号
     * @return 编号
     */
    Long createContact(@Valid CrmContactCreateReqVO createReqVO, Long userId);

    /**
     * 更新联系人
     *
     * @param updateReqVO 更新信息
     */
    void updateContact(@Valid CrmContactUpdateReqVO updateReqVO);

    /**
     * 删除联系人
     *
     * @param id 编号
     */
    void deleteContact(Long id);

    /**
     * 获得联系人
     *
     * @param id 编号
     * @return 联系人
     */
    CrmContactDO getContact(Long id);

    /**
     * 获得联系人列表
     *
     * @param ids 编号
     * @return 联系人列表
     */
    List<CrmContactDO> getContactList(Collection<Long> ids);

    /**
     * 获得联系人分页
     *
     * 数据权限：基于 {@link CrmContactDO}
     *
     * @param pageReqVO 分页查询
     * @return 联系人分页
     */
    PageResult<CrmContactDO> getContactPage(CrmContactPageReqVO pageReqVO);

    /**
     * 获得联系人分页，基于指定客户
     *
     * 数据权限：基于 {@link CrmCustomerDO} 读取
     *
     * @param pageReqVO 分页查询
     * @return 联系人分页
     */
    PageResult<CrmContactDO> getContactPageByCustomer(CrmContactPageReqVO pageReqVO);

    /**
     * 获取所有联系人列表
     *
     * @return 所有联系人列表
     */
    List<CrmContactDO> getContactList();

}
