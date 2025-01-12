package cn.iocoder.yudao.module.iot.dal.tdengine;

import cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData.IotDeviceLogPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceLogDO;
import cn.iocoder.yudao.module.iot.framework.tdengine.core.annotation.TDengineDS;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
     * 初始化只创建一次
     */
    void createDeviceLogSTable();


    // TODO @super：单个参数，不用加 @Param
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
    List<IotDeviceLogDO> selectPage(IotDeviceLogPageReqVO reqVO);

    /**
     * 获得设备日志总数
     *
     * @param reqVO 查询条件
     * @return 日志总数
     */
    Long selectCount(IotDeviceLogPageReqVO reqVO);

    /**
     * 查询设备日志表是否存在
     *
     */
    Object checkDeviceLogTableExists();
}
