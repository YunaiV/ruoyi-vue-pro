package cn.iocoder.yudao.module.iot.service.tdengine;

import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.TdFieldDO;
import cn.iocoder.yudao.module.iot.dal.tdengine.TdEngineMapper;
import cn.iocoder.yudao.module.iot.domain.SelectDto;
import cn.iocoder.yudao.module.iot.domain.TableDto;
import cn.iocoder.yudao.module.iot.domain.TagsSelectDao;
import cn.iocoder.yudao.module.iot.domain.visual.SelectVisualDto;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TdEngineServiceImpl implements TdEngineService {

    @Resource
    private TdEngineMapper tdEngineMapper;

    @Override
    public void createDateBase(String dataBaseName) {
        tdEngineMapper.createDatabase(dataBaseName);
    }

    @Override
    public void createSuperTable(List<TdFieldDO> schemaFields, List<TdFieldDO> tagsFields, String dataBaseName, String superTableName) {
        tdEngineMapper.createSuperTable(schemaFields, tagsFields, dataBaseName, superTableName);
    }

    @Override
    public void createTable(TableDto tableDto) {
        tdEngineMapper.createTable(tableDto);
    }

    @Override
    public void insertData(TableDto tableDto) {
        tdEngineMapper.insertData(tableDto);
    }

    @Override
    public List<Map<String, Object>> selectByTimesTamp(SelectDto selectDto) {
        return List.of();
    }

    @Override
    public void addColumnForSuperTable(String dataBaseName,String superTableName, List<TdFieldDO> fields) {
        for (TdFieldDO field : fields) {
            tdEngineMapper.addColumnForSuperTable(dataBaseName,superTableName, field);
        }
    }

    @Override
    public void dropColumnForSuperTable(String dataBaseName,String superTableName, List<TdFieldDO> fields) {
        for (TdFieldDO field : fields) {
            tdEngineMapper.dropColumnForSuperTable(dataBaseName,superTableName, field);
        }
    }

    @Override
    public Long getCountByTimesTamp(SelectDto selectDto) {
        return 0L;
    }

    @Override
    public void initSTableFrame(String msg) {

    }

    @Override
    public Map<String, Object> getLastData(SelectDto selectDto) {
        return Map.of();
    }

    @Override
    public Map<String, Map<String, Object>> getLastDataByTags(TagsSelectDao tagsSelectDao) {
        return Map.of();
    }

    @Override
    public List<Map<String, Object>> getHistoryData(SelectVisualDto selectVisualDto) {
        return List.of();
    }

    @Override
    public List<Map<String, Object>> getRealtimeData(SelectVisualDto selectVisualDto) {
        return List.of();
    }

    @Override
    public List<Map<String, Object>> getAggregateData(SelectVisualDto selectVisualDto) {
        return List.of();
    }

    @Override
    public Integer checkSuperTableExists(String dataBaseName, String superTableName) {
        List<Map<String, Object>> results = tdEngineMapper.showSuperTables(dataBaseName, superTableName);
        return results == null || results.isEmpty() ? 0 : results.size();
    }

    @Override
    public List<Map<String, Object>> describeSuperTable(String dataBaseName, String superTableName) {
        return tdEngineMapper.describeSuperTable(dataBaseName, superTableName);
    }
}