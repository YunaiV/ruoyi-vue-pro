package cn.iocoder.yudao.module.crm.convert.clue;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.clue.vo.*;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.CrmContactSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.clue.CrmClueDO;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionTransferReqBO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 线索 Convert
 *
 * @author Wanwan
 */
@Mapper
public interface CrmClueConvert {

    CrmClueConvert INSTANCE = Mappers.getMapper(CrmClueConvert.class);

    CrmClueDO convert(CrmClueSaveReqVO bean);

    CrmClueRespVO convert(CrmClueDO bean);

    PageResult<CrmClueRespVO> convertPage(PageResult<CrmClueDO> page);

    List<CrmClueExcelVO> convertList02(List<CrmClueDO> list);

    @Mapping(target = "bizId", source = "reqVO.id")
    CrmPermissionTransferReqBO convert(CrmClueTransferReqVO reqVO, Long userId);

}
