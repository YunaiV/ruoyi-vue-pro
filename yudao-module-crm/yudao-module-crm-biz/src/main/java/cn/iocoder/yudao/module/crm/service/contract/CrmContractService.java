package cn.iocoder.yudao.module.crm.service.contract;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.contract.CrmContractPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.contract.CrmContractSaveReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.contract.CrmContractTransferReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractProductDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

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
     * @param id                 合同编号
     * @param contactNextTime    下次联系时间
     * @param contactLastContent 最后联系内容
     */
    void updateContractFollowUp(Long id, LocalDateTime contactNextTime, String contactLastContent);

    /**
     * 发起合同审批流程
     *
     * @param id     合同编号
     * @param userId 用户编号
     */
    void submitContract(Long id, Long userId);

    /**
     * 更新合同流程审批结果
     *
     * @param id        合同编号
     * @param bpmResult BPM 审批结果
     */
    void updateContractAuditStatus(Long id, Integer bpmResult);

    /**
     * 获得合同
     *
     * @param id 编号
     * @return 合同
     */
    CrmContractDO getContract(Long id);

    /**
     * 校验合同是否合法
     *
     * @param id 编号
     * @return 合同
     */
    CrmContractDO validateContract(Long id);

    /**
     * 获得合同列表
     *
     * @param ids 编号
     * @return 合同列表
     */
    List<CrmContractDO> getContractList(Collection<Long> ids);

    /**
     * 获得合同 Map
     *
     * @param ids 编号
     * @return 合同 Map
     */
    default Map<Long, CrmContractDO> getContractMap(Collection<Long> ids) {
        return convertMap(getContractList(ids), CrmContractDO::getId);
    }

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
     * @return 合同分页
     */
    PageResult<CrmContractDO> getContractPageByCustomerId(CrmContractPageReqVO pageReqVO);

    /**
     * 获得合同分页，基于指定商机
     *
     * 数据权限：基于 {@link CrmBusinessDO} 读取
     *
     * @param pageReqVO 分页查询
     * @return 合同分页
     */
    PageResult<CrmContractDO> getContractPageByBusinessId(CrmContractPageReqVO pageReqVO);

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

    /**
     * 根据商机编号，获取关联客户的合同数量
     *
     * @param businessId 商机编号
     * @return 数量
     */
    Long getContractCountByBusinessId(Long businessId);

    /**
     * 根据合同编号，获得合同的产品列表
     *
     * @param contactId 合同编号
     * @return 产品列表
     */
    List<CrmContractProductDO> getContractProductListByContractId(Long contactId);

    /**
     * 获得待审核合同数量
     *
     * @param userId 用户编号
     * @return 提醒数量
     */
    Long getAuditContractCount(Long userId);

    /**
     * 获得即将到期（提醒）的合同数量
     *
     * @param userId 用户编号
     * @return 提醒数量
     */
    Long getRemindContractCount(Long userId);

    /**
     * 获得合同列表
     *
     * @param customerId  客户编号
     * @param ownerUserId 负责人编号
     * @return 合同列表
     */
    List<CrmContractDO> getContractListByCustomerIdOwnerUserId(Long customerId, Long ownerUserId);

}
