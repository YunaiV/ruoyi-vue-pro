package cn.iocoder.yudao.module.promotion.convert.seckilltime;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.promotion.controller.admin.seckilltime.vo.*;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckilltime.SeckillTimeDO;

/**
 * 秒杀时段 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface SeckillTimeConvert {

    SeckillTimeConvert INSTANCE = Mappers.getMapper(SeckillTimeConvert.class);

    SeckillTimeDO convert(SeckillTimeCreateReqVO bean);

    SeckillTimeDO convert(SeckillTimeUpdateReqVO bean);

    SeckillTimeRespVO convert(SeckillTimeDO bean);

    List<SeckillTimeRespVO> convertList(List<SeckillTimeDO> list);

    PageResult<SeckillTimeRespVO> convertPage(PageResult<SeckillTimeDO> page);

}
