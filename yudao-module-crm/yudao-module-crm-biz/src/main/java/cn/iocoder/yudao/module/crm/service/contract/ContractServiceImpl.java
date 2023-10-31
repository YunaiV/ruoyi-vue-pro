package cn.iocoder.yudao.module.crm.service.contract;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.*;
import cn.iocoder.yudao.module.crm.convert.contract.ContractConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.crm.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.module.crm.framework.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.framework.enums.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.framework.enums.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionCreateBO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CONTRACT_NOT_EXISTS;

/**
 * 合同 Service 实现类
 *
 * @author dhb52
 */
@Service
@Validated
public class ContractServiceImpl implements ContractService {

    @Resource
    private ContractMapper contractMapper;

    @Resource
    private CrmPermissionService crmPermissionService;

    @Override
    public Long createContract(ContractCreateReqVO createReqVO, Long userId) {
        // 插入
        ContractDO contract = ContractConvert.INSTANCE.convert(createReqVO);
        contractMapper.insert(contract);

        // 创建数据权限
        crmPermissionService.createCrmPermission(new CrmPermissionCreateBO().setCrmType(CrmBizTypeEnum.CRM_CONTRACT.getType())
                .setCrmDataId(contract.getId()).setOwnerUserId(userId)); // 设置当前操作的人为负责人

        // 返回
        return contract.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTRACT, permissionLevel = CrmPermissionLevelEnum.WRITE)
    public void updateContract(ContractUpdateReqVO updateReqVO) {
        // 校验存在
        validateContractExists(updateReqVO.getId());
        // 更新
        ContractDO updateObj = ContractConvert.INSTANCE.convert(updateReqVO);
        contractMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTRACT, permissionLevel = CrmPermissionLevelEnum.WRITE)
    public void deleteContract(Long id) {
        // 校验存在
        validateContractExists(id);
        // 删除
        contractMapper.deleteById(id);
    }

    private ContractDO validateContractExists(Long id) {
        ContractDO contract = contractMapper.selectById(id);
        if (contract == null) {
            throw exception(CONTRACT_NOT_EXISTS);
        }
        return contract;
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CONTRACT, permissionLevel = CrmPermissionLevelEnum.READ)
    public ContractDO getContract(Long id) {
        return contractMapper.selectById(id);
    }

    @Override
    public List<ContractDO> getContractList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return contractMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<ContractDO> getContractPage(ContractPageReqVO pageReqVO) {
        return contractMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ContractDO> getContractList(ContractExportReqVO exportReqVO) {
        return contractMapper.selectList(exportReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferContract(CrmTransferContractReqVO reqVO, Long userId) {
        // 1 校验合同是否存在
        validateContractExists(reqVO.getId());

        // 2. 数据权限转移
        crmPermissionService.transferCrmPermission(
                ContractConvert.INSTANCE.convert(reqVO, userId).setBizType(CrmBizTypeEnum.CRM_CONTRACT.getType()));

    }

}
