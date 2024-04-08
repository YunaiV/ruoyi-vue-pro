package cn.iocoder.yudao.module.crm.service.contact;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.CrmContactPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.CrmContactSaveReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.CrmContactTransferReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

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
     * @param userId      用户编号
     * @return 编号
     */
    Long createContact(@Valid CrmContactSaveReqVO createReqVO, Long userId);

    /**
     * 更新联系人
     *
     * @param updateReqVO 更新信息
     */
    void updateContact(@Valid CrmContactSaveReqVO updateReqVO);

    /**
     * 删除联系人
     *
     * @param id 编号
     */
    void deleteContact(Long id);

    /**
     * 联系人转移
     *
     * @param reqVO  请求
     * @param userId 用户编号
     */
    void transferContact(CrmContactTransferReqVO reqVO, Long userId);

    /**
     * 更新指定客户的联系人的负责人
     * 数据权限基于 【客户】
     *
     * @param customerId  客户编号
     * @param ownerUserId 用户编号
     */
    void updateOwnerUserIdByCustomerId(Long customerId, Long ownerUserId);

    /**
     * 更新联系人相关跟进信息
     *
     * @param id                 编号
     * @param contactNextTime    下次联系时间
     * @param contactLastContent 最后联系内容
     */
    void updateContactFollowUp(Long id, LocalDateTime contactNextTime, String contactLastContent);

    /**
     * 更新联系人的下次联系时间
     *
     * @param ids             编号数组
     * @param contactNextTime 下次联系时间
     */
    void updateContactContactNextTime(Collection<Long> ids, LocalDateTime contactNextTime);

    /**
     * 获得联系人
     *
     * @param id 编号
     * @return 联系人
     */
    CrmContactDO getContact(Long id);

    /**
     * 校验联系人
     *
     * @param id 编号
     */
    void validateContact(Long id);

    /**
     * 获得联系人列表
     *
     * @param ids 编号
     * @return 联系人列表
     */
    List<CrmContactDO> getContactList(Collection<Long> ids);

    /**
     * 获得联系人 Map
     *
     * @param ids 编号
     * @return 联系人 Map
     */
    default Map<Long, CrmContactDO> getContactMap(Collection<Long> ids) {
        return convertMap(getContactList(ids), CrmContactDO::getId);
    }

    /**
     * 获取联系人列表（校验权限）
     *
     * @param userId 用户编号
     * @return 联系人列表
     */
    List<CrmContactDO> getContactList(Long userId);

    /**
     * 获得联系人分页
     *
     * 数据权限：基于 {@link CrmContactDO}
     *
     * @param pageReqVO 分页查询
     * @param userId    用户编号
     * @return 联系人分页
     */
    PageResult<CrmContactDO> getContactPage(CrmContactPageReqVO pageReqVO, Long userId);

    /**
     * 获得联系人分页
     *
     * 数据权限：基于 {@link CrmCustomerDO}
     *
     * @param pageVO 分页查询
     * @return 联系人分页
     */
    PageResult<CrmContactDO> getContactPageByCustomerId(CrmContactPageReqVO pageVO);

    /**
     * 获得联系人分页
     *
     * 数据权限：基于 {@link CrmBusinessDO}
     *
     * @param pageVO 分页查询
     * @return 联系人分页
     */
    PageResult<CrmContactDO> getContactPageByBusinessId(CrmContactPageReqVO pageVO);

    /**
     * 获取关联客户的联系人数量
     *
     * @param customerId 客户编号
     * @return 数量
     */
    Long getContactCountByCustomerId(Long customerId);

    /**
     * 获得联系人列表
     *
     * @param customerId  客户编号
     * @param ownerUserId 负责人编号
     * @return 联系人列表
     */
    List<CrmContactDO> getContactListByCustomerIdOwnerUserId(Long customerId, Long ownerUserId);

}
