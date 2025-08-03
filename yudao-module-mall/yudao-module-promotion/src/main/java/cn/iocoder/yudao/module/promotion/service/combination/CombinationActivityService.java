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
import java.util.Collections;
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
     * 关闭拼团活动
     *
     * @param id 拼团活动编号
     */
    void closeCombinationActivityById(Long id);

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
     * @param activityId 拼团活动 id
     * @return 拼团活动的商品列表
     */
    default List<CombinationProductDO> getCombinationProductsByActivityId(Long activityId) {
        return getCombinationProductListByActivityIds(Collections.singletonList(activityId));
    }

    /**
     * 获得拼团活动商品列表
     *
     * @param activityIds 拼团活动 ids
     * @return 拼团活动的商品列表
     */
    List<CombinationProductDO> getCombinationProductListByActivityIds(Collection<Long> activityIds);

    /**
     * 获得拼团活动列表
     *
     * @param ids 拼团活动 ids
     * @return 拼团活动的列表
     */
    List<CombinationActivityDO> getCombinationActivityListByIds(Collection<Long> ids);

    /**
     * 获取正在进行的活动分页数据
     *
     * @param pageParam 分页请求
     * @return 拼团活动分页
     */
    PageResult<CombinationActivityDO> getCombinationActivityPage(PageParam pageParam);

    /**
     * 获取指定活动、指定 SKU 编号的商品
     *
     * @param activityId 活动编号
     * @param skuId      SKU 编号
     * @return 活动商品信息
     */
    CombinationProductDO selectByActivityIdAndSkuId(Long activityId, Long skuId);

    /**
     * 获得 SPU 进行中的拼团活动
     *
     * @param spuId SPU 编号数组
     * @return 拼团活动
     */
    CombinationActivityDO getMatchCombinationActivityBySpuId(Long spuId);

}
