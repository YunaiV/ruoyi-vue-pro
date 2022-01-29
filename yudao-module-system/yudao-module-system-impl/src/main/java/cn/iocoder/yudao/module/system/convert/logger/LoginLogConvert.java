package cn.iocoder.yudao.module.system.convert.logger;

import cn.iocoder.yudao.module.system.controller.admin.logger.vo.loginlog.LoginLogExcelVO;
import cn.iocoder.yudao.module.system.controller.admin.logger.vo.loginlog.LoginLogRespVO;
import cn.iocoder.yudao.module.system.dal.dataobject.logger.SysLoginLogDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface LoginLogConvert {

    LoginLogConvert INSTANCE = Mappers.getMapper(LoginLogConvert.class);

    PageResult<LoginLogRespVO> convertPage(PageResult<SysLoginLogDO> page);

    List<LoginLogExcelVO> convertList(List<SysLoginLogDO> list);

}
