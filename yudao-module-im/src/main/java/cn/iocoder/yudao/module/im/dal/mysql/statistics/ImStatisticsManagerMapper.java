package cn.iocoder.yudao.module.im.dal.mysql.statistics;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * IM 数据看板 Mapper
 * <p>
 * 独立于业务 Mapper：所有统计 SQL 集中在此，仅服务于 manager 看板，不被其它业务调用，保持各业务 Mapper / Service 不受统计需求污染。
 *
 * @author 芋道源码
 */
@Mapper
public interface ImStatisticsManagerMapper {

    String NORMAL_MESSAGE_CONDITION = "type IN (101,102,103,104,105,107,108,115,125) AND status <> 2";

    // ==================== 用户 ====================

    /**
     * 用户总数（system_users）
     */
    @Select("SELECT COUNT(*) FROM system_users WHERE deleted = 0")
    Long selectTotalUserCount();

    /**
     * 区间内新增用户数
     */
    @Select("SELECT COUNT(*) FROM system_users " +
            "WHERE deleted = 0 AND create_time >= #{beginTime} AND create_time < #{endTime}")
    Long selectNewUserCount(@Param("beginTime") LocalDateTime beginTime,
                            @Param("endTime") LocalDateTime endTime);

    /**
     * 区间内活跃用户数：私聊或群聊发过消息的去重用户数
     */
    @Select("SELECT COUNT(DISTINCT user_id) FROM (" +
            "  SELECT sender_id AS user_id FROM im_private_message " +
            "  WHERE deleted = 0 AND " + NORMAL_MESSAGE_CONDITION +
            "  AND send_time >= #{beginTime} AND send_time < #{endTime}" +
            "  UNION ALL " +
            "  SELECT sender_id AS user_id FROM im_group_message " +
            "  WHERE deleted = 0 AND " + NORMAL_MESSAGE_CONDITION +
            "  AND send_time >= #{beginTime} AND send_time < #{endTime}" +
            ") t")
    Long selectActiveUserCount(@Param("beginTime") LocalDateTime beginTime,
                               @Param("endTime") LocalDateTime endTime);

    /**
     * 区间内每日新增用户数（按天分组）
     *
     * @return [{date: "yyyy-MM-dd", count: 123}, ...]
     */
    @Select("SELECT DATE(create_time) AS date, COUNT(*) AS count FROM system_users " +
            "WHERE deleted = 0 AND create_time >= #{beginTime} AND create_time < #{endTime} " +
            "GROUP BY DATE(create_time)")
    List<Map<String, Object>> selectNewUserDailyCount(@Param("beginTime") LocalDateTime beginTime,
                                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 区间内每日活跃用户数（按天分组、跨私聊+群聊去重）
     */
    @Select("SELECT day AS date, COUNT(DISTINCT user_id) AS count FROM (" +
            "  SELECT DATE(send_time) AS day, sender_id AS user_id FROM im_private_message " +
            "  WHERE deleted = 0 AND " + NORMAL_MESSAGE_CONDITION +
            "  AND send_time >= #{beginTime} AND send_time < #{endTime}" +
            "  UNION ALL " +
            "  SELECT DATE(send_time) AS day, sender_id AS user_id FROM im_group_message " +
            "  WHERE deleted = 0 AND " + NORMAL_MESSAGE_CONDITION +
            "  AND send_time >= #{beginTime} AND send_time < #{endTime}" +
            ") t GROUP BY day")
    List<Map<String, Object>> selectActiveUserDailyCount(@Param("beginTime") LocalDateTime beginTime, @Param("endTime") LocalDateTime endTime);

    // ==================== 群 ====================

    /**
     * 当前有效群总数
     */
    @Select("SELECT COUNT(*) FROM im_group WHERE deleted = 0 AND status = 0")
    Long selectTotalGroupCount();

    /**
     * 区间内新建群数
     */
    @Select("SELECT COUNT(*) FROM im_group " +
            "WHERE deleted = 0 AND create_time >= #{beginTime} AND create_time < #{endTime}")
    Long selectNewGroupCount(@Param("beginTime") LocalDateTime beginTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 群规模分布（按群成员数分桶）
     *
     * @return [{range: "1-9 人", count: 123}, ...]
     */
    @Select("SELECT " +
            "  CASE " +
            "    WHEN cnt < 10 THEN '1-9 人' " +
            "    WHEN cnt < 50 THEN '10-49 人' " +
            "    WHEN cnt < 200 THEN '50-199 人' " +
            "    ELSE '200+ 人' " +
            "  END AS `range`, COUNT(*) AS count " +
            "FROM (" +
            "  SELECT g.id, COUNT(m.id) AS cnt " +
            "  FROM im_group g " +
            "  LEFT JOIN im_group_member m ON m.group_id = g.id AND m.status = 0 AND m.deleted = 0 " +
            "  WHERE g.deleted = 0 AND g.status = 0 " +
            "  GROUP BY g.id" +
            ") t " +
            "GROUP BY `range`")
    List<Map<String, Object>> selectGroupSizeDistribution();

    // ==================== 消息 ====================

    /**
     * 区间内私聊消息数
     */
    @Select("SELECT COUNT(*) FROM im_private_message " +
            "WHERE deleted = 0 AND " + NORMAL_MESSAGE_CONDITION +
            " AND send_time >= #{beginTime} AND send_time < #{endTime}")
    Long selectPrivateMessageCount(@Param("beginTime") LocalDateTime beginTime,
                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 区间内群聊消息数
     */
    @Select("SELECT COUNT(*) FROM im_group_message " +
            "WHERE deleted = 0 AND " + NORMAL_MESSAGE_CONDITION +
            " AND send_time >= #{beginTime} AND send_time < #{endTime}")
    Long selectGroupMessageCount(@Param("beginTime") LocalDateTime beginTime,
                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 区间内每日私聊消息数（按天分组）
     */
    @Select("SELECT DATE(send_time) AS date, COUNT(*) AS count FROM im_private_message " +
            "WHERE deleted = 0 AND " + NORMAL_MESSAGE_CONDITION +
            " AND send_time >= #{beginTime} AND send_time < #{endTime} " +
            "GROUP BY DATE(send_time)")
    List<Map<String, Object>> selectPrivateMessageDailyCount(@Param("beginTime") LocalDateTime beginTime,
                                                             @Param("endTime") LocalDateTime endTime);

    /**
     * 区间内每日群聊消息数（按天分组）
     */
    @Select("SELECT DATE(send_time) AS date, COUNT(*) AS count FROM im_group_message " +
            "WHERE deleted = 0 AND " + NORMAL_MESSAGE_CONDITION +
            " AND send_time >= #{beginTime} AND send_time < #{endTime} " +
            "GROUP BY DATE(send_time)")
    List<Map<String, Object>> selectGroupMessageDailyCount(@Param("beginTime") LocalDateTime beginTime,
                                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 区间内消息类型分布（私聊+群聊合并）
     *
     * @return [{type: 0, count: 123}, ...]
     */
    @Select("SELECT type, COUNT(*) AS count FROM (" +
            "  SELECT type FROM im_private_message " +
            "  WHERE deleted = 0 AND " + NORMAL_MESSAGE_CONDITION +
            "  AND send_time >= #{beginTime} AND send_time < #{endTime}" +
            "  UNION ALL " +
            "  SELECT type FROM im_group_message " +
            "  WHERE deleted = 0 AND " + NORMAL_MESSAGE_CONDITION +
            "  AND send_time >= #{beginTime} AND send_time < #{endTime}" +
            ") t GROUP BY type")
    List<Map<String, Object>> selectMessageTypeDistribution(@Param("beginTime") LocalDateTime beginTime,
                                                            @Param("endTime") LocalDateTime endTime);

    /**
     * 区间内 TOP 发送者（私聊+群聊合并，按消息数倒序）
     *
     * @return [{userId: 1024, messageCount: 1500}, ...]
     */
    @Select("SELECT user_id AS userId, COUNT(*) AS messageCount FROM (" +
            "  SELECT sender_id AS user_id FROM im_private_message " +
            "  WHERE deleted = 0 AND " + NORMAL_MESSAGE_CONDITION +
            "  AND send_time >= #{beginTime} AND send_time < #{endTime}" +
            "  UNION ALL " +
            "  SELECT sender_id AS user_id FROM im_group_message " +
            "  WHERE deleted = 0 AND " + NORMAL_MESSAGE_CONDITION +
            "  AND send_time >= #{beginTime} AND send_time < #{endTime}" +
            ") t GROUP BY user_id ORDER BY messageCount DESC LIMIT #{limit}")
    List<Map<String, Object>> selectTopSenders(@Param("beginTime") LocalDateTime beginTime,
                                               @Param("endTime") LocalDateTime endTime,
                                               @Param("limit") int limit);

}
