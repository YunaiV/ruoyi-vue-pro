package cn.iocoder.yudao.module.iot.service.tdengine;

import cn.iocoder.yudao.module.iot.dal.tdengine.TdEngineDatabaseMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * TD 引擎的数据库 Service 实现类
 */
@Service
@Slf4j
public class TdEngineDatabaseServiceImpl implements TdEngineDatabaseService {

    @Resource
    private TdEngineDatabaseMapper tdEngineDatabaseMapper;

    @Override
    public void createDatabase(String dataBaseName) {
        tdEngineDatabaseMapper.createDatabase(dataBaseName);
    }
}