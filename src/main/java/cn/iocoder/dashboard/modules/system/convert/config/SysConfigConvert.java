package cn.iocoder.dashboard.modules.system.convert.config;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.config.vo.SysConfigCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.config.vo.SysConfigExcelVO;
import cn.iocoder.dashboard.modules.system.controller.config.vo.SysConfigRespVO;
import cn.iocoder.dashboard.modules.system.controller.config.vo.SysConfigUpdateReqVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.config.SysConfigDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SysConfigConvert {

    SysConfigConvert INSTANCE = Mappers.getMapper(SysConfigConvert.class);

    PageResult<SysConfigRespVO> convertPage(PageResult<SysConfigDO> page);

    SysConfigRespVO convert(SysConfigDO bean);

    SysConfigDO convert(SysConfigCreateReqVO bean);

    SysConfigDO convert(SysConfigUpdateReqVO bean);

    List<SysConfigExcelVO> convertList(List<SysConfigDO> list);

}
