package cn.iocoder.yudao.module.system.convert.notify;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.system.controller.admin.notify.vo.message.NotifyMessageCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.message.NotifyMessageRespVO;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.message.NotifyMessageUpdateReqVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyMessageDO;

/**
 * 站内信 Convert
 *
 * @author xrcoder
 */
@Mapper
public interface NotifyMessageConvert {

    NotifyMessageConvert INSTANCE = Mappers.getMapper(NotifyMessageConvert.class);

    NotifyMessageDO convert(NotifyMessageCreateReqVO bean);

    NotifyMessageDO convert(NotifyMessageUpdateReqVO bean);

    NotifyMessageRespVO convert(NotifyMessageDO bean);

    List<NotifyMessageRespVO> convertList(List<NotifyMessageDO> list);

    PageResult<NotifyMessageRespVO> convertPage(PageResult<NotifyMessageDO> page);

}
