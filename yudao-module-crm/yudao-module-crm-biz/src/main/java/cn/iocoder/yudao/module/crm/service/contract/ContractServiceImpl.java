package cn.iocoder.yudao.module.crm.service.contract;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.*;
import cn.iocoder.yudao.module.crm.convert.contract.ContractConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.crm.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.crm.framework.utils.AuthUtil.isReadAndWrite;

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
    private AdminUserApi adminUserApi;

    @Override
    public Long createContract(ContractCreateReqVO createReqVO) {
        // 插入
        ContractDO contract = ContractConvert.INSTANCE.convert(createReqVO);
        contractMapper.insert(contract);
        // 返回
        return contract.getId();
    }

    @Override
    public void updateContract(ContractUpdateReqVO updateReqVO) {
        // 校验存在
        validateContractExists(updateReqVO.getId());
        // 更新
        ContractDO updateObj = ContractConvert.INSTANCE.convert(updateReqVO);
        contractMapper.updateById(updateObj);
    }

    @Override
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
    public void contractTransfer(CrmContractTransferReqVO reqVO, Long userId) {
        // 1. 校验合同是否存在
        ContractDO contract = validateContractExists(reqVO.getId());
        // 1.2. 校验用户是否拥有读写权限
        if (!isReadAndWrite(contract.getRwUserIds(), userId)) {
            throw exception(CONTRACT_TRANSFER_FAIL_PERMISSION_DENIED);
        }
        // 2. 校验新负责人是否存在
        AdminUserRespDTO user = adminUserApi.getUser(reqVO.getOwnerUserId());
        if (user == null) {
            throw exception(CONTRACT_TRANSFER_FAIL_OWNER_USER_NOT_EXISTS);
        }

        // 3. 更新新的负责人
        ContractDO updateContract = ContractConvert.INSTANCE.convert(contract, reqVO, userId);
        contractMapper.updateById(updateContract);

        // 4. TODO 记录合同转移日志

    }

}
