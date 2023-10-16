package cn.iocoder.yudao.module.promotion.dal.mysql.combination;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.recrod.CombinationRecordReqPageVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationRecordDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 拼团记录 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface CombinationRecordMapper extends BaseMapperX<CombinationRecordDO> {

    default CombinationRecordDO selectByUserIdAndOrderId(Long userId, Long orderId) {
        return selectOne(CombinationRecordDO::getUserId, userId,
                CombinationRecordDO::getOrderId, orderId);
    }

    /**
     * 查询拼团记录
     *
     * @param headId 团长编号
     * @return 拼团记录
     */
    default CombinationRecordDO selectByHeadId(Long headId, Integer status) {
        return selectOne(new LambdaQueryWrapperX<CombinationRecordDO>()
                .eq(CombinationRecordDO::getId, headId)
                .eq(CombinationRecordDO::getStatus, status));
    }

    /**
     * 查询拼团记录
     *
     * @param userId     用户 id
     * @param activityId 活动 id
     * @return 拼团记录
     */
    default List<CombinationRecordDO> selectListByUserIdAndActivityId(Long userId, Long activityId) {
        return selectList(new LambdaQueryWrapperX<CombinationRecordDO>()
                .eq(CombinationRecordDO::getUserId, userId)
                .eq(CombinationRecordDO::getActivityId, activityId));
    }

    /**
     * 获取最近的 count 条数据
     *
     * @param count 数量
     * @return 拼团记录列表
     */
    default List<CombinationRecordDO> selectLatestList(int count) {
        return selectList(new LambdaQueryWrapperX<CombinationRecordDO>()
                .orderByDesc(CombinationRecordDO::getId)
                .last("LIMIT " + count));
    }

    default List<CombinationRecordDO> selectListByActivityIdAndStatusAndHeadId(Long activityId, Integer status,
                                                                               Long headId, Integer count) {
        return selectList(new LambdaQueryWrapperX<CombinationRecordDO>()
                .eqIfPresent(CombinationRecordDO::getActivityId, activityId)
                .eqIfPresent(CombinationRecordDO::getStatus, status)
                .eq(CombinationRecordDO::getHeadId, headId)
                .orderByDesc(CombinationRecordDO::getId)
                .last("LIMIT " + count));
    }

    default Map<Long, Integer> selectCombinationRecordCountMapByActivityIdAndStatusAndHeadId(Collection<Long> activityIds,
                                                                                             Integer status, Long headId) {
        // SQL count 查询
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<CombinationRecordDO>()
                .select("COUNT(DISTINCT(user_id)) AS recordCount, activity_id AS activityId")
                .in("activity_id", activityIds)
                .eq(status != null, "status", status)
                .eq(headId != null, "head_id", headId)
                .groupBy("activity_id"));
        if (CollUtil.isEmpty(result)) {
            return Collections.emptyMap();
        }
        // 转换数据
        return CollectionUtils.convertMap(result,
                record -> MapUtil.getLong(record, "activityId"),
                record -> MapUtil.getInt(record, "recordCount"));
    }

    default PageResult<CombinationRecordDO> selectPage(CombinationRecordReqPageVO pageVO) {
        LambdaQueryWrapperX<CombinationRecordDO> queryWrapper = new LambdaQueryWrapperX<CombinationRecordDO>()
                .eqIfPresent(CombinationRecordDO::getStatus, pageVO.getStatus())
                .betweenIfPresent(CombinationRecordDO::getCreateTime, pageVO.getCreateTime());
        // 如果 headId 非空，说明查询指定团的团长 + 团员的拼团记录
        if (pageVO.getHeadId() != null) {
            queryWrapper.eq(CombinationRecordDO::getId, pageVO.getHeadId()) // 团长
                    .or().eq(CombinationRecordDO::getHeadId, pageVO.getHeadId()); // 团员
        }
        return selectPage(pageVO, queryWrapper);
    }

    /**
     * 查询指定条件的记录数
     *
     * @param status       状态，可为 null
     * @param virtualGroup 是否虚拟成团，可为 null
     * @param headId       团长编号，可为 null
     * @return 记录数
     */
    default Long selectCountByHeadAndStatusAndVirtualGroup(Integer status, Boolean virtualGroup, Long headId) {
        return selectCount(new LambdaQueryWrapperX<CombinationRecordDO>()
                .eqIfPresent(CombinationRecordDO::getStatus, status)
                .eqIfPresent(CombinationRecordDO::getVirtualGroup, virtualGroup)
                .eqIfPresent(CombinationRecordDO::getHeadId, headId));
    }

    /**
     * 查询用户拼团记录（DISTINCT 去重），也就是说查询会员表中的用户有多少人参与过拼团活动每个人只统计一次
     *
     * @return 参加过拼团的用户数
     */
    default Long selectUserCount() {
        return selectCount(new QueryWrapper<CombinationRecordDO>()
                .select("DISTINCT (user_id)"));
    }

    default List<CombinationRecordDO> selectListByHeadIdAndStatusAndExpireTimeLt(Long headId, Integer status, LocalDateTime dateTime) {
        return selectList(new LambdaQueryWrapperX<CombinationRecordDO>()
                .eq(CombinationRecordDO::getHeadId, headId)
                .eq(CombinationRecordDO::getStatus, status)
                .lt(CombinationRecordDO::getExpireTime, dateTime));
    }

    default List<CombinationRecordDO> selectListByHeadId(Long headId) {
        return selectList(CombinationRecordDO::getHeadId, headId);
    }

}
