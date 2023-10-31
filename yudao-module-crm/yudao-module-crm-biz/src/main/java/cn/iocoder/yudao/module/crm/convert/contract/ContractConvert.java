package cn.iocoder.yudao.module.crm.convert.contract;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmTransferPermissionReqBO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

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

    @Mappings({
            @Mapping(target = "bizId", source = "reqVO.id"),
            @Mapping(target = "newOwnerUserId", source = "reqVO.id")
    })
    CrmTransferPermissionReqBO convert(CrmTransferContractReqVO reqVO, Long userId);

}
