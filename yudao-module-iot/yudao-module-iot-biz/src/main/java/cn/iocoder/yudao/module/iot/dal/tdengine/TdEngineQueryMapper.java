package cn.iocoder.yudao.module.iot.dal.tdengine;

import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.SelectDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.SelectVisualDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.TagsSelectDO;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * TD 引擎的查询 Mapper
 */
@Mapper
@DS("tdengine")
public interface TdEngineQueryMapper {

    /**
     * 根据时间戳查询数据
     *
     * @param selectDO 查询条件
     * @return 查询结果
     */
    List<Map<String, Object>> selectByTimestamp(SelectDO selectDO);

    /**
     * 根据时间戳获取数据条数
     *
     * @param selectDO 查询条件
     * @return 数据条数
     */
    Map<String, Long> getCountByTimestamp(SelectDO selectDO);

    /**
     * 获取最新数据
     *
     * @param selectDO 查询条件
     * @return 最新数据
     */
    Map<String, Object> getLastData(SelectDO selectDO);

    /**
     * 获取历史数据
     *
     * @param selectVisualDO 查询条件
     * @return 历史数据列表
     */
    @InterceptorIgnore(tenantLine = "true")
    List<Map<String, Object>> getHistoryData(SelectVisualDO selectVisualDO);

    /**
     * 获取实时数据
     *
     * @param selectVisualDO 查询条件
     * @return 实时数据列表
     */
    List<Map<String, Object>> getRealtimeData(SelectVisualDO selectVisualDO);

    /**
     * 获取聚合数据
     *
     * @param selectVisualDO 查询条件
     * @return 聚合数据列表
     */
    List<Map<String, Object>> getAggregateData(SelectVisualDO selectVisualDO);

    /**
     * 根据标签获取最新数据
     *
     * @param tagsSelectDO 查询条件
     * @return 最新数据列表
     */
    List<Map<String, Object>> getLastDataByTags(TagsSelectDO tagsSelectDO);

    /**
     * 获取历史数据条数
     *
     * @param selectVisualDO 查询条件
     * @return 数据条数
     */
    @InterceptorIgnore(tenantLine = "true")
    Long getHistoryCount(SelectVisualDO selectVisualDO);
}
