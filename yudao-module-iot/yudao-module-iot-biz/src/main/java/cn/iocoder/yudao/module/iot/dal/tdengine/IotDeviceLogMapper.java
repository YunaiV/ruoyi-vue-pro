package cn.iocoder.yudao.module.iot.dal.tdengine;

import cn.iocoder.yudao.module.iot.controller.admin.device.vo.data.IotDeviceLogPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceLogDO;
import cn.iocoder.yudao.module.iot.framework.tdengine.core.annotation.TDengineDS;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 设备日志 {@link IotDeviceLogDO} Mapper 接口
 */
@Mapper
@TDengineDS
@InterceptorIgnore(tenantLine = "true") // 避免 SQL 解析，因为 JSqlParser 对 TDengine 的 SQL 解析会报错
public interface IotDeviceLogMapper {

    /**
     * 创建设备日志超级表
     */
    void createDeviceLogSTable();

    /**
     * 查询设备日志表是否存在
     *
     * @return 存在则返回表名；不存在则返回 null
     */
    String showDeviceLogSTable();

    /**
     * 插入设备日志数据
     *
     * 如果子表不存在，会自动创建子表
     *
     * @param log 设备日志数据
     */
    void insert(IotDeviceLogDO log);

    /**
     * 获得设备日志分页
     *
     * @param reqVO 分页查询条件
     * @return 设备日志列表
     */
    IPage<IotDeviceLogDO> selectPage(IPage<IotDeviceLogDO> page,
                                     @Param("reqVO") IotDeviceLogPageReqVO reqVO);

    /**
     * 统计设备日志数量
     *
     * @param createTime 创建时间，如果为空，则统计所有日志数量
     * @return 日志数量
     */
    Long selectCountByCreateTime(@Param("createTime") Long createTime);

    // TODO @super：1）上行、下行，不写在 mapper 里，而是通过参数传递，这样，selectDeviceLogUpCountByHour、selectDeviceLogDownCountByHour 可以合并；
    //  TODO @super：2）不能只基于 identifier 来计算，而是要 type + identifier 成对
    /**
     * 查询每个小时设备上行消息数量
     */
    List<Map<String, Object>> selectDeviceLogUpCountByHour(@Param("deviceKey") String deviceKey,
                                                           @Param("startTime") Long startTime,
                                                           @Param("endTime") Long endTime);

    /**
     * 查询每个小时设备下行消息数量
     */
    List<Map<String, Object>> selectDeviceLogDownCountByHour(@Param("deviceKey") String deviceKey,
                                                             @Param("startTime") Long startTime,
                                                             @Param("endTime") Long endTime);

}
