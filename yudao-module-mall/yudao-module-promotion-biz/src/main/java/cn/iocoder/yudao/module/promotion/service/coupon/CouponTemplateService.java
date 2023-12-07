package cn.iocoder.yudao.module.promotion.service.coupon;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.coupon.vo.template.CouponTemplateCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.coupon.vo.template.CouponTemplatePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.coupon.vo.template.CouponTemplateUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.coupon.CouponTemplateDO;
import cn.iocoder.yudao.module.promotion.enums.coupon.CouponTakeTypeEnum;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 优惠劵模板 Service 接口
 *
 * @author 芋道源码
 */
public interface CouponTemplateService {

    /**
     * 创建优惠劵模板
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCouponTemplate(@Valid CouponTemplateCreateReqVO createReqVO);

    /**
     * 更新优惠劵模板
     *
     * @param updateReqVO 更新信息
     */
    void updateCouponTemplate(@Valid CouponTemplateUpdateReqVO updateReqVO);

    /**
     * 更新优惠劵模板的状态
     *
     * @param id     编号
     * @param status 状态
     */
    void updateCouponTemplateStatus(Long id, Integer status);

    /**
     * 删除优惠劵模板
     *
     * @param id 编号
     */
    void deleteCouponTemplate(Long id);

    /**
     * 获得优惠劵模板
     *
     * @param id 编号
     * @return 优惠劵模板
     */
    CouponTemplateDO getCouponTemplate(Long id);

    /**
     * 获得优惠劵模板分页
     *
     * @param pageReqVO 分页查询
     * @return 优惠劵模板分页
     */
    PageResult<CouponTemplateDO> getCouponTemplatePage(CouponTemplatePageReqVO pageReqVO);

    /**
     * 更新优惠劵模板的领取数量
     *
     * @param id        优惠劵模板编号
     * @param incrCount 增加数量
     */
    void updateCouponTemplateTakeCount(Long id, int incrCount);

    /**
     * 获得指定领取方式的优惠券模板
     *
     * @param takeType 领取方式
     * @return 优惠券模板列表
     */
    List<CouponTemplateDO> getCouponTemplateListByTakeType(CouponTakeTypeEnum takeType);

    /**
     * 获得优惠券模板列表
     *
     * @param canTakeTypes      可领取的类型列表
     * @param productScope      商品使用范围类型
     * @param productScopeValue 商品使用范围编号
     * @param count             查询数量
     * @return 优惠券模板列表
     */
    List<CouponTemplateDO> getCouponTemplateList(List<Integer> canTakeTypes, Integer productScope,
                                                 Long productScopeValue, Integer count);

    /**
     * 获得优惠券模版列表
     *
     * @param ids 优惠券模版编号
     * @return 优惠券模版列表
     */
    List<CouponTemplateDO> getCouponTemplateList(Collection<Long> ids);

}
