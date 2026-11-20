package cn.iocoder.yudao.module.iot.dal.questdb;

import cn.iocoder.yudao.module.iot.controller.admin.device.vo.message.IotDeviceMessagePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceMessageDO;
import cn.iocoder.yudao.module.iot.framework.questdb.core.annotation.QuestDBDS;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * QuestDB 设备消息 Mapper
 *
 * 注意：时间参数均为微秒；分页通过手写 LIMIT lo, hi 实现，不依赖 MyBatis Plus 分页插件
 *
 * @author 芋道源码
 */
@Mapper
@QuestDBDS
@InterceptorIgnore(tenantLine = "true", dataPermission = "true") // 避免 SQL 解析，因为 JSqlParser 对 QuestDB 的 SQL 解析会报错；租户由 Service 层控制
public interface IotDeviceMessageQuestDBMapper {

    void createTable();

    /**
     * 获得消息表的列
     *
     * @return 列列表，包括 column、type、designated、upsertKey
     */
    List<Map<String, Object>> selectColumnList();

    /**
     * 获得消息表的属性
     *
     * @return 表属性，包括 designatedTimestamp、walEnabled、dedup；表不存在时，返回 null
     */
    Map<String, Object> selectTable();

    /**
     * 获得消息表的建表语句
     *
     * 原因：DEDUP UPSERT 与 SKIP 的 tables().dedup、table_columns().upsertKey 完全相同，只有建表语句能区分
     *
     * @return SHOW CREATE TABLE 的 ddl
     */
    String selectCreateTable();

    /**
     * 写入消息
     *
     * @param message 消息
     * @param ts      存储时间（微秒）
     * @param params  请求参数的 JSON 字符串
     * @param data    响应结果的 JSON 字符串
     * @param code    响应错误码。转为 Long 绑定：按 int4 绑定时，Integer.MIN_VALUE 会被 QuestDB 当成 NULL
     */
    void insert(@Param("message") IotDeviceMessageDO message,
                @Param("ts") Long ts,
                @Param("params") String params,
                @Param("data") String data,
                @Param("code") Long code);

    Long selectCount(@Param("reqVO") IotDeviceMessagePageReqVO reqVO);

    /**
     * 分页查询
     *
     * @param lo 起始行（包含）；为 null 时不分页
     * @param hi 结束行（不包含）
     */
    List<IotDeviceMessageDO> selectList(@Param("reqVO") IotDeviceMessagePageReqVO reqVO,
                                        @Param("lo") Long lo,
                                        @Param("hi") Long hi);

    List<IotDeviceMessageDO> selectListByRequestIdsAndReply(@Param("deviceId") Long deviceId,
                                                            @Param("requestIds") Collection<String> requestIds,
                                                            @Param("reply") Boolean reply);

    Long selectCountByCreateTime(@Param("createTime") Long createTime);

    List<Map<String, Object>> selectCountGroupByHour(@Param("startTime") Long startTime,
                                                     @Param("endTime") Long endTime);

}
