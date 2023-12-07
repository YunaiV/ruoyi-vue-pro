package cn.iocoder.yudao.module.crm.service.contactbusinesslink;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.contactbusinesslink.vo.CrmContactBusinessLinkPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contactbusinesslink.vo.CrmContactBusinessLinkSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contactbusinesslink.CrmContactBusinessLinkDO;

import javax.validation.Valid;
import java.util.List;

/**
 * CRM 联系人商机关联 Service 接口
 *
 * @author 芋道源码
 */
public interface CrmContactBusinessLinkService {

    /**
     * 创建联系人商机关联
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createContactBusinessLink(@Valid CrmContactBusinessLinkSaveReqVO createReqVO);

    /**
     * 创建联系人商机关联
     *
     * @param createReqVO 创建信息
     */
    void createContactBusinessLinkBatch(@Valid List<CrmContactBusinessLinkSaveReqVO> createReqVO);

    /**
     * 更新联系人商机关联
     *
     * @param updateReqVO 更新信息
     */
    void updateContactBusinessLink(@Valid CrmContactBusinessLinkSaveReqVO updateReqVO);

    /**
     * 删除联系人商机关联
     *
     * @param createReqVO  删除列表
     */
    void deleteContactBusinessLink(@Valid List<CrmContactBusinessLinkSaveReqVO> createReqVO);

    /**
     * 获得联系人商机关联
     *
     * @param id 编号
     * @return 联系人商机关联
     */
    CrmContactBusinessLinkDO getContactBusinessLink(Long id);

    /**
     * 获得联系人商机关联分页
     *
     * @param pageReqVO 编号
     * @return 联系人商机关联
     */
    PageResult<CrmBusinessRespVO> getContactBusinessLinkPageByContact(CrmContactBusinessLinkPageReqVO pageReqVO);

    /**
     * 获得联系人商机关联分页
     *
     * @param pageReqVO 分页查询
     * @return 联系人商机关联分页
     */
    PageResult<CrmContactBusinessLinkDO> getContactBusinessLinkPage(CrmContactBusinessLinkPageReqVO pageReqVO);

}