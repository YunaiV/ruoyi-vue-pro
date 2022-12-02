package cn.iocoder.yudao.module.promotion.convert.seckill.seckilltime;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.time.SeckillTimeCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.time.SeckillTimeRespVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.time.SeckillTimeUpdateReqVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckilltime.SeckillTimeDO;

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
