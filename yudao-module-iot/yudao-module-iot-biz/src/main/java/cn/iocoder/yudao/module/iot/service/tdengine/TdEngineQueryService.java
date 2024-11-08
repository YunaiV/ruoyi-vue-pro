package cn.iocoder.yudao.module.iot.service.tdengine;

import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.SelectVisualDO;

import java.util.List;
import java.util.Map;

/**
 * TD 引擎的查询 Service 接口
 */
public interface TdEngineQueryService {

    /**
     * 获取历史数据
     *
     * @param selectVisualDO 查询条件
     * @return 历史数据列表
     */
    List<Map<String, Object>> getHistoryData(SelectVisualDO selectVisualDO);

    /**
     * 获取历史数据条数
     *
     * @param selectVisualDO 查询条件
     * @return 数据条数
     */
    Long getHistoryCount(SelectVisualDO selectVisualDO);
}
