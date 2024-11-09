package cn.iocoder.yudao.module.iot.dal.tdengine;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.SelectDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.SelectVisualDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.TagsSelectDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.TdTableDO;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 专门处理 TD Engine 的 DML（数据操作语言）操作，处理所有的数据查询和写入操作，如插入数据、查询数据等
 */
@Mapper
@DS("tdengine")
public interface TdEngineDMLMapper {

    /**
     * 插入数据 - 指定列插入数据
     *
     * @param table 数据
     *              dataBaseName 数据库名
     *              tableName 表名
     *              columns 列
     */
    @TenantIgnore
    void insertData(TdTableDO table);

    /**
     * 根据时间戳查询数据
     *
     * @param selectDO 查询条件
     * @return 查询结果列表
     */
    @TenantIgnore
    List<Map<String, Object>> selectByTimestamp(SelectDO selectDO);

    /**
     * 根据时间戳获取数据条数
     *
     * @param selectDO 查询条件
     * @return 数据条数
     */
    @TenantIgnore
    Map<String, Long> selectCountByTimestamp(SelectDO selectDO);

    /**
     * 获取最新数据
     *
     * @param selectDO 查询条件
     * @return 最新数据
     */
    @TenantIgnore
    Map<String, Object> selectOneLastData(SelectDO selectDO);

    /**
     * 获取历史数据列表
     *
     * @param selectVisualDO 查询条件
     * @return 历史数据列表
     */
    @TenantIgnore
    List<Map<String, Object>> selectHistoryDataList(SelectVisualDO selectVisualDO);

    /**
     * 获取实时数据列表
     *
     * @param selectVisualDO 查询条件
     * @return 实时数据列表
     */
    @TenantIgnore
    List<Map<String, Object>> selectRealtimeDataList(SelectVisualDO selectVisualDO);

    /**
     * 获取聚合数据列表
     *
     * @param selectVisualDO 查询条件
     * @return 聚合数据列表
     */
    @TenantIgnore
    List<Map<String, Object>> selectAggregateDataList(SelectVisualDO selectVisualDO);

    /**
     * 根据标签获取最新数据列表
     *
     * @param tagsSelectDO 查询条件
     * @return 最新数据列表
     */
    @TenantIgnore
    List<Map<String, Object>> selectLastDataListByTags(TagsSelectDO tagsSelectDO);

    /**
     * 获取历史数据条数
     *
     * @param selectVisualDO 查询条件
     * @return 数据条数
     */
    @TenantIgnore
    Long selectHistoryCount(SelectVisualDO selectVisualDO);
}