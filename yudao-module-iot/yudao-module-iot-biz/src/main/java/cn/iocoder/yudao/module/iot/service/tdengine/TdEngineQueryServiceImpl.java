package cn.iocoder.yudao.module.iot.service.tdengine;

import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.SelectVisualDO;
import cn.iocoder.yudao.module.iot.dal.tdengine.TdEngineQueryMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TdEngineQueryServiceImpl implements TdEngineQueryService {

    @Resource
    private TdEngineQueryMapper tdEngineQueryMapper;

    @Override
    public List<Map<String, Object>> getHistoryData(SelectVisualDO selectVisualDO) {
        return tdEngineQueryMapper.getHistoryData(selectVisualDO);
    }

    @Override
    public Long getHistoryCount(SelectVisualDO selectVisualDO) {
        return tdEngineQueryMapper.getHistoryCount(selectVisualDO);
    }
}
