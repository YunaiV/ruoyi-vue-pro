package cn.iocoder.yudao.module.crm.service.contract;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.CrmContractPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.CrmContractSaveReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.CrmContractTransferReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.service.followup.bo.CrmUpdateFollowUpReqBO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * CRM 合同 Service 接口
 *
 * @author dhb52
 */
public interface CrmContractService {

    /**
     * 创建合同
     *
     * @param createReqVO 创建信息
     * @param userId      用户编号
     * @return 编号
     */
    Long createContract(@Valid CrmContractSaveReqVO createReqVO, Long userId);

    /**
     * 更新合同
     *
     * @param updateReqVO 更新信息
     */
    void updateContract(@Valid CrmContractSaveReqVO updateReqVO);

    /**
     * 删除合同
     *
     * @param id 编号
     */
    void deleteContract(Long id);

    /**
     * 合同转移
     *
     * @param reqVO  请求
     * @param userId 用户编号
     */
    void transferContract(CrmContractTransferReqVO reqVO, Long userId);

    /**
     * 更新合同相关的更进信息
     *
     * @param contractUpdateFollowUpReqBO 信息
     */
    void updateContractFollowUp(CrmUpdateFollowUpReqBO contractUpdateFollowUpReqBO);

    /**
     * 发起合同审批流程
     *
     * @param id     合同编号
     * @param userId 用户编号
     */
    void handleApprove(Long id, Long userId);

    /**
     * 获得合同
     *
     * @param id 编号
     * @return 合同
     */
    CrmContractDO getContract(Long id);

    /**
     * 获得合同列表
     *
     * @param ids 编号
     * @return 合同列表
     */
    List<CrmContractDO> getContractList(Collection<Long> ids);

    /**
     * 获得合同分页
     *
     * 数据权限：基于 {@link CrmContractDO} 读取
     *
     * @param pageReqVO 分页查询
     * @param userId    用户编号
     * @return 合同分页
     */
    PageResult<CrmContractDO> getContractPage(CrmContractPageReqVO pageReqVO, Long userId);

    /**
     * 获得合同分页，基于指定客户
     *
     * 数据权限：基于 {@link CrmCustomerDO} 读取
     *
     * @param pageReqVO 分页查询
     * @return 联系人分页
     */
    PageResult<CrmContractDO> getContractPageByCustomerId(CrmContractPageReqVO pageReqVO);

    /**
     * 查询属于某个联系人的合同数量
     *
     * @param contactId 联系人ID
     * @return 合同
     */
    Long getContractCountByContactId(Long contactId);

    /**
     * 获取关联客户的合同数量
     *
     * @param customerId 客户编号
     * @return 数量
     */
    Long getContractCountByCustomerId(Long customerId);

    // TODO @puhui999：要不改成 getContractCountByBusinessId
    /**
     * 根据商机ID获取关联客户的合同数量
     *
     * @param businessId 商机编号
     * @return 数量
     */
    Long selectCountByBusinessId(Long businessId);

}
