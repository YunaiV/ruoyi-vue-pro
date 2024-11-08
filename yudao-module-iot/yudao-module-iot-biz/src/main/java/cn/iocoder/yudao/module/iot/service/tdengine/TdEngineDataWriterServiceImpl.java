package cn.iocoder.yudao.module.iot.service.tdengine;

import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.TdTableDO;
import cn.iocoder.yudao.module.iot.dal.tdengine.TdEngineDataWriterMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * TD 引擎的数据写入 Service 实现类
 */
@Service
public class TdEngineDataWriterServiceImpl implements TdEngineDataWriterService {

    @Resource
    private TdEngineDataWriterMapper tdEngineDataWriterMapper;

    @Override
    public void insertData(TdTableDO table) {
        tdEngineDataWriterMapper.insertData(table);
    }
}
