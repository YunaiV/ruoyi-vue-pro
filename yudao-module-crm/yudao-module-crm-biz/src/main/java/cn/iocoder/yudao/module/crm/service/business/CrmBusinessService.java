package cn.iocoder.yudao.module.crm.service.business;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessSaveReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessTransferReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.service.business.bo.CrmBusinessUpdateProductReqBO;
import cn.iocoder.yudao.module.crm.service.followup.bo.CrmUpdateFollowUpReqBO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 商机 Service 接口
 *
 * @author ljlleo
 */
public interface CrmBusinessService {

    /**
     * 创建商机
     *
     * @param createReqVO 创建信息
     * @param userId      用户编号
     * @return 编号
     */
    Long createBusiness(@Valid CrmBusinessSaveReqVO createReqVO, Long userId);

    /**
     * 更新商机
     *
     * @param updateReqVO 更新信息
     */
    void updateBusiness(@Valid CrmBusinessSaveReqVO updateReqVO);

    /**
     * 更新商机相关跟进信息
     *
     * @param updateFollowUpReqBOList 跟进信息
     */
    void updateBusinessFollowUpBatch(List<CrmUpdateFollowUpReqBO> updateFollowUpReqBOList);

    /**
     * 删除商机
     *
     * @param id 编号
     */
    void deleteBusiness(Long id);

    /**
     * 商机转移
     *
     * @param reqVO  请求
     * @param userId 用户编号
     */
    void transferBusiness(CrmBusinessTransferReqVO reqVO, Long userId);

    /**
     * 更新商机关联商品
     *
     * @param updateProductReqBO 请求
     */
    void updateBusinessProduct(CrmBusinessUpdateProductReqBO updateProductReqBO);

    /**
     * 获得商机
     *
     * @param id 编号
     * @return 商机
     */
    CrmBusinessDO getBusiness(Long id);

    /**
     * 获得商机列表
     *
     * @param ids 编号
     * @return 商机列表
     */
    List<CrmBusinessDO> getBusinessList(Collection<Long> ids, Long userId);

    /**
     * 获得商机列表
     *
     * @param ids 编号
     * @return 商机列表
     */
    List<CrmBusinessDO> getBusinessList(Collection<Long> ids);

    /**
     * 获得商机分页
     *
     * 数据权限：基于 {@link CrmBusinessDO}
     *
     * @param pageReqVO 分页查询
     * @param userId    用户编号
     * @return 商机分页
     */
    PageResult<CrmBusinessDO> getBusinessPage(CrmBusinessPageReqVO pageReqVO, Long userId);

    /**
     * 获得商机分页，基于指定客户
     *
     * 数据权限：基于 {@link CrmCustomerDO} 读取
     *
     * @param pageReqVO 分页查询
     * @return 商机分页
     */
    PageResult<CrmBusinessDO> getBusinessPageByCustomerId(CrmBusinessPageReqVO pageReqVO);

    /**
     * 获得商机分页，基于指定联系人
     *
     * 数据权限：基于 {@link CrmContactDO} 读取
     *
     * @param pageReqVO 分页参数
     * @return 商机分页
     */
    PageResult<CrmBusinessDO> getBusinessPageByContact(CrmBusinessPageReqVO pageReqVO);

    /**
     * 获取关联客户的商机数量
     *
     * @param customerId 客户编号
     * @return 数量
     */
    Long getBusinessCountByCustomerId(Long customerId);

}
