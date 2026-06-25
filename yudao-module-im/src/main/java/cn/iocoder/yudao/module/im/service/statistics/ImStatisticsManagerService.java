package cn.iocoder.yudao.module.im.service.statistics;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * IM 数据看板 Service 接口
 * <p>
 * 仅服务于 manager 后台统计页，独立于业务 Service，避免污染。
 * 返回的均为聚合后的简单结构，由 Controller 负责 VO 装配与昵称回填。
 *
 * @author 芋道源码
 */
public interface ImStatisticsManagerService {

    // ==================== 用户 ====================

    /**
     * 获取用户总数
     */
    Long getTotalUserCount();

    /**
     * 获取区间内新增用户数
     */
    Long getNewUserCount(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获取区间内活跃用户数（私聊+群聊去重）
     */
    Long getActiveUserCount(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获取区间内每日新增用户数 Map（key 为 yyyy-MM-dd 日期）
     */
    Map<LocalDateTime, Long> getNewUserDailyCountMap(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获取区间内每日活跃用户数 Map
     */
    Map<LocalDateTime, Long> getActiveUserDailyCountMap(LocalDateTime beginTime, LocalDateTime endTime);

    // ==================== 群 ====================

    /**
     * 获取群总数
     */
    Long getTotalGroupCount();

    /**
     * 获取区间内新建群数
     */
    Long getNewGroupCount(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获取群规模分布 Map（key 为分桶名）
     */
    Map<String, Long> getGroupSizeCountMap();

    // ==================== 消息 ====================

    /**
     * 获取区间内私聊消息数
     */
    Long getPrivateMessageCount(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获取区间内群聊消息数
     */
    Long getGroupMessageCount(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获取区间内每日私聊消息数 Map
     */
    Map<LocalDateTime, Long> getPrivateMessageDailyCountMap(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获取区间内每日群聊消息数 Map
     */
    Map<LocalDateTime, Long> getGroupMessageDailyCountMap(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获取区间内内容类型分布 Map（key 为消息类型）
     */
    Map<Integer, Long> getMessageTypeCountMap(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获取区间内 TOP 发送者 Map（key 为 userId，value 为消息数；按消息数倒序）
     */
    Map<Long, Long> getTopSenderCountMap(LocalDateTime beginTime, LocalDateTime endTime, int limit);

}
