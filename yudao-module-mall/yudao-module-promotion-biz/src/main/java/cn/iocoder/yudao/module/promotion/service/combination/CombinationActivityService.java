package cn.iocoder.yudao.module.promotion.service.combination;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationProductDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 拼团活动 Service 接口
 *
 * @author HUIHUI
 */
public interface CombinationActivityService {

    /**
     * 创建拼团活动
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCombinationActivity(@Valid CombinationActivityCreateReqVO createReqVO);

    /**
     * 更新拼团活动
     *
     * @param updateReqVO 更新信息
     */
    void updateCombinationActivity(@Valid CombinationActivityUpdateReqVO updateReqVO);

    /**
     * 删除拼团活动
     *
     * @param id 编号
     */
    void deleteCombinationActivity(Long id);

    /**
     * 校验拼团活动是否存在
     *
     * @param id 编号
     * @return 拼团活动
     */
    CombinationActivityDO validateCombinationActivityExists(Long id);

    /**
     * 获得拼团活动
     *
     * @param id 编号
     * @return 拼团活动
     */
    CombinationActivityDO getCombinationActivity(Long id);

    /**
     * 获得拼团活动分页
     *
     * @param pageReqVO 分页查询
     * @return 拼团活动分页
     */
    PageResult<CombinationActivityDO> getCombinationActivityPage(CombinationActivityPageReqVO pageReqVO);

    /**
     * 获得拼团活动商品列表
     *
     * @param activityIds 拼团活动 ids
     * @return 拼团活动的商品列表
     */
    List<CombinationProductDO> getCombinationProductsByActivityIds(Collection<Long> activityIds);

    /**
     * 校验是否满足拼团条件
     * 如果不满足，会抛出异常
     *
     * @param activityId 活动编号
     * @param userId     用户编号
     * @param skuId      sku 编号
     * @param count      数量
     */
    void validateCombination(Long activityId, Long userId, Long skuId, Integer count);

    /**
     * 获取 APP 端活动展示数据
     *
     * @param count 需要的数量
     * @return 活动列表
     */
    List<CombinationActivityDO> getCombinationActivityAppList(Integer count);

    /**
     * 获取 APP 端活动分页数据
     *
     * @param pageParam 分页参数
     * @return 活动分页数据
     */
    PageResult<CombinationActivityDO> getCombinationActivityAppPage(PageParam pageParam);

}
