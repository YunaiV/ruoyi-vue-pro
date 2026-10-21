package cn.iocoder.yudao.module.iot.dal.iotdb;

import cn.iocoder.yudao.module.iot.framework.iotdb.core.annotation.IoTDBDS;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * IoTDB 设备消息 Mapper
 *
 * 注意：参数均为 {@link IotDeviceMessageIoTDBCodec} 编码后的存储值；分页通过手写 LIMIT/OFFSET 实现，不依赖 MyBatis Plus 分页插件
 *
 * @author 芋道源码
 */
@Mapper
@IoTDBDS
@InterceptorIgnore(tenantLine = "true", dataPermission = "true") // 避免 SQL 解析，因为 JSqlParser 对 IoTDB 的 SQL 解析会报错；租户由 Service 层控制
public interface IotDeviceMessageIoTDBMapper {

    /**
     * 获得消息表的列
     *
     * @return 列列表，包括 ColumnName、DataType、Category 等
     */
    List<Map<String, Object>> selectColumnList();

    void createTable();

    void insert(IotDeviceMessageIoTDBRow row);

    List<IotDeviceMessageIoTDBRow> selectList(@Param("deviceId") String deviceId,
                                              @Param("method") String method,
                                              @Param("upstream") Integer upstream,
                                              @Param("reply") Integer reply,
                                              @Param("identifier") String identifier,
                                              @Param("offset") Integer offset,
                                              @Param("limit") Integer limit);

    Long selectCount(@Param("deviceId") String deviceId,
                     @Param("method") String method,
                     @Param("upstream") Integer upstream,
                     @Param("reply") Integer reply,
                     @Param("identifier") String identifier);

    List<IotDeviceMessageIoTDBRow> selectListByRequestIdsAndReply(@Param("deviceId") String deviceId,
                                                                  @Param("requestIds") Collection<String> requestIds,
                                                                  @Param("reply") Integer reply);

    Long selectCountByCreateTime(@Param("createTime") Long createTime);

    List<Map<String, Object>> selectCountGroupByHour(@Param("startTime") Long startTime,
                                                     @Param("endTime") Long endTime);

}
