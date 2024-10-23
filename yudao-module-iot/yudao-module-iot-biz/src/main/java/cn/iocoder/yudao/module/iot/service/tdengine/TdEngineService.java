package cn.iocoder.yudao.module.iot.service.tdengine;

import cn.iocoder.yudao.module.iot.domain.FieldsVo;
import cn.iocoder.yudao.module.iot.domain.SelectDto;
import cn.iocoder.yudao.module.iot.domain.TableDto;
import cn.iocoder.yudao.module.iot.domain.TagsSelectDao;
import cn.iocoder.yudao.module.iot.domain.visual.SelectVisualDto;

import java.util.List;
import java.util.Map;

/**
 * TdEngineService
 */
public interface TdEngineService {

    /**
     * 创建数据库
     *
     * @param dataBaseName 数据库名称
     * @throws Exception 异常
     */
    void createDateBase(String dataBaseName) throws Exception;

    /**
     * 创建超级表
     *
     * @param schemaFields   schema字段
     * @param tagsFields     tags字段
     * @param dataBaseName   数据库名称
     * @param superTableName 超级表名称
     * @throws Exception 异常
     */
    void createSuperTable(List<FieldsVo> schemaFields, List<FieldsVo> tagsFields, String dataBaseName,
                          String superTableName) throws Exception;

    /**
     * 创建表
     *
     * @param tableDto 表信息
     * @throws Exception 异常
     */
    void createTable(TableDto tableDto) throws Exception;

    /**
     * 插入数据
     *
     * @param tableDto 表信息
     * @throws Exception 异常
     */
    void insertData(TableDto tableDto) throws Exception;

    /**
     * 根据时间戳查询数据
     *
     * @param selectDto 查询条件
     * @return 数据
     * @throws Exception 异常
     */
    List<Map<String, Object>> selectByTimesTamp(SelectDto selectDto) throws Exception;

    /**
     * 为超级表添加列
     *
     * @param superTableName 超级表名称
     * @param fieldsVo       字段信息
     * @throws Exception 异常
     */
    void addColumnForSuperTable(String superTableName, FieldsVo fieldsVo) throws Exception;

    /**
     * 为超级表删除列
     *
     * @param superTableName 超级表名称
     * @param fieldsVo       字段信息
     * @throws Exception 异常
     */
    void dropColumnForSuperTable(String superTableName, FieldsVo fieldsVo) throws Exception;

    /**
     * 为超级表添加tag
     */
    Long getCountByTimesTamp(SelectDto selectDto) throws Exception;

    /**
     * 检查表是否存在
     *
     * @return 1存在 0不存在
     * @throws Exception 异常
     */
    Integer checkTableExists(SelectDto selectDto) throws Exception;

    /**
     * 初始化超级表
     *
     * @param msg 消息
     * @throws Exception 异常
     */
    void initSTableFrame(String msg) throws Exception;

    /**
     * 获取最新数据
     *
     * @param selectDto 查询条件
     * @return 数据
     * @throws Exception 异常
     */
    Map<String, Object> getLastData(SelectDto selectDto) throws Exception;

    /**
     * 根据tag查询最新数据
     *
     * @param tagsSelectDao 查询条件
     * @return 数据
     */
    Map<String, Map<String, Object>> getLastDataByTags(TagsSelectDao tagsSelectDao);

    /**
     * 获取历史数据
     *
     * @param selectVisualDto 查询条件
     * @return 数据
     */
    List<Map<String, Object>> getHistoryData(SelectVisualDto selectVisualDto);

    /**
     * 获取实时数据
     *
     * @param selectVisualDto 查询条件
     * @return 数据
     */
    List<Map<String, Object>> getRealtimeData(SelectVisualDto selectVisualDto);

    /**
     * 获取聚合数据
     *
     * @param selectVisualDto 查询条件
     * @return 数据
     */
    List<Map<String, Object>> getAggregateData(SelectVisualDto selectVisualDto);
}
