package cn.iocoder.yudao.module.point.convert.pointrecord;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.point.controller.admin.pointrecord.vo.*;
import cn.iocoder.yudao.module.point.dal.dataobject.pointrecord.PointRecordDO;

/**
 * 用户积分记录 Convert
 *
 * @author QingX
 */
@Mapper
public interface PointRecordConvert {

    PointRecordConvert INSTANCE = Mappers.getMapper(PointRecordConvert.class);

    PointRecordDO convert(PointRecordCreateReqVO bean);

    PointRecordDO convert(PointRecordUpdateReqVO bean);

    PointRecordRespVO convert(PointRecordDO bean);

    List<PointRecordRespVO> convertList(List<PointRecordDO> list);

    PageResult<PointRecordRespVO> convertPage(PageResult<PointRecordDO> page);

    List<PointRecordExcelVO> convertList02(List<PointRecordDO> list);

}
