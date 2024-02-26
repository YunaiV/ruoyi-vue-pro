package cn.iocoder.yudao.module.crm.service.contact;

import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.CrmContactBusiness2ReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.CrmContactBusinessReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactBusinessDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * CRM 联系人与商机的关联 Service 接口
 *
 * @author 芋道源码
 */
public interface CrmContactBusinessService {

    /**
     * 创建联系人与商机的关联【通过联系人，关联商机】
     *
     * @param createReqVO 创建信息
     */
    void createContactBusinessList(@Valid CrmContactBusinessReqVO createReqVO);

    /**
     * 创建联系人与商机的关联【通过商机，关联联系人】
     *
     * @param createReqVO 创建信息
     */
    void createContactBusinessList2(@Valid CrmContactBusiness2ReqVO createReqVO);

    /**
     * 删除联系人与商机的关联【通过联系人，取关商机】
     *
     * @param deleteReqVO 删除信息
     */
    void deleteContactBusinessList(@Valid CrmContactBusinessReqVO deleteReqVO);

    /**
     * 删除联系人与商机的关联【通过商机，取关联系人】
     *
     * @param deleteReqVO 删除信息
     */
    void deleteContactBusinessList2(@Valid CrmContactBusiness2ReqVO deleteReqVO);

    /**
     * 删除联系人与商机的关联，基于联系人编号
     *
     * @param contactId 联系人编号
     */
    void deleteContactBusinessByContactId(Long contactId);

    /**
     * 获得联系人与商机的关联列表，基于联系人编号
     *
     * @param contactId 联系人编号
     * @return 联系人商机关联
     */
    List<CrmContactBusinessDO> getContactBusinessListByContactId(Long contactId);

    /**
     * 获得联系人与商机的关联列表，基于商机编号
     *
     * @param businessId 商机编号
     * @return 联系人商机关联
     */
    List<CrmContactBusinessDO> getContactBusinessListByBusinessId(Long businessId);

}