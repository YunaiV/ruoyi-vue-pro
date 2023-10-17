package cn.iocoder.yudao.module.crm.service.contract;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.ContractCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.ContractExportReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.ContractPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.ContractUpdateReqVO;
import cn.iocoder.yudao.module.crm.convert.contract.ContractConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.crm.dal.mysql.contract.ContractMapper;
import org.springframework.stereotype.Service;
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

    private void validateContractExists(Long id) {
        if (contractMapper.selectById(id) == null) {
            throw exception(CONTRACT_NOT_EXISTS);
        }
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

}
