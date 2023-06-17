package cn.iocoder.yudao.module.point.convert.pointconfig;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.point.controller.admin.pointconfig.vo.*;
import cn.iocoder.yudao.module.point.dal.dataobject.pointconfig.PointConfigDO;

/**
 * 积分设置 Convert
 *
 * @author QingX
 */
@Mapper
public interface PointConfigConvert {

    PointConfigConvert INSTANCE = Mappers.getMapper(PointConfigConvert.class);

    PointConfigDO convert(PointConfigCreateReqVO bean);

    PointConfigDO convert(PointConfigUpdateReqVO bean);

    PointConfigRespVO convert(PointConfigDO bean);

    List<PointConfigRespVO> convertList(List<PointConfigDO> list);

    PageResult<PointConfigRespVO> convertPage(PageResult<PointConfigDO> page);

    List<PointConfigExcelVO> convertList02(List<PointConfigDO> list);

}
