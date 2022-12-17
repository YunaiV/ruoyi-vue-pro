package cn.iocoder.yudao.module.system.convert.notify;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.log.NotifyLogBaseVO;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyMessageDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * <p>
 *
 * </p>
 *
 * @author LuoWenFeng
 */
@Mapper
public interface NotifyLogConvert {

    NotifyLogConvert INSTANCE = Mappers.getMapper(NotifyLogConvert.class);

    PageResult<NotifyLogBaseVO> convertPage(PageResult<NotifyMessageDO> page);

}
