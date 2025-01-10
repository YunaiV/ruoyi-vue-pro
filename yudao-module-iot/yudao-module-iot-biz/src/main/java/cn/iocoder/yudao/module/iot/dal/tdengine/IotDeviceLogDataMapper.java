package cn.iocoder.yudao.module.iot.dal.tdengine;

import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceLogDO;
import cn.iocoder.yudao.module.iot.framework.tdengine.core.annotation.TDengineDS;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * IoT 设备日志 Mapper
 *
 * @author alwayssuper
 */
@Mapper
@TDengineDS
@InterceptorIgnore(tenantLine = "true") // 避免 SQL 解析，因为 JSqlParser 对 TDengine 的 SQL 解析会报错
public interface IotDeviceLogDataMapper {

    /**
     * 创建设备日志超级表
     * 初始化只创建一次
     */
    void createDeviceLogSTable();

    /**
     * 创建设备日志子表
     *
     * @param deviceKey 设备标识
     */
    void createDeviceLogTable( @Param("deviceKey") String deviceKey);

    /**
     * 插入设备日志数据
     *
     * @param log 设备日志数据
     */
    void insert(@Param("log") IotDeviceLogDO log);

}
