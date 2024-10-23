package cn.iocoder.yudao.module.iot.service.tdengine;

import cn.iocoder.yudao.module.iot.domain.FieldsVo;
import cn.iocoder.yudao.module.iot.domain.SelectDto;
import cn.iocoder.yudao.module.iot.domain.TableDto;
import cn.iocoder.yudao.module.iot.domain.TagsSelectDao;
import cn.iocoder.yudao.module.iot.domain.visual.SelectVisualDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TdEngineServiceImpl implements TdEngineService {


    @Override
    public void createDateBase(String dataBaseName) {

    }

    @Override
    public void createSuperTable(List<FieldsVo> schemaFields, List<FieldsVo> tagsFields, String dataBaseName, String superTableName) {

    }

    @Override
    public void createTable(TableDto tableDto) {

    }

    @Override
    public void insertData(TableDto tableDto) {

    }

    @Override
    public List<Map<String, Object>> selectByTimesTamp(SelectDto selectDto) {
        return List.of();
    }

    @Override
    public void addColumnForSuperTable(String superTableName, FieldsVo fieldsVo) {

    }

    @Override
    public void dropColumnForSuperTable(String superTableName, FieldsVo fieldsVo) {

    }

    @Override
    public Long getCountByTimesTamp(SelectDto selectDto) {
        return 0L;
    }

    @Override
    public Integer checkTableExists(SelectDto selectDto) {
        return 0;
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
}