package cn.iocoder.yudao.module.promotion.dal.mysql.coupon;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.controller.admin.coupon.vo.coupon.CouponPageReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.coupon.CouponDO;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionProductScopeEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.yulichang.toolkit.MPJWrappers;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 优惠劵 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CouponMapper extends BaseMapperX<CouponDO> {

    default PageResult<CouponDO> selectPage(CouponPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CouponDO>()
                .eqIfPresent(CouponDO::getTemplateId, reqVO.getTemplateId())
                .eqIfPresent(CouponDO::getStatus, reqVO.getStatus())
                .inIfPresent(CouponDO::getUserId, reqVO.getUserIds())
                .betweenIfPresent(CouponDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CouponDO::getId));
    }

    default List<CouponDO> selectListByUserIdAndStatus(Long userId, Integer status) {
        return selectList(new LambdaQueryWrapperX<CouponDO>()
                .eq(CouponDO::getUserId, userId).eq(CouponDO::getStatus, status));
    }

    default CouponDO selectByIdAndUserId(Long id, Long userId) {
        return selectOne(new LambdaQueryWrapperX<CouponDO>()
                .eq(CouponDO::getId, id).eq(CouponDO::getUserId, userId));
    }

    default int delete(Long id, Collection<Integer> whereStatuses) {
        return update(null, new LambdaUpdateWrapper<CouponDO>()
                .eq(CouponDO::getId, id).in(CouponDO::getStatus, whereStatuses)
                .set(CouponDO::getDeleted, 1));
    }

    default int updateByIdAndStatus(Long id, Integer status, CouponDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<CouponDO>()
                .eq(CouponDO::getId, id).eq(CouponDO::getStatus, status));
    }

    default Long selectCountByUserIdAndStatus(Long userId, Integer status) {
        return selectCount(new LambdaQueryWrapperX<CouponDO>()
                .eq(CouponDO::getUserId, userId)
                .eq(CouponDO::getStatus, status));
    }

    default List<CouponDO> selectListByTemplateIdAndUserId(Long templateId, Collection<Long> userIds) {
        return selectList(new LambdaQueryWrapperX<CouponDO>()
                .eq(CouponDO::getTemplateId, templateId)
                .in(CouponDO::getUserId, userIds)
        );
    }

    default Map<Long, Integer> selectCountByUserIdAndTemplateIdIn(Long userId, Collection<Long> templateIds) {
        String templateIdAlias = "templateId";
        String countAlias = "count";
        List<Map<String, Object>> list = selectMaps(MPJWrappers.lambdaJoin(CouponDO.class)
                .selectAs(CouponDO::getTemplateId, templateIdAlias)
                .selectCount(CouponDO::getId, countAlias)
                .eq(CouponDO::getUserId, userId)
                .in(CouponDO::getTemplateId, templateIds)
                .groupBy(CouponDO::getTemplateId));
        return convertMap(list, map -> MapUtil.getLong(map, templateIdAlias), map -> MapUtil.getInt(map, countAlias));
    }

    default List<CouponDO> selectListByUserIdAndStatusAndUsePriceLeAndProductScope(
            Long userId, Integer status) {
        List<CouponDO> couponDOS = selectList(new LambdaQueryWrapperX<CouponDO>()
                .eq(CouponDO::getUserId, userId)
                .eq(CouponDO::getStatus, status)
        );
        return couponDOS;
    }

    default List<CouponDO> selectListByStatusAndValidEndTimeLe(Integer status, LocalDateTime validEndTime) {
        return selectList(new LambdaQueryWrapperX<CouponDO>()
                .eq(CouponDO::getStatus, status)
                .le(CouponDO::getValidEndTime, validEndTime)
        );
    }

}
