package cn.iocoder.yudao.module.iot.dal.tdengine;

import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceLogDO;
import cn.iocoder.yudao.module.iot.framework.tdengine.core.annotation.TDengineDS;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * IOT 设备日志数据 Mapper 接口
 * 
 * 基于 TDengine 实现设备日志的存储
 */
@Mapper
@TDengineDS
@InterceptorIgnore(tenantLine = "true") // 避免 SQL 解析，因为 JSqlParser 对 TDengine 的 SQL 解析会报错
public interface IotDeviceLogDataMapper {

    /**
     * 创建设备日志超级表
     * 
     * 注意：初始化时只需创建一次
     */
    void createDeviceLogSTable();




    /**
     * 插入设备日志数据
     * 
     * 如果子表不存在，会自动创建子表
     * 
     * @param log 设备日志数据
     */
    void insert(@Param("log") IotDeviceLogDO log);
}
