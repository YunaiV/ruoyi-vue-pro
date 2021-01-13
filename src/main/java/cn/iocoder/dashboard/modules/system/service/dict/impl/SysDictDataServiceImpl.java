package cn.iocoder.dashboard.modules.system.service.dict.impl;

import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.data.SysDictDataCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.data.SysDictDataPageReqVO;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.data.SysDictDataUpdateReqVO;
import cn.iocoder.dashboard.modules.system.convert.dict.SysDictDataConvert;
import cn.iocoder.dashboard.modules.system.dal.mysql.dao.dict.SysDictDataMapper;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dict.SysDictDataDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dict.SysDictTypeDO;
import cn.iocoder.dashboard.modules.system.service.dict.SysDictDataService;
import cn.iocoder.dashboard.modules.system.service.dict.SysDictTypeService;
import com.google.common.collect.ImmutableTable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static cn.iocoder.dashboard.modules.system.enums.SysErrorCodeConstants.*;

/**
 * 字典数据 Service 实现类
 *
 * @author ruoyi
 */
@Service
public class SysDictDataServiceImpl implements SysDictDataService {

    private static final Comparator<SysDictDataDO> COMPARATOR_TYPE_AND_SORT = Comparator
            .comparing(SysDictDataDO::getDictType)
            .thenComparingInt(SysDictDataDO::getSort);

    /**
     * 字典数据缓存，第二个 key 使用 label
     *
     * key1：字典类型 dictType
     * key2：字典标签 label
     */
    private ImmutableTable<String, String, SysDictDataDO> labelDictDataCache;
    /**
     * 字典数据缓存，第二个 key 使用 value
     *
     * key1：字典类型 dictType
     * key2：字典值 value
     */
    private ImmutableTable<String, String, SysDictDataDO> valueDictDataCache;

    @Resource
    private SysDictTypeService dictTypeService;

    @Resource
    private SysDictDataMapper dictDataMapper;

    @Override
    @PostConstruct
    public void init() {
        // 获得字典数据
        List<SysDictDataDO> list = this.listDictDatas();
        // 构建缓存
        ImmutableTable.Builder<String, String, SysDictDataDO> labelDictDataBuilder = ImmutableTable.builder();
        ImmutableTable.Builder<String, String, SysDictDataDO> valueDictDataBuilder = ImmutableTable.builder();
        list.forEach(dictData -> {
            labelDictDataBuilder.put(dictData.getDictType(), dictData.getLabel(), dictData);
            valueDictDataBuilder.put(dictData.getDictType(), dictData.getValue(), dictData);
        });
        labelDictDataCache = labelDictDataBuilder.build();
        valueDictDataCache = valueDictDataBuilder.build();
    }

    @Override
    public List<SysDictDataDO> listDictDatas() {
        List<SysDictDataDO> list = dictDataMapper.selectList();
        list.sort(COMPARATOR_TYPE_AND_SORT);
        return list;
    }

    @Override
    public PageResult<SysDictDataDO> pageDictDatas(SysDictDataPageReqVO reqVO) {
        return SysDictDataConvert.INSTANCE.convertPage02(dictDataMapper.selectList(reqVO));
    }

    @Override
    public SysDictDataDO getDictData(Long id) {
        return dictDataMapper.selectById(id);
    }

    @Override
    public Long createDictData(SysDictDataCreateReqVO reqVO) {
        // 校验正确性
        this.checkCreateOrUpdate(null, reqVO.getLabel(), reqVO.getDictType());
        // 插入字典类型
        SysDictDataDO dictData = SysDictDataConvert.INSTANCE.convert(reqVO);
        dictDataMapper.insert(dictData);
        return dictData.getId();
    }

    @Override
    public void updateDictData(SysDictDataUpdateReqVO reqVO) {
        // 校验正确性
        this.checkCreateOrUpdate(reqVO.getId(), reqVO.getLabel(), reqVO.getDictType());
        // 更新字典类型
        SysDictDataDO updateObj = SysDictDataConvert.INSTANCE.convert(reqVO);
        dictDataMapper.updateById(updateObj);
    }

    @Override
    public void deleteDictData(Long id) {
        // 校验是否存在
        this.checkDictDataExists(id);
        // 删除字典数据
        dictDataMapper.deleteById(id);
    }

    @Override
    public int countByDictType(String dictType) {
        return dictDataMapper.selectCountByDictType(dictType);
    }

    private void checkCreateOrUpdate(Long id, String label, String dictType) {
        // 校验自己存在
        checkDictDataExists(id);
        // 校验字典数据的值的唯一性
        checkDictDataValueUnique(id, label);
        // 校验字典类型有效
        checkDictTypeValid(dictType);
    }

    private void checkDictDataValueUnique(Long id, String label) {
        SysDictDataDO dictData = dictDataMapper.selectByLabel(label);
        if (dictData == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的字典数据
        if (id == null) {
            throw ServiceExceptionUtil.exception(DICT_DATA_VALUE_DUPLICATE);
        }
        if (!dictData.getId().equals(id)) {
            throw ServiceExceptionUtil.exception(DICT_DATA_VALUE_DUPLICATE);
        }
    }

    private void checkDictDataExists(Long id) {
        if (id == null) {
            return;
        }
        SysDictDataDO dictData = dictDataMapper.selectById(id);
        if (dictData == null) {
            throw ServiceExceptionUtil.exception(DICT_DATA_NOT_FOUND);
        }
    }

    private void checkDictTypeValid(String type) {
        SysDictTypeDO dictType = dictTypeService.getDictType(type);
        if (dictType == null) {
            throw ServiceExceptionUtil.exception(DICT_TYPE_NOT_FOUND);
        }
        if (!CommonStatusEnum.ENABLE.getStatus().equals(dictType.getStatus())) {
            throw ServiceExceptionUtil.exception(DICT_TYPE_NOT_ENABLE);
        }
    }

    @Override
    public SysDictDataDO getDictDataFromCache(String type, String value) {
        return valueDictDataCache.get(type, value);
    }

    @Override
    public SysDictDataDO parseDictDataFromCache(String type, String label) {
        return labelDictDataCache.get(type, label);
    }

    @Override
    public List<SysDictDataDO> listDictDatasFromCache(String type) {
        return new ArrayList<>(labelDictDataCache.row(type).values());
    }

}
