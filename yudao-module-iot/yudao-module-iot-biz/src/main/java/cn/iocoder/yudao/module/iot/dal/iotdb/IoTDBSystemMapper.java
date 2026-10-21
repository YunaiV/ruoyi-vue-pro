package cn.iocoder.yudao.module.iot.dal.iotdb;

import cn.iocoder.yudao.module.iot.framework.iotdb.core.IoTDBUtils;
import cn.iocoder.yudao.module.iot.framework.iotdb.core.annotation.IoTDBDS;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * IoTDB 系统信息 Mapper：只读取配置，用于启动前提检查
 *
 * @author 芋道源码
 */
@Mapper
@IoTDBDS
@InterceptorIgnore(tenantLine = "true", dataPermission = "true") // 避免 SQL 解析，因为 JSqlParser 对 IoTDB 的 SQL 解析会报错
public interface IoTDBSystemMapper {

    @Select("SHOW VARIABLES")
    List<Map<String, Object>> selectVariableList();

    @Select("SHOW DATANODES")
    List<Map<String, Object>> selectDataNodeList();

    /**
     * 获得指定 DataNode 的全部配置
     *
     * @param nodeId DataNode 编号，来自 SHOW DATANODES 的结果，不接收外部输入
     * @return 配置列表
     */
    @Select("SHOW ALL CONFIGURATION ON ${nodeId}")
    List<Map<String, Object>> selectAllConfigurationList(@Param("nodeId") Integer nodeId);

    /**
     * 校验 IoTDB 的前提条件，不满足时抛出异常。只检查，不修改任何配置：
     * 1. 数据库已预建：连接 URL 中的数据库不存在时，获取连接即失败
     * 2. TimestampPrecision 为 ms
     * 3. 所有 DataNode 为 Running，且 enable_partial_insert=false
     */
    default void verifyPrerequisites() {
        IoTDBUtils.validateTimestampPrecision(selectVariableList());
        List<Integer> nodeIds = IoTDBUtils.validateDataNodes(selectDataNodeList());
        for (Integer nodeId : nodeIds) {
            IoTDBUtils.validatePartialInsertDisabled(nodeId, selectAllConfigurationList(nodeId));
        }
    }

}
