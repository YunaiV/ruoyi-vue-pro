package cn.iocoder.yudao.module.crm.convert.contract;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.ContractDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;

/**
 * 合同 Convert
 *
 * @author dhb52
 */
@Mapper
public interface ContractConvert {

    ContractConvert INSTANCE = Mappers.getMapper(ContractConvert.class);

    ContractDO convert(ContractCreateReqVO bean);

    ContractDO convert(ContractUpdateReqVO bean);

    ContractRespVO convert(ContractDO bean);

    List<ContractRespVO> convertList(List<ContractDO> list);

    PageResult<ContractRespVO> convertPage(PageResult<ContractDO> page);

    List<ContractExcelVO> convertList02(List<ContractDO> list);

    // TODO @puhui999：参考 CrmBusinessConvert 的修改建议
    default ContractDO convert(ContractDO contract, CrmContractTransferReqVO reqVO, Long userId) {
        Set<Long> rwUserIds = contract.getRwUserIds();
        rwUserIds.removeIf(item -> ObjUtil.equal(item, userId)); // 移除老负责人
        rwUserIds.add(reqVO.getOwnerUserId()); // 读写权限加入新的负人
        return new ContractDO().setId(contract.getId()).setOwnerUserId(reqVO.getOwnerUserId()) // 设置新负责人
                .setRwUserIds(rwUserIds);
    }

}
