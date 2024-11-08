package cn.iocoder.yudao.module.iot.service.tdengine;

import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.TdFieldDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.TdTableDO;
import cn.iocoder.yudao.module.iot.dal.tdengine.TdEngineSuperTableMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * TD 引擎的超级表 Service 实现类
 */
@Slf4j
@Service
public class TdEngineSuperTableServiceImpl implements TdEngineSuperTableService {

    @Resource
    private TdEngineSuperTableMapper tdEngineSuperTableMapper;

    @Override
    public void createSuperTable(TdTableDO superTable) {
        tdEngineSuperTableMapper.createSuperTable(superTable);
    }

    @Override
    public List<Map<String, Object>> showSuperTables(TdTableDO superTable) {
        return tdEngineSuperTableMapper.showSuperTables(superTable);
    }

    @Override
    public List<Map<String, Object>> describeSuperTable(TdTableDO superTable) {
        return tdEngineSuperTableMapper.describeSuperTable(superTable);
    }

    @Override
    public void addColumnForSuperTable(TdTableDO superTable) {
        tdEngineSuperTableMapper.addColumnForSuperTable(superTable);
    }

    @Override
    public void dropColumnForSuperTable(TdTableDO superTable) {
        tdEngineSuperTableMapper.dropColumnForSuperTable(superTable);
    }

    @Override
    public void modifyColumnWidthForSuperTable(TdTableDO superTable) {
        tdEngineSuperTableMapper.modifyColumnWidthForSuperTable(superTable);
    }

    @Override
    public void addTagForSuperTable(TdTableDO superTable) {
        tdEngineSuperTableMapper.addTagForSuperTable(superTable);
    }

    @Override
    public void dropTagForSuperTable(TdTableDO superTable) {
        tdEngineSuperTableMapper.dropTagForSuperTable(superTable);
    }

    @Override
    public Integer checkSuperTableExists(TdTableDO superTable) {
        List<Map<String, Object>> results = tdEngineSuperTableMapper.showSuperTables(superTable);
        return results == null || results.isEmpty() ? 0 : results.size();
    }

    @Override
    public void addColumnsForSuperTable(TdTableDO superTable) {
        for (TdFieldDO column : superTable.getColumns()) {
            tdEngineSuperTableMapper.addColumnForSuperTable(TdTableDO.builder()
                    .dataBaseName(superTable.getDataBaseName())
                    .superTableName(superTable.getSuperTableName())
                    .column(column)
                    .build());
        }
    }

    @Override
    public void dropColumnsForSuperTable(TdTableDO superTable) {
        for (TdFieldDO column : superTable.getColumns()) {
            tdEngineSuperTableMapper.dropColumnForSuperTable(TdTableDO.builder()
                    .dataBaseName(superTable.getDataBaseName())
                    .superTableName(superTable.getSuperTableName())
                    .column(column)
                    .build());
        }
    }
}