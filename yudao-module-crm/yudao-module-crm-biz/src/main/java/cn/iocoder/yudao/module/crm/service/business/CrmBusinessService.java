package cn.iocoder.yudao.module.crm.service.business;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessSaveReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessTransferReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessUpdateStatusReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.funnel.CrmStatisticsFunnelReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessProductDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessStatusDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.enums.business.CrmBusinessEndStatusEnum;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

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
     * @param id                 编号
     * @param contactNextTime    下次联系时间
     * @param contactLastContent 最后联系内容
     */
    void updateBusinessFollowUp(Long id, LocalDateTime contactNextTime, String contactLastContent);

    /**
     * 更新商机的下次联系时间
     *
     * @param ids             编号数组
     * @param contactNextTime 下次联系时间
     */
    void updateBusinessContactNextTime(Collection<Long> ids, LocalDateTime contactNextTime);

    /**
     * 更新商机的状态
     *
     * @param reqVO 更新请求
     */
    void updateBusinessStatus(CrmBusinessUpdateStatusReqVO reqVO);

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
     * 获得商机
     *
     * @param id 编号
     * @return 商机
     */
    CrmBusinessDO getBusiness(Long id);

    /**
     * 校验商机是否有效
     *
     * @param id 编号
     * @return 商机
     */
    CrmBusinessDO validateBusiness(Long id);

    /**
     * 获得商机列表
     *
     * @param ids 编号
     * @return 商机列表
     */
    List<CrmBusinessDO> getBusinessList(Collection<Long> ids);

    /**
     * 获得商机 Map
     *
     * @param ids 编号
     * @return 商机 Map
     */
    default Map<Long, CrmBusinessDO> getBusinessMap(Collection<Long> ids) {
        return convertMap(getBusinessList(ids), CrmBusinessDO::getId);
    }

    /**
     * 获得指定商机编号的产品列表
     *
     * @param businessId 商机编号
     * @return 商机产品列表
     */
    List<CrmBusinessProductDO> getBusinessProductListByBusinessId(Long businessId);

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

    /**
     * 获得使用指定商机状态组的商机数量
     *
     * @param statusTypeId 商机状态组编号
     * @return 数量
     */
    Long getBusinessCountByStatusTypeId(Long statusTypeId);

    /**
     * 获得商机状态名称
     *
     * @param endStatus 结束状态
     * @param status    商机状态
     * @return 商机状态名称
     */
    default String getBusinessStatusName(Integer endStatus, CrmBusinessStatusDO status) {
        if (endStatus != null) {
            return CrmBusinessEndStatusEnum.fromStatus(endStatus).getName();
        }
        return status.getName();
    }

    /**
     * 获得商机列表
     *
     * @param customerId  客户编号
     * @param ownerUserId 负责人编号
     * @return 商机列表
     */
    List<CrmBusinessDO> getBusinessListByCustomerIdOwnerUserId(Long customerId, Long ownerUserId);

    /**
     * 获得商机分页，目前用于【数据统计】
     *
     * @param pageVO 请求
     * @return 商机分页
     */
    PageResult<CrmBusinessDO> getBusinessPageByDate(CrmStatisticsFunnelReqVO pageVO);

}
