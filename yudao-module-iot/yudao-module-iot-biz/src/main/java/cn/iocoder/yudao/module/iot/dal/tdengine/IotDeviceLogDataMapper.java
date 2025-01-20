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
    //讨论：艿菇这里有些特殊情况，我也学习了一下这块知识：
    // 如果使用的是Java 8及以上版本，并且编译器保留了参数名（通过编译器选项-parameters启用），则可以去掉@Param注解。MyBatis会自动使用参数的实际名称
    // 但在TDengine中 @Param去掉后TDengine会报错，以下是大模型的回答：
    // 不用加 @Param在普通的 MySQL 场景下是正确的 - 对于 MyBatis，当方法只有一个参数时，确实可以不用添加 @Param 注解。
    //但是在 TDengine 的场景下，情况不同：
    //TDengine 的特殊性：
    //TDengine 使用特殊的 SQL 语法
    //需要处理超级表(STable)和子表的概念
    //参数绑定的方式与普通 MySQL 不同
    //为什么这里必须要 @Param：
    //XML 中使用了 ${log.deviceKey} 这样的参数引用方式
    //需要在 SQL 中动态构建表名（device_log_${log.deviceKey}）
    //没有 @Param("log") 的话，MyBatis 无法正确解析参数
    /**
     * 插入设备日志数据
     *
     * 如果子表不存在，会自动创建子表
     *
     * @param log 设备日志数据
     */
    void insert(@Param("log") IotDeviceLogDO log);

    /**
     * 获得设备日志分页
     *
     * @param reqVO 分页查询条件
     * @return 设备日志列表
     */
    List<IotDeviceLogDO> selectPage(@Param("reqVO") IotDeviceLogPageReqVO reqVO);

    /**
     * 获得设备日志总数
     *
     * @param reqVO 查询条件
     * @return 日志总数
     */
    Long selectCount(@Param("reqVO") IotDeviceLogPageReqVO reqVO);

    /**
     * 查询设备日志表是否存在
     *
     */
    Object checkDeviceLogTableExists();
}
