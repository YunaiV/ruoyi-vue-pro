package cn.iocoder.yudao.module.iot.dal.tdengine;

import cn.iocoder.yudao.module.iot.domain.FieldsVo;
import cn.iocoder.yudao.module.iot.domain.SelectDto;
import cn.iocoder.yudao.module.iot.domain.TableDto;
import cn.iocoder.yudao.module.iot.domain.TagsSelectDao;
import cn.iocoder.yudao.module.iot.domain.visual.SelectVisualDto;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
@DS("tdengine")
public interface TdEngineMapper {

    void createDatabase(@Param("dataBaseName") String dataBaseName);

    void createSuperTable(@Param("schemaFields") List<FieldsVo> schemaFields,
                          @Param("tagsFields") List<FieldsVo> tagsFields,
                          @Param("dataBaseName") String dataBaseName,
                          @Param("superTableName") String superTableName);

    void createTable(TableDto tableDto);

    void insertData(TableDto tableDto);

    List<Map<String, Object>> selectByTimestamp(SelectDto selectDto);

    void addColumnForSuperTable(@Param("superTableName") String superTableName,
                                @Param("fieldsVo") FieldsVo fieldsVo);

    void dropColumnForSuperTable(@Param("superTableName") String superTableName,
                                 @Param("fieldsVo") FieldsVo fieldsVo);

    void addTagForSuperTable(@Param("superTableName") String superTableName,
                             @Param("fieldsVo") FieldsVo fieldsVo);

    void dropTagForSuperTable(@Param("superTableName") String superTableName,
                              @Param("fieldsVo") FieldsVo fieldsVo);

    Map<String, Long> getCountByTimestamp(SelectDto selectDto);

    /**
     * 检查表是否存在
     *
     * @param dataBaseName 数据库名称
     * @param tableName    表名称
     */
    Integer checkTableExists(@Param("dataBaseName") String dataBaseName, @Param("tableName") String tableName);

    Map<String, Object> getLastData(SelectDto selectDto);

    List<Map<String, Object>> getHistoryData(SelectVisualDto selectVisualDto);

    List<Map<String, Object>> getRealtimeData(SelectVisualDto selectVisualDto);

    List<Map<String, Object>> getAggregateData(SelectVisualDto selectVisualDto);

    List<Map<String, Object>> getLastDataByTags(TagsSelectDao tagsSelectDao);

    /**
     * 创建超级表
     *
     * @param sql sql
     * @return 返回值
     */
    @InterceptorIgnore(tenantLine = "true")
    Integer createSuperTableDevice(String sql);

}
