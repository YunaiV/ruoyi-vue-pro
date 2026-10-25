package cn.iocoder.yudao.module.iot.dal.iotdb;

import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyRespVO;
import cn.iocoder.yudao.module.iot.framework.iotdb.core.annotation.IoTDBDS;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * IoTDB 设备属性 Mapper
 *
 * 注意：表名、列名需要动态拼接，只允许传入 Long 产品编号、DAO 归一化并校验后的列名、白名单内的类型；值统一使用 #{} 绑定
 *
 * @author 芋道源码
 */
@Mapper
@IoTDBDS
@InterceptorIgnore(tenantLine = "true", dataPermission = "true") // 避免 SQL 解析，因为 JSqlParser 对 IoTDB 的 SQL 解析会报错；租户由 Service 层控制
public interface IotDevicePropertyIoTDBMapper {

    /**
     * 获得产品属性表的列
     *
     * @param productId 产品编号
     * @return 列列表，包括 ColumnName、DataType、Category 等
     */
    List<Map<String, Object>> selectColumnList(@Param("productId") Long productId);

    void createTable(@Param("productId") Long productId,
                     @Param("columns") List<Column> columns);

    void addColumn(@Param("productId") Long productId,
                   @Param("column") Column column);

    void dropColumn(@Param("productId") Long productId,
                    @Param("columnName") String columnName);

    void dropTable(@Param("productId") Long productId);

    void insert(@Param("productId") Long productId,
                @Param("deviceId") String deviceId,
                @Param("ts") Long ts,
                @Param("reportTime") Long reportTime,
                @Param("columns") List<Column> columns);

    List<IotDevicePropertyRespVO> selectHistoryList(@Param("productId") Long productId,
                                                    @Param("columnName") String columnName,
                                                    @Param("deviceId") String deviceId,
                                                    @Param("startTime") Long startTime,
                                                    @Param("endTime") Long endTime);

    /**
     * 属性列
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class Column {

        /**
         * 列名，格式为 p_ + 中立字段名
         */
        private String name;
        /**
         * 数据类型，例如说 INT32、STRING；写入时为空
         */
        private String type;
        /**
         * 值；DDL 时为空
         */
        private Object value;

    }

}
