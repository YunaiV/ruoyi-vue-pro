package cn.iocoder.yudao.module.promotion.service.price;

import cn.iocoder.yudao.module.promotion.api.price.dto.CouponMeetRespDTO;
import cn.iocoder.yudao.module.promotion.api.price.dto.PriceCalculateReqDTO;
import cn.iocoder.yudao.module.promotion.api.price.dto.PriceCalculateRespDTO;

import java.util.List;

/**
 * 价格计算 Service 接口
 *
 * @author 芋道源码
 */
public interface PriceService {

    /**
     * 计算商品的价格
     *
     * @param calculateReqDTO 价格请求
     * @return 价格响应
     */
    PriceCalculateRespDTO calculatePrice(PriceCalculateReqDTO calculateReqDTO);

    /**
     * 获得优惠劵的匹配信息列表
     *
     * @param calculateReqDTO 价格请求
     * @return 价格响应
     */
    List<CouponMeetRespDTO> getMeetCouponList(PriceCalculateReqDTO calculateReqDTO);

}
