package cn.iocoder.yudao.module.system.service.dict;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.dict.vo.data.DictDataPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.dict.vo.data.DictDataSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.dict.DictDataDO;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * 字典数据 Service 接口
 *
 * @author ruoyi
 */
public interface DictDataService {

    /**
     * 创建字典数据
     *
     * @param createReqVO 字典数据信息
     * @return 字典数据编号
     */
    Long createDictData(DictDataSaveReqVO createReqVO);

    /**
     * 更新字典数据
     *
     * @param updateReqVO 字典数据信息
     */
    void updateDictData(DictDataSaveReqVO updateReqVO);

    /**
     * 删除字典数据
     *
     * @param id 字典数据编号
     */
    void deleteDictData(Long id);

    /**
     * 获得字典数据列表
     *
     * @param status   状态
     * @param dictType 字典类型
     * @return 字典数据全列表
     */
    List<DictDataDO> getDictDataList(@Nullable Integer status, @Nullable String dictType);

    /**
     * 获得字典数据分页列表
     *
     * @param pageReqVO 分页请求
     * @return 字典数据分页列表
     */
    PageResult<DictDataDO> getDictDataPage(DictDataPageReqVO pageReqVO);

    /**
     * 获得字典数据详情
     *
     * @param id 字典数据编号
     * @return 字典数据
     */
    DictDataDO getDictData(Long id);

    /**
     * 获得指定字典类型的数据数量
     *
     * @param dictType 字典类型
     * @return 数据数量
     */
    long getDictDataCountByDictType(String dictType);

    /**
     * 校验字典数据们是否有效。如下情况，视为无效：
     * 1. 字典数据不存在
     * 2. 字典数据被禁用
     *
     * @param dictType 字典类型
     * @param values   字典数据值的数组
     */
    void validateDictDataList(String dictType, Collection<String> values);

    /**
     * 获得指定的字典数据
     *
     * @param dictType 字典类型
     * @param value    字典数据值
     * @return 字典数据
     */
    DictDataDO getDictData(String dictType, String value);

    /**
     * 解析获得指定的字典数据，从缓存中
     *
     * @param dictType 字典类型
     * @param label    字典数据标签
     * @return 字典数据
     */
    DictDataDO parseDictData(String dictType, String label);

    /**
     * 获得指定数据类型的字典数据列表
     *
     * @param dictType 字典类型
     * @return 字典数据列表
     */
    List<DictDataDO> getDictDataListByDictType(String dictType);

}
