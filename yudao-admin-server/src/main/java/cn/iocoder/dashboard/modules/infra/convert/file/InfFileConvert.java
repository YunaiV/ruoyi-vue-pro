package cn.iocoder.dashboard.modules.infra.convert.file;

import cn.iocoder.yudao.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.infra.controller.file.vo.InfFileRespVO;
import cn.iocoder.dashboard.modules.infra.dal.dataobject.file.InfFileDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface InfFileConvert {

    InfFileConvert INSTANCE = Mappers.getMapper(InfFileConvert.class);

    InfFileRespVO convert(InfFileDO bean);

    PageResult<InfFileRespVO> convertPage(PageResult<InfFileDO> page);

}
