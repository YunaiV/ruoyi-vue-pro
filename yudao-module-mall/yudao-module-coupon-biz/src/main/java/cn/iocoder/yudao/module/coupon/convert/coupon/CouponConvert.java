package cn.iocoder.yudao.module.coupon.convert.coupon;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.coupon.controller.admin.coupon.vo.*;
import cn.iocoder.yudao.module.coupon.dal.dataobject.coupon.CouponDO;

/**
 * 优惠券 Convert
 *
 * @author wxr
 */
@Mapper
public interface CouponConvert {

    CouponConvert INSTANCE = Mappers.getMapper(CouponConvert.class);

    CouponDO convert(CouponCreateReqVO bean);

    CouponDO convert(CouponUpdateReqVO bean);

    CouponRespVO convert(CouponDO bean);

    List<CouponRespVO> convertList(List<CouponDO> list);

    PageResult<CouponRespVO> convertPage(PageResult<CouponDO> page);

    List<CouponExcelVO> convertList02(List<CouponDO> list);

}
