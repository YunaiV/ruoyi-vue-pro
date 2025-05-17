package cn.iocoder.yudao.module.crm.service.receivable;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.receivable.CrmReceivablePageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.receivable.CrmReceivableSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.CrmReceivableDO;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * CRM 回款 Service 接口
 *
 * @author 赤焰
 */
public interface CrmReceivableService {

    /**
     * 创建回款
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createReceivable(@Valid CrmReceivableSaveReqVO createReqVO);

    /**
     * 更新回款
     *
     * @param updateReqVO 更新信息
     */
    void updateReceivable(@Valid CrmReceivableSaveReqVO updateReqVO);

    /**
     * 更新回款流程审批结果
     *
     * @param id        回款编号
     * @param bpmResult BPM 审批结果
     */
    void updateReceivableAuditStatus(Long id, Integer bpmResult);

    /**
     * 删除回款
     *
     * @param id 编号
     */
    void deleteReceivable(Long id);

    /**
     * 发起回款审批流程
     *
     * @param id     回款编号
     * @param userId 用户编号
     */
    void submitReceivable(Long id, Long userId);

    /**
     * 获得回款
     *
     * @param id 编号
     * @return 回款
     */
    CrmReceivableDO getReceivable(Long id);

    /**
     * 获得回款列表
     *
     * @param ids 编号
     * @return 回款列表
     */
    List<CrmReceivableDO> getReceivableList(Collection<Long> ids);

    /**
     * 获得回款 Map
     *
     * @param ids 编号
     * @return 回款 Map
     */
    default Map<Long, CrmReceivableDO> getReceivableMap(Collection<Long> ids) {
        return convertMap(getReceivableList(ids), CrmReceivableDO::getId);
    }

    /**
     * 获得回款分页
     *
     * 数据权限：基于 {@link CrmReceivableDO} 读取
     *
     * @param pageReqVO 分页查询
     * @param userId    用户编号
     * @return 回款分页
     */
    PageResult<CrmReceivableDO> getReceivablePage(CrmReceivablePageReqVO pageReqVO, Long userId);

    /**
     * 获得回款分页，基于指定客户
     *
     * 数据权限：基于 {@link CrmCustomerDO} 读取
     *
     * @param pageReqVO 分页查询
     * @return 回款分页
     */
    PageResult<CrmReceivableDO> getReceivablePageByCustomerId(CrmReceivablePageReqVO pageReqVO);

    /**
     * 获得待审核回款数量
     *
     * @param userId 用户编号
     * @return 待审批数量
     */
    Long getAuditReceivableCount(Long userId);

    /**
     * 获得合同已回款金额 Map
     *
     * @param contractIds 合同编号
     * @return 回款金额 Map
     */
    Map<Long, BigDecimal> getReceivablePriceMapByContractId(Collection<Long> contractIds);

    /**
     * 根据合同编号查询回款数量
     *
     * @param contractId 合同编号
     * @return 回款数量
     */
    Long getReceivableCountByContractId(Long contractId);

}
