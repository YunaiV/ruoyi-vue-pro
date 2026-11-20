package cn.iocoder.yudao.module.iot.dal.questdb;

import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyRespVO;
import cn.iocoder.yudao.module.iot.framework.questdb.core.annotation.QuestDBDS;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * QuestDB 设备属性 Mapper
 *
 * 注意：表名、列名需要动态拼接，只允许传入 Long 产品编号、DAO 归一化并校验后的列名、白名单内的类型；值统一使用 #{} 绑定
 *
 * @author 芋道源码
 */
@Mapper
@QuestDBDS
@InterceptorIgnore(tenantLine = "true", dataPermission = "true") // 避免 SQL 解析，因为 JSqlParser 对 QuestDB 的 SQL 解析会报错；租户由 Service 层控制
public interface IotDevicePropertyQuestDBMapper {

    /**
     * 等待产品属性表的 WAL 应用完成
     *
     * 原因：ALTER 写入 WAL 后异步应用，应用前 table_columns 读到的仍是旧结构。表被挂起时，等待到查询超时后报错
     *
     * @param productId 产品编号
     */
    Object waitWalTable(@Param("productId") Long productId);

    /**
     * 获得产品属性表的列
     *
     * @param productId 产品编号
     * @return 列列表，包括 column、type、designated
     */
    List<Map<String, Object>> selectColumnList(@Param("productId") Long productId);

    /**
     * 获得产品属性表的属性
     *
     * @param productId 产品编号
     * @return 表属性，包括 walEnabled、dedup；表不存在时，返回 null
     */
    Map<String, Object> selectTable(@Param("productId") Long productId);

    void createTable(@Param("productId") Long productId,
                     @Param("columns") List<Column> columns);

    void addColumn(@Param("productId") Long productId,
                   @Param("column") Column column);

    void dropColumn(@Param("productId") Long productId,
                    @Param("columnName") String columnName);

    void dropTable(@Param("productId") Long productId);

    /**
     * 写入设备属性
     *
     * @param ts 存储时间（微秒）
     */
    void insert(@Param("productId") Long productId,
                @Param("deviceId") Long deviceId,
                @Param("ts") Long ts,
                @Param("reportTime") Long reportTime,
                @Param("columns") List<Column> columns);

    /**
     * 查询属性历史
     *
     * @param startTime 开始时间（微秒），包含
     * @param endTime   结束时间（微秒），包含
     * @return 历史数据，updateTime 为毫秒；value 为存储值，由 DAO 解码
     */
    List<IotDevicePropertyRespVO> selectHistoryList(@Param("productId") Long productId,
                                                    @Param("columnName") String columnName,
                                                    @Param("deviceId") Long deviceId,
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
         * 列名，格式为 p_ 或 d_ + 中立字段名
         */
        private String name;
        /**
         * 数据类型，例如说 LONG、VARCHAR；写入时为空
         */
        private String type;
        /**
         * 值；DDL 时为空
         */
        private Object value;

    }

}
