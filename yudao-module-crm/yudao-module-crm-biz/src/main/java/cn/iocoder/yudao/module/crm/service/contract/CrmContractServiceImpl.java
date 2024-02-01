package cn.iocoder.yudao.module.crm.service.contract;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.CrmContractPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.CrmContractSaveReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.CrmContractTransferReqVO;
import cn.iocoder.yudao.module.crm.convert.contract.CrmContractConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessProductDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.product.CrmProductDO;
import cn.iocoder.yudao.module.crm.dal.mysql.contract.CrmContractMapper;
import cn.iocoder.yudao.module.crm.enums.common.CrmAuditStatusEnum;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.framework.permission.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessProductService;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessService;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
import cn.iocoder.yudao.module.crm.service.followup.bo.CrmUpdateFollowUpReqBO;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionCreateReqBO;
import cn.iocoder.yudao.module.crm.service.product.CrmProductService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.crm.enums.LogRecordConstants.*;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_NOT_EXISTS;

/**
 * CRM 合同 Service 实现类
 *
 * @author dhb52
 */
@Service
@Validated
public class CrmContractServiceImpl implements CrmContractService {

    public static final String CONTRACT_APPROVE = "contract-approve"; // 合同审批流程标识

    @Resource
    private CrmContractMapper contractMapper;

    @Resource
    private CrmPermissionService crmPermissionService;
    @Resource
    private CrmBusinessProductService businessProductService;
    @Resource
    private CrmProductService productService;
    @Resource
    private CrmCustomerService customerService;
    @Resource
    private CrmBusinessService businessService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private BpmProcessInstanceApi bpmProcessInstanceApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CONTRACT_TYPE, subType = CRM_CONTRACT_CREATE_SUB_TYPE, bizNo = "{{#contract.id}}",
            success = CRM_CONTRACT_CREATE_SUCCESS)
    public Long createContract(CrmContractSaveReqVO createReqVO, Long userId) {
        validateRelationDataExists(createReqVO);
        // 1.1 插入合同
        CrmContractDO contract = BeanUtils.toBean(createReqVO, CrmContractDO.class).setId(null);
        contractMapper.insert(contract);
        // 1.2 插入商机关联商品
        List<CrmBusinessProductDO> businessProduct = convertBusinessProductList(createReqVO);
        businessProductService.insertBatch(businessProduct);

        // 2. 创建数据权限
        crmPermissionService.createPermission(new CrmPermissionCreateReqBO().setUserId(userId)
                .setBizType(CrmBizTypeEnum.CRM_CONTRACT.getType()).setBizId(contract.getId())
                .setLevel(CrmPermissionLevelEnum.OWNER.getLevel()));

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("contract", contract);
        return contract.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CONTRACT_TYPE, subType = CRM_CONTRACT_UPDATE_SUB_TYPE, bizNo = "{{#updateReqVO.id}}",
            success = CRM_CONTRACT_UPDATE_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTRACT, bizId = "#updateReqVO.id", level = CrmPermissionLevelEnum.WRITE)
    public void updateContract(CrmContractSaveReqVO updateReqVO) {
        // TODO @合同待定：只有草稿、审批中，可以编辑；
        if (ObjUtil.notEqual(updateReqVO.getAuditStatus(), CrmAuditStatusEnum.DRAFT.getStatus()) ||
                ObjUtil.notEqual(updateReqVO.getAuditStatus(), CrmAuditStatusEnum.PROCESS.getStatus())) {
            throw exception(CONTRACT_UPDATE_FAIL_EDITING_PROHIBITED);
        }
        validateRelationDataExists(updateReqVO);
        // 校验存在
        CrmContractDO oldContract = validateContractExists(updateReqVO.getId());
        // 更新合同
        CrmContractDO updateObj = BeanUtils.toBean(updateReqVO, CrmContractDO.class);
        contractMapper.updateById(updateObj);

        // TODO puhui999: @芋艿：合同变更关联的商机后商品怎么处理？
        // TODO @puhui999：和商品 spu、sku 编辑一样；新增的插入；修改的更新；删除的删除
        //List<CrmBusinessProductDO> businessProduct = convertBusinessProductList(updateReqVO);
        //businessProductService.selectListByBusinessId()
        //diffList()

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, BeanUtils.toBean(oldContract, CrmContractSaveReqVO.class));
        LogRecordContext.putVariable("contractName", oldContract.getName());
    }

    // TODO @合同待定：缺一个取消合同的接口；只有草稿、审批中可以取消；CrmAuditStatusEnum

    private List<CrmBusinessProductDO> convertBusinessProductList(CrmContractSaveReqVO reqVO) {
        // 校验商品存在
        Set<Long> productIds = convertSet(reqVO.getProductItems(), CrmContractSaveReqVO.CrmContractProductItem::getId);
        List<CrmProductDO> productList = productService.getProductList(productIds);
        if (CollUtil.isEmpty(productIds) || productList.size() != productIds.size()) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        Map<Long, CrmProductDO> productMap = convertMap(productList, CrmProductDO::getId);
        return convertList(reqVO.getProductItems(), productItem -> {
            // TODO @puhui999：这里可以改成直接 return，不用弄一个 businessProduct 变量哈；
            CrmBusinessProductDO businessProduct = BeanUtils.toBean(productMap.get(productItem.getId()), CrmBusinessProductDO.class);
            businessProduct.setId(null).setBusinessId(reqVO.getBusinessId()).setProductId(productItem.getId())
                    .setCount(productItem.getCount()).setDiscountPercent(productItem.getDiscountPercent()).setTotalPrice(calculator(businessProduct));
            return businessProduct;
        });
    }

    /**
     * 计算商品总价
     *
     * @param businessProduct 关联商品
     * @return 商品总价
     */
    // TODO @puhui999：这个逻辑的计算，是不是可以封装到 calculateRatePriceFloor 里；
    private Integer calculator(CrmBusinessProductDO businessProduct) {
        int price = businessProduct.getPrice() * businessProduct.getCount();
        if (businessProduct.getDiscountPercent() == null) {
            return price;
        }
        return MoneyUtils.calculateRatePriceFloor(price, (double) (businessProduct.getDiscountPercent() / 100));
    }

    /**
     * 校验关联数据是否存在
     *
     * @param reqVO 请求
     */
    private void validateRelationDataExists(CrmContractSaveReqVO reqVO) {
        // 1. 校验客户
        if (reqVO.getCustomerId() != null && customerService.getCustomer(reqVO.getCustomerId()) == null) {
            throw exception(CUSTOMER_NOT_EXISTS);
        }
        // 2. 校验负责人
        if (reqVO.getOwnerUserId() != null && adminUserApi.getUser(reqVO.getOwnerUserId()) == null) {
            throw exception(USER_NOT_EXISTS);
        }
        // 3. 如果有关联商机，则需要校验存在
        if (reqVO.getBusinessId() != null && businessService.getBusiness(reqVO.getBusinessId()) == null) {
            throw exception(BUSINESS_NOT_EXISTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CONTRACT_TYPE, subType = CRM_CONTRACT_DELETE_SUB_TYPE, bizNo = "{{#id}}",
            success = CRM_CONTRACT_DELETE_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTRACT, bizId = "#id", level = CrmPermissionLevelEnum.OWNER)
    public void deleteContract(Long id) {
        // TODO @合同待定：如果被 CrmReceivableDO 所使用，则不允许删除
        // 校验存在
        CrmContractDO contract = validateContractExists(id);
        // 删除
        contractMapper.deleteById(id);
        // 删除数据权限
        crmPermissionService.deletePermission(CrmBizTypeEnum.CRM_CONTRACT.getType(), id);

        // 记录操作日志上下文
        LogRecordContext.putVariable("contractName", contract.getName());
    }

    private CrmContractDO validateContractExists(Long id) {
        CrmContractDO contract = contractMapper.selectById(id);
        if (contract == null) {
            throw exception(CONTRACT_NOT_EXISTS);
        }
        return contract;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CONTRACT_TYPE, subType = CRM_CONTRACT_TRANSFER_SUB_TYPE, bizNo = "{{#reqVO.id}}",
            success = CRM_CONTRACT_TRANSFER_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTRACT, bizId = "#reqVO.id", level = CrmPermissionLevelEnum.OWNER)
    public void transferContract(CrmContractTransferReqVO reqVO, Long userId) {
        // 1. 校验合同是否存在
        CrmContractDO contract = validateContractExists(reqVO.getId());

        // 2.1 数据权限转移
        crmPermissionService.transferPermission(
                CrmContractConvert.INSTANCE.convert(reqVO, userId).setBizType(CrmBizTypeEnum.CRM_CONTRACT.getType()));
        // 2.2 设置负责人
        contractMapper.updateOwnerUserIdById(reqVO.getId(), reqVO.getNewOwnerUserId());

        // 3. 记录转移日志
        LogRecordContext.putVariable("contract", contract);
    }

    @Override
    public void updateContractFollowUp(CrmUpdateFollowUpReqBO contractUpdateFollowUpReqBO) {
        contractMapper.updateById(BeanUtils.toBean(contractUpdateFollowUpReqBO, CrmContractDO.class).setId(contractUpdateFollowUpReqBO.getBizId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleApprove(Long id, Long userId) {
        // TODO @puhui999：需要做状态检查

        // 创建合同审批流程实例
        String processInstanceId = bpmProcessInstanceApi.createProcessInstance(userId, new BpmProcessInstanceCreateReqDTO()
                .setProcessDefinitionKey(CONTRACT_APPROVE).setBusinessKey(String.valueOf(id)));

        // 更新合同工作流编号
        contractMapper.updateById(new CrmContractDO().setId(id).setProcessInstanceId(processInstanceId)
                .setAuditStatus(CrmAuditStatusEnum.PROCESS.getStatus()));
    }

    //======================= 查询相关 =======================

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTRACT, bizId = "#id", level = CrmPermissionLevelEnum.READ)
    public CrmContractDO getContract(Long id) {
        return contractMapper.selectById(id);
    }

    @Override
    public List<CrmContractDO> getContractList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return contractMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<CrmContractDO> getContractPage(CrmContractPageReqVO pageReqVO, Long userId) {
        return contractMapper.selectPage(pageReqVO, userId);
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#pageReqVO.customerId", level = CrmPermissionLevelEnum.READ)
    public PageResult<CrmContractDO> getContractPageByCustomerId(CrmContractPageReqVO pageReqVO) {
        return contractMapper.selectPageByCustomerId(pageReqVO);
    }

    @Override
    public Long getContractCountByContactId(Long contactId) {
        return contractMapper.selectCountByContactId(contactId);
    }

    @Override
    public Long getContractCountByCustomerId(Long customerId) {
        return contractMapper.selectCount(CrmContractDO::getCustomerId, customerId);
    }

    @Override
    public Long selectCountByBusinessId(Long businessId) {
        return contractMapper.selectCountByBusinessId(businessId);
    }
    // TODO @合同待定：需要新增一个 ContractConfigDO 表，合同配置，重点是到期提醒；
}
