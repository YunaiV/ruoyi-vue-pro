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
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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

    @Resource
    private SysDictTypeService dictTypeService;

    @Resource
    private SysDictDataMapper dictDataMapper;

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

}
