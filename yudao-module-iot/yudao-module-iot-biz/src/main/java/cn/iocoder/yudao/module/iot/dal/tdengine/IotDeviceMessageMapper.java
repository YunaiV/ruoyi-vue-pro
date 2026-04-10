package cn.iocoder.yudao.module.iot.dal.tdengine;

import cn.iocoder.yudao.module.iot.controller.admin.device.vo.message.IotDeviceMessagePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceMessageDO;
import cn.iocoder.yudao.module.iot.framework.tdengine.core.annotation.TDengineDS;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 设备消息 {@link IotDeviceMessageDO} Mapper 接口
 */
@Mapper
@TDengineDS
@InterceptorIgnore(tenantLine = "true") // 避免 SQL 解析，因为 JSqlParser 对 TDengine 的 SQL 解析会报错
public interface IotDeviceMessageMapper {

    /**
     * 创建设备消息超级表
     */
    void createSTable();

    /**
     * 查询设备消息表是否存在
     *
     * @return 存在则返回表名；不存在则返回 null
     */
    String showSTable();

    /**
     * 插入设备消息数据
     *
     * 如果子表不存在，会自动创建子表
     *
     * @param message 设备消息数据
     */
    void insert(IotDeviceMessageDO message);

    /**
     * 获得设备消息分页
     *
     * @param reqVO 分页查询条件
     * @return 设备消息列表
     */
    IPage<IotDeviceMessageDO> selectPage(IPage<IotDeviceMessageDO> page,
                                         @Param("reqVO") IotDeviceMessagePageReqVO reqVO);

    /**
     * 统计设备消息数量
     *
     * @param createTime 创建时间，如果为空，则统计所有消息数量
     * @return 消息数量
     */
    Long selectCountByCreateTime(@Param("createTime") Long createTime);

    /**
     * 按照 requestIds 批量查询消息
     *
     * @param deviceId 设备编号
     * @param requestIds 请求编号集合
     * @param reply 是否回复消息
     * @return 消息列表
     */
    List<IotDeviceMessageDO> selectListByRequestIdsAndReply(@Param("deviceId") Long deviceId,
                                                            @Param("requestIds") Collection<String> requestIds,
                                                            @Param("reply") Boolean reply);

    /**
     * 按照时间范围（小时），统计设备的消息数量
     */
    List<Map<String, Object>> selectDeviceMessageCountGroupByDate(@Param("startTime") Long startTime,
                                                                  @Param("endTime") Long endTime);

}
