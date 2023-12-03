package cn.iocoder.yudao.module.crm.convert.contactbusinessslink;

import cn.iocoder.yudao.module.crm.controller.admin.contactbusinesslink.vo.CrmContactBusinessLinkSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contactbusinesslink.CrmContactBusinessLinkDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface CrmContactBusinessLinkConvert {
    CrmContactBusinessLinkConvert INSTANCE = Mappers.getMapper(CrmContactBusinessLinkConvert.class);
    CrmContactBusinessLinkDO convert(CrmContactBusinessLinkSaveReqVO bean);
    List<CrmContactBusinessLinkDO> convert(List<CrmContactBusinessLinkSaveReqVO> bean);
}
