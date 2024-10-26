package cn.iocoder.yudao.module.iot.dal.tdengine;

import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.TdFieldDO;
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

    /**
     * 创建数据库
     *
     * @param dataBaseName 数据库名称
     */
    @InterceptorIgnore(tenantLine = "true")
    void createDatabase(@Param("dataBaseName") String dataBaseName);

    /**
     * 创建超级表
     *
     * @param schemaFields   schema字段
     * @param tagsFields     tags字段
     * @param dataBaseName   数据库名称
     * @param superTableName 超级表名称
     */
    @InterceptorIgnore(tenantLine = "true")
    void createSuperTable(@Param("schemaFields") List<TdFieldDO> schemaFields,
                          @Param("tagsFields") List<TdFieldDO> tagsFields,
                          @Param("dataBaseName") String dataBaseName,
                          @Param("superTableName") String superTableName);

    /**
     * 查看超级表 - 显示当前数据库下的所有超级表信息
     * SQL：SHOW STABLES [LIKE tb_name_wildcard];
     *
     * @param dataBaseName   数据库名称
     * @param superTableName 超级表名称
     */
    @InterceptorIgnore(tenantLine = "true")
    List<Map<String, Object>> showSuperTables(@Param("dataBaseName") String dataBaseName,
                                              @Param("superTableName") String superTableName);

    /**
     * 查看超级表 - 获取超级表的结构信息
     * SQL：DESCRIBE [db_name.]stb_name;
     * <p>
     * * @param dataBaseName 数据库名称
     * * @param superTableName 超级表名称
     */
    @InterceptorIgnore(tenantLine = "true")
    List<Map<String, Object>> describeSuperTable(@Param("dataBaseName") String dataBaseName,
                                                 @Param("superTableName") String superTableName);

    /**
     * 为超级表添加列
     *
     * @param dataBaseName   数据库名称
     * @param superTableName 超级表名称
     * @param field          字段信息
     */
    @InterceptorIgnore(tenantLine = "true")
    void addColumnForSuperTable(@Param("dataBaseName") String dataBaseName,
                                @Param("superTableName") String superTableName,
                                @Param("field") TdFieldDO field);

    /**
     * 为超级表删除列
     *
     * @param dataBaseName   数据库名称
     * @param superTableName 超级表名称
     * @param field          字段信息
     */
    @InterceptorIgnore(tenantLine = "true")
    void dropColumnForSuperTable(@Param("dataBaseName") String dataBaseName,
                                 @Param("superTableName") String superTableName,
                                 @Param("field") TdFieldDO field);

    void createTable(TableDto tableDto);

    void insertData(TableDto tableDto);

    List<Map<String, Object>> selectByTimestamp(SelectDto selectDto);


    void addTagForSuperTable(@Param("superTableName") String superTableName,
                             @Param("fieldsVo") FieldsVo fieldsVo);

    void dropTagForSuperTable(@Param("superTableName") String superTableName,
                              @Param("fieldsVo") FieldsVo fieldsVo);

    Map<String, Long> getCountByTimestamp(SelectDto selectDto);

    Map<String, Object> getLastData(SelectDto selectDto);

    List<Map<String, Object>> getHistoryData(SelectVisualDto selectVisualDto);

    List<Map<String, Object>> getRealtimeData(SelectVisualDto selectVisualDto);

    List<Map<String, Object>> getAggregateData(SelectVisualDto selectVisualDto);

    List<Map<String, Object>> getLastDataByTags(TagsSelectDao tagsSelectDao);


}
