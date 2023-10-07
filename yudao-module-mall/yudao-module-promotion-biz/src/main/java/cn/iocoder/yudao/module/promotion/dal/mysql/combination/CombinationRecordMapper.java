package cn.iocoder.yudao.module.promotion.dal.mysql.combination;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.recrod.CombinationRecordReqPageVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationRecordDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.time.LocalTime;
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

    default List<CombinationRecordDO> selectListByUserIdAndStatus(Long userId, Integer status) {
        return selectList(new LambdaQueryWrapperX<CombinationRecordDO>()
                .eq(CombinationRecordDO::getUserId, userId)
                .eq(CombinationRecordDO::getStatus, status));
    }

    /**
     * 查询拼团记录
     *
     * @param headId 团长编号
     * @return 拼团记录
     */
    default CombinationRecordDO selectOneByHeadId(Long headId, Integer status) {
        return selectOne(new LambdaQueryWrapperX<CombinationRecordDO>()
                .eq(CombinationRecordDO::getId, headId)
                .eq(CombinationRecordDO::getStatus, status));
    }

    default List<CombinationRecordDO> selectListByHeadIdAndStatus(Long headId, Integer status) {
        return selectList(new LambdaQueryWrapperX<CombinationRecordDO>()
                .eq(CombinationRecordDO::getHeadId, headId)
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

    /**
     * 获得最近 count 条拼团记录（团长发起的）
     *
     * @param activityId 拼团活动编号
     * @param status     记录状态
     * @param count      数量
     * @return 拼团记录列表
     */
    default List<CombinationRecordDO> selectList(Long activityId, Integer status, Integer count) {
        return selectList(new LambdaQueryWrapperX<CombinationRecordDO>()
                .eqIfPresent(CombinationRecordDO::getActivityId, activityId)
                .eqIfPresent(CombinationRecordDO::getStatus, status)
                .eq(CombinationRecordDO::getHeadId, null) // TODO 团长的 headId 是不是 null 还是自己的记录编号来着？
                .orderByDesc(CombinationRecordDO::getCreateTime)
                .last("LIMIT " + count));
    }

    default Map<Long, Integer> selectCombinationRecordCountMapByActivityIdAndStatusAndHeadId(Collection<Long> activityIds,
                                                                                             Integer status, Integer headId) {
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

    static LocalDateTime[] builderQueryTime(Integer dateType) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime[] createTime = null; // 全部时间段
        // 今天-一天开始到结束
        if (ObjectUtil.equal(dateType, CombinationRecordReqPageVO.TO_DAY)) {
            createTime = new LocalDateTime[]{now.toLocalDate().atStartOfDay(), now.toLocalDate().atTime(LocalTime.MAX)};
        }
        // 昨天-昨天开始和结束
        if (ObjectUtil.equal(dateType, CombinationRecordReqPageVO.YESTERDAY)) {
            createTime = new LocalDateTime[]{now.minusDays(1).toLocalDate().atStartOfDay(),
                    now.minusDays(1).toLocalDate().atTime(LocalTime.MAX)};
        }
        // 最近七天
        if (ObjectUtil.equal(dateType, CombinationRecordReqPageVO.LAST_SEVEN_DAYS)) {
            createTime = new LocalDateTime[]{now.minusDays(7).toLocalDate().atStartOfDay(),
                    now.toLocalDate().atTime(LocalTime.MAX)};
        }
        // 最近 30 天
        if (ObjectUtil.equal(dateType, CombinationRecordReqPageVO.LAST_30_DAYS)) {
            createTime = new LocalDateTime[]{now.minusDays(30).toLocalDate().atStartOfDay(),
                    now.toLocalDate().atTime(LocalTime.MAX)};
        }
        // 本月
        if (ObjectUtil.equal(dateType, CombinationRecordReqPageVO.THIS_MONTH)) {
            // 获取本月的开始时间
            LocalDateTime startTime = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
            // 获取下个月的开始时间，然后减去一秒以获取本月的结束时间
            LocalDateTime endTime = now.withDayOfMonth(1).plusMonths(1).toLocalDate().atStartOfDay().minusSeconds(1);
            createTime = new LocalDateTime[]{startTime, endTime};
        }
        // 本年
        if (ObjectUtil.equal(dateType, CombinationRecordReqPageVO.THIS_YEAR)) {
            // 获取本年的开始时间
            LocalDateTime startTime = now.withDayOfYear(1).toLocalDate().atStartOfDay();
            // 获取下一年的开始时间，然后减去一秒以获取本年的结束时间
            LocalDateTime endTime = now.withDayOfYear(1).plusYears(1).toLocalDate().atStartOfDay().minusSeconds(1);
            createTime = new LocalDateTime[]{startTime, endTime};
        }
        return createTime;
    }

    default PageResult<CombinationRecordDO> selectPage(CombinationRecordReqPageVO pageVO) {
        // 兼容自选时间段
        if (pageVO.getDateType() != null) {
            pageVO.setCreateTime(builderQueryTime(pageVO.getDateType()));
        }
        return selectPage(pageVO, new LambdaQueryWrapperX<CombinationRecordDO>()
                .eqIfPresent(CombinationRecordDO::getStatus, pageVO.getStatus())
                .betweenIfPresent(CombinationRecordDO::getCreateTime, pageVO.getCreateTime()));
    }

    default Long selectCount(Integer dateType) {
        return selectCount(new LambdaQueryWrapperX<CombinationRecordDO>()
                .betweenIfPresent(CombinationRecordDO::getCreateTime, builderQueryTime(dateType)));
    }

    /**
     * 查询指定团长的拼团记录（包括团长）
     *
     * @param activityId 活动编号
     * @param headId     团长编号
     * @return 拼团记录
     */
    default List<CombinationRecordDO> selectList(Long activityId, Long headId) {
        return selectList(new LambdaQueryWrapperX<CombinationRecordDO>()
                .eq(CombinationRecordDO::getActivityId, activityId)
                .eq(CombinationRecordDO::getHeadId, headId)
                .or()
                .eq(CombinationRecordDO::getId, headId));

    }

}
