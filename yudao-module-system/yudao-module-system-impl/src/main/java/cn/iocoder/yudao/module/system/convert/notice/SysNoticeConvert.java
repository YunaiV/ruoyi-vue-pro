package cn.iocoder.yudao.module.system.convert.notice;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.notice.vo.SysNoticeCreateReqVO;
import cn.iocoder.yudao.module.system.controller.notice.vo.SysNoticeRespVO;
import cn.iocoder.yudao.module.system.controller.notice.vo.SysNoticeUpdateReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.notice.SysNoticeDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SysNoticeConvert {

    SysNoticeConvert INSTANCE = Mappers.getMapper(SysNoticeConvert.class);

    PageResult<SysNoticeRespVO> convertPage(PageResult<SysNoticeDO> page);

    SysNoticeRespVO convert(SysNoticeDO bean);

    SysNoticeDO convert(SysNoticeUpdateReqVO bean);

    SysNoticeDO convert(SysNoticeCreateReqVO bean);

}
