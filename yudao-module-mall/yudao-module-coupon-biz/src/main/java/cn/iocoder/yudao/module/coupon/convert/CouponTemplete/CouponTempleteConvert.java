package cn.iocoder.yudao.module.coupon.convert.CouponTemplete;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.coupon.controller.admin.templete.vo.CouponTempleteCreateReqVO;
import cn.iocoder.yudao.module.coupon.controller.admin.templete.vo.CouponTempleteExcelVO;
import cn.iocoder.yudao.module.coupon.controller.admin.templete.vo.CouponTempleteRespVO;
import cn.iocoder.yudao.module.coupon.controller.admin.templete.vo.CouponTempleteUpdateReqVO;
import cn.iocoder.yudao.module.coupon.dal.dataobject.CouponTemplete.CouponTempleteDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


/**
 * 优惠券模板 Convert
 *
 * @author wxr
 */
@Mapper
public interface CouponTempleteConvert {

    CouponTempleteConvert INSTANCE = Mappers.getMapper(CouponTempleteConvert.class);

    CouponTempleteDO convert(CouponTempleteCreateReqVO bean);

    CouponTempleteDO convert(CouponTempleteUpdateReqVO bean);

    CouponTempleteRespVO convert(CouponTempleteDO bean);

    List<CouponTempleteRespVO> convertList(List<CouponTempleteDO> list);

    PageResult<CouponTempleteRespVO> convertPage(PageResult<CouponTempleteDO> page);

    List<CouponTempleteExcelVO> convertList02(List<CouponTempleteDO> list);

}
