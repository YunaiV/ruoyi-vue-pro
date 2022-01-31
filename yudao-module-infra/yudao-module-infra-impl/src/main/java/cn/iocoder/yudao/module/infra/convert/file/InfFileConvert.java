package cn.iocoder.yudao.module.infra.convert.file;

import cn.iocoder.yudao.coreservice.modules.infra.controller.file.vo.InfFileRespVO;
import cn.iocoder.yudao.coreservice.modules.infra.dal.dataobject.file.InfFileDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface InfFileConvert {

    InfFileConvert INSTANCE = Mappers.getMapper(InfFileConvert.class);

    InfFileRespVO convert(InfFileDO bean);

    PageResult<InfFileRespVO> convertPage(PageResult<InfFileDO> page);

}
