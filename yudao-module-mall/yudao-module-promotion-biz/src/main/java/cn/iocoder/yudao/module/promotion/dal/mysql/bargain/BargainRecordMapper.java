package cn.iocoder.yudao.module.promotion.dal.mysql.bargain;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.recrod.BargainRecordPageReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainRecordDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 砍价记录 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface BargainRecordMapper extends BaseMapperX<BargainRecordDO> {

    default BargainRecordDO selectByIdAndUserId(Long id, Long userId) {
        return selectOne(BargainRecordDO::getId, id,
                BargainRecordDO::getUserId, userId);
    }

    default List<BargainRecordDO> selectListByUserIdAndActivityIdAndStatus(
            Long userId, Long activityId, Integer status) {
        return selectList(new LambdaQueryWrapper<>(BargainRecordDO.class)
                .eq(BargainRecordDO::getUserId, userId)
                .eq(BargainRecordDO::getActivityId, activityId)
                .eq(BargainRecordDO::getStatus, status));
    }

    default BargainRecordDO selectLastByUserIdAndActivityId(Long userId, Long activityId) {
        return selectOne(new LambdaQueryWrapper<>(BargainRecordDO.class)
                .eq(BargainRecordDO::getUserId, userId)
                .eq(BargainRecordDO::getActivityId, activityId)
                .orderByDesc(BargainRecordDO::getId)
                .last("LIMIT 1"));
    }

    default Long selectCountByUserIdAndActivityIdAndStatus(
            Long userId, Long activityId, Integer status) {
        return selectCount(new LambdaQueryWrapper<>(BargainRecordDO.class)
                .eq(BargainRecordDO::getUserId, userId)
                .eq(BargainRecordDO::getActivityId, activityId)
                .eq(BargainRecordDO::getStatus, status));
    }

    default int updateByIdAndBargainPrice(Long id, Integer whereBargainPrice, BargainRecordDO updateObj) {
        return update(updateObj, new LambdaQueryWrapper<>(BargainRecordDO.class)
                .eq(BargainRecordDO::getId, id)
                .eq(BargainRecordDO::getBargainPrice, whereBargainPrice));
    }

    default Map<Long, Integer> selectUserCountByActivityIdsAndStatus(Collection<Long> activityIds, Integer status) {
        // SQL count 查询
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<BargainRecordDO>()
                .select("COUNT(DISTINCT(user_id)) AS userCount, activity_id AS activityId")
                .in("activity_id", activityIds)
                .eq(status != null, "status", status)
                .groupBy("activity_id"));
        if (CollUtil.isEmpty(result)) {
            return Collections.emptyMap();
        }
        // 转换数据
        return CollectionUtils.convertMap(result,
                record -> MapUtil.getLong(record, "activityId"),
                record -> MapUtil.getInt(record, "userCount" ));
    }

    @Select("SELECT COUNT(DISTINCT(user_id)) FROM promotion_bargain_record " +
            "WHERE status = #{status}")
    Integer selectUserCountByStatus(@Param("status") Integer status);

    @Select("SELECT COUNT(DISTINCT(user_id)) FROM promotion_bargain_record " +
            "WHERE activity_id = #{activityId} " +
            "AND status = #{status}")
    Integer selectUserCountByActivityIdAndStatus(@Param("activityId") Long activityId,
                                                 @Param("status") Integer status);

    default PageResult<BargainRecordDO> selectPage(BargainRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BargainRecordDO>()
                .eqIfPresent(BargainRecordDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(BargainRecordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(BargainRecordDO::getId));
    }

    default PageResult<BargainRecordDO> selectBargainRecordPage(Long userId, PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapperX<BargainRecordDO>()
                .eq(BargainRecordDO::getUserId, userId)
                .orderByDesc(BargainRecordDO::getId));
    }

    default List<BargainRecordDO> selectListByStatusAndCount(Integer status, Integer count) {
        return selectList(new LambdaQueryWrapper<>(BargainRecordDO.class)
                .eq(BargainRecordDO::getStatus, status)
                .last("LIMIT " + count));
    }

    /**
     * 更新砍价的订单编号，前提是 orderId 原本是空的
     *
     * @param id 砍价记录编号
     * @param orderId 订单编号
     * @return 更新数量
     */
    default int updateOrderIdById(Long id, Long orderId) {
        return update(new BargainRecordDO().setOrderId(orderId).setEndTime(LocalDateTime.now()),
                new LambdaQueryWrapper<>(BargainRecordDO.class)
                        .eq(BargainRecordDO::getId, id)
                        .isNull(BargainRecordDO::getOrderId));
    }

}
