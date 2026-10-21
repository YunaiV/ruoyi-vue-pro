package cn.iocoder.yudao.module.iot.dal.storage;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.message.IotDeviceMessagePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceMessageDO;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.*;

/**
 * IoT 设备【消息】日志的存储 DAO 接口
 *
 * 由 yudao.iot.storage.type 选择 TDengine、IoTDB 其中一个实现。各实现的消息身份不同：
 * 1. TDengine：(deviceId, ts, id)，按 ts DESC, id DESC 排序。同一设备、同一毫秒、不同 id 的消息都会保留
 * 2. IoTDB：(deviceId, ts)，id 为普通字段，按 ts DESC 排序。同一设备、同一毫秒再次写入时，后写的整行替换先写的，即使 id 不同。
 *    原因：id 作为 TAG 时，每个设备下每出现一个新的消息 id，就产生一个 IoTDB 元数据设备，元数据随消息量无限增长
 *
 * 重复写入时：
 * 1. 相同 (deviceId, ts, id)：两者都只保留一行。IoTDB 由编码保证整行替换（null 字段也会清空旧值）；TDengine 沿用原有写入语义
 * 2. 重投、重发等重新获取 ts 的写入，按各自的物理键判断：TDengine 为 (deviceId, ts, id)，与已有行都不同时新增，否则覆盖该行；
 *    IoTDB 为 (deviceId, ts)，该设备该毫秒没有行时新增，已有行时覆盖该行（可能是另一条消息）
 *
 * @author 芋道源码
 */
public interface IotDeviceMessageDAO {

    /**
     * 按小时统计结果的 key：整点时间，类型为 {@link Timestamp}
     */
    String COUNT_TIME = "time";
    /**
     * 按小时统计结果的 key：上行消息数，类型为 {@link Long}
     */
    String COUNT_UPSTREAM = "upstream_count";
    /**
     * 按小时统计结果的 key：下行消息数，类型为 {@link Long}
     */
    String COUNT_DOWNSTREAM = "downstream_count";

    /**
     * 初始化：系统启动时，校验存储后端的前提条件，并创建消息表
     */
    void initialize();

    /**
     * 写入设备消息
     *
     * @param message 设备消息
     */
    void insert(IotDeviceMessageDO message);

    /**
     * 获得设备消息分页
     *
     * 注意：与现有行为保持一致，不按 reqVO.times 过滤
     *
     * @param reqVO 分页查询条件
     * @return 设备消息分页；消息表不存在时，返回空分页
     */
    PageResult<IotDeviceMessageDO> selectPage(IotDeviceMessagePageReqVO reqVO);

    /**
     * 按照 requestIds 批量查询消息
     *
     * 各存储后端保持一致：requestIds 中的 null 忽略、重复去重；忽略后为空，或 reply 为 null 时，返回空列表
     *
     * @param deviceId   设备编号
     * @param requestIds 请求编号集合
     * @param reply      是否回复消息
     * @return 消息列表
     */
    List<IotDeviceMessageDO> selectListByRequestIdsAndReply(Long deviceId, Collection<String> requestIds, Boolean reply);

    /**
     * 统计设备消息数量
     *
     * @param createTime 创建时间（毫秒），如果为空，则统计所有消息数量
     * @return 消息数量
     */
    Long selectCountByCreateTime(@Nullable Long createTime);

    /**
     * 按照时间范围（小时），统计设备的消息数量
     *
     * @param startTime 开始时间（毫秒），包含
     * @param endTime   结束时间（毫秒），包含
     * @return 每个整点的统计，key 见 {@link #COUNT_TIME}、{@link #COUNT_UPSTREAM}、{@link #COUNT_DOWNSTREAM}
     */
    List<Map<String, Object>> selectDeviceMessageCountGroupByDate(Long startTime, Long endTime);

    /**
     * 构建按小时统计的一行结果，统一各存储后端的 key 与 Java 类型
     *
     * @param time            整点时间，支持 Timestamp、毫秒数值
     * @param upstreamCount   上行消息数，支持任意 Number
     * @param downstreamCount 下行消息数，支持任意 Number
     * @return 统计结果
     */
    @SuppressWarnings("PatternVariableCanBeUsed") // 保持 JDK8 兼容性
    static Map<String, Object> buildMessageCount(Object time, Object upstreamCount, Object downstreamCount) {
        // 1. 整点时间
        Timestamp timestamp;
        if (time instanceof Timestamp) {
            timestamp = (Timestamp) time;
        } else if (time instanceof Date) {
            timestamp = new Timestamp(((Date) time).getTime());
        } else if (time instanceof Number) {
            timestamp = new Timestamp(((Number) time).longValue());
        } else {
            throw new IllegalArgumentException("无法转换为整点时间：" + time);
        }
        // 2. 上下行数量：SUM、COUNT 等聚合结果的 Number 子类因库而异，统一转为 Long
        Map<String, Object> count = new HashMap<>();
        count.put(COUNT_TIME, timestamp);
        count.put(COUNT_UPSTREAM, upstreamCount != null ? ((Number) upstreamCount).longValue() : 0L);
        count.put(COUNT_DOWNSTREAM, downstreamCount != null ? ((Number) downstreamCount).longValue() : 0L);
        return count;
    }

}
