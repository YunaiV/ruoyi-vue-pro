package cn.iocoder.yudao.module.promotion.convert.coupon;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.coupon.vo.coupon.CouponPageItemRespVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.coupon.CouponDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 优惠劵 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface CouponConvert {

    CouponConvert INSTANCE = Mappers.getMapper(CouponConvert.class);

    PageResult<CouponPageItemRespVO> convertPage(PageResult<CouponDO> page);

}
