package cn.iocoder.yudao.module.infra.convert.config;

import cn.iocoder.yudao.coreservice.modules.infra.dal.dataobject.config.InfConfigDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.infra.controller.admin.config.vo.ConfigCreateReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.config.vo.ConfigExcelVO;
import cn.iocoder.yudao.module.infra.controller.admin.config.vo.ConfigRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.config.vo.ConfigUpdateReqVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface InfConfigConvert {

    InfConfigConvert INSTANCE = Mappers.getMapper(InfConfigConvert.class);

    PageResult<ConfigRespVO> convertPage(PageResult<InfConfigDO> page);

    ConfigRespVO convert(InfConfigDO bean);

    InfConfigDO convert(ConfigCreateReqVO bean);

    InfConfigDO convert(ConfigUpdateReqVO bean);

    List<ConfigExcelVO> convertList(List<InfConfigDO> list);

}
