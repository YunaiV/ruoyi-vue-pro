package cn.iocoder.yudao.module.iot.service.tdengine;

import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.TdTableDO;
import cn.iocoder.yudao.module.iot.dal.tdengine.TdEngineTableMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * TD 引擎的表 Service 实现类
 */
@Slf4j
@Service
public class TdEngineTableServiceImpl implements TdEngineTableService {

    @Resource
    private TdEngineTableMapper tdEngineTableMapper;

    @Override
    public void createTable(TdTableDO table) {
        tdEngineTableMapper.createTable(table);
    }
}
