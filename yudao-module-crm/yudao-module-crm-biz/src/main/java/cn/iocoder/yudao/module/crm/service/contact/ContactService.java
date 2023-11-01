package cn.iocoder.yudao.module.crm.service.contact;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.ContactDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * crm联系人 Service 接口
 *
 * @author 芋道源码
 */
public interface ContactService {

    /**
     * 创建crm联系人
     *
     * @param createReqVO 创建信息
     * @param userId      用户编号
     * @return 编号
     */
    Long createContact(@Valid ContactCreateReqVO createReqVO, Long userId);

    /**
     * 更新crm联系人
     *
     * @param updateReqVO 更新信息
     */
    void updateContact(@Valid ContactUpdateReqVO updateReqVO);

    /**
     * 删除crm联系人
     *
     * @param id 编号
     */
    void deleteContact(Long id);

    /**
     * 获得crm联系人
     *
     * @param id 编号
     * @return crm联系人
     */
    ContactDO getContact(Long id);

    /**
     * 获得crm联系人列表
     *
     * @param ids 编号
     * @return crm联系人列表
     */
    List<ContactDO> getContactList(Collection<Long> ids);

    /**
     * 获得crm联系人分页
     *
     * @param pageReqVO 分页查询
     * @return crm联系人分页
     */
    PageResult<ContactDO> getContactPage(ContactPageReqVO pageReqVO);

    /**
     * 获得crm联系人列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return crm联系人列表
     */
    List<ContactDO> getContactList(ContactExportReqVO exportReqVO);

    /**
     * 联系人编号
     *
     * @param reqVO  请求
     * @param userId 用户编号
     */
    void transferContact(CrmContactTransferReqVO reqVO, Long userId);

}
