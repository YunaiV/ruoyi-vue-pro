package cn.iocoder.yudao.module.mp.convert.user;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.controller.admin.user.vo.MpUserRespVO;
import cn.iocoder.yudao.module.mp.dal.dataobject.user.MpUserDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface MpUserConvert {

    MpUserConvert INSTANCE = Mappers.getMapper(MpUserConvert.class);

    MpUserRespVO convert(MpUserDO bean);

    List<MpUserRespVO> convertList(List<MpUserDO> list);

    PageResult<MpUserRespVO> convertPage(PageResult<MpUserDO> page);

}
