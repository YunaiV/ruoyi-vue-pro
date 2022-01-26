package cn.iocoder.yudao.adminserver.modules.system.service.dict.impl;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.adminserver.modules.system.controller.dict.vo.data.SysDictDataCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.dict.vo.data.SysDictDataExportReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.dict.vo.data.SysDictDataPageReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.dict.vo.data.SysDictDataUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.convert.dict.SysDictDataConvert;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.dict.SysDictTypeDO;
import cn.iocoder.yudao.adminserver.modules.system.dal.mysql.dict.SysDictDataMapper;
import cn.iocoder.yudao.adminserver.modules.system.mq.producer.dict.SysDictDataProducer;
import cn.iocoder.yudao.adminserver.modules.system.service.dict.SysDictDataService;
import cn.iocoder.yudao.adminserver.modules.system.service.dict.SysDictTypeService;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.dict.SysDictDataDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

import static cn.iocoder.yudao.adminserver.modules.system.enums.SysErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 字典数据 Service 实现类
 *
 * @author ruoyi
 */
@Service
@Slf4j
public class SysDictDataServiceImpl implements SysDictDataService {

    /**
     * 排序 dictType > sort
     */
    private static final Comparator<SysDictDataDO> COMPARATOR_TYPE_AND_SORT = Comparator
            .comparing(SysDictDataDO::getDictType)
            .thenComparingInt(SysDictDataDO::getSort);

    @Resource
    private SysDictTypeService dictTypeService;

    @Resource
    private SysDictDataMapper dictDataMapper;

    @Resource
    private SysDictDataProducer dictDataProducer;

    /**
     * 如果字典数据发生变化，从数据库中获取最新的全量字典数据。
     * 如果未发生变化，则返回空
     *
     * @param maxUpdateTime 当前字典数据的最大更新时间
     * @return 字典数据列表
     */
    private List<SysDictDataDO> loadDictDataIfUpdate(Date maxUpdateTime) {
        // 第一步，判断是否要更新。
        if (maxUpdateTime == null) { // 如果更新时间为空，说明 DB 一定有新数据
            log.info("[loadDictDataIfUpdate][首次加载全量字典数据]");
        } else { // 判断数据库中是否有更新的字典数据
            if (!dictDataMapper.selectExistsByUpdateTimeAfter(maxUpdateTime)) {
                return null;
            }
            log.info("[loadDictDataIfUpdate][增量加载全量字典数据]");
        }
        // 第二步，如果有更新，则从数据库加载所有字典数据
        return dictDataMapper.selectList();
    }

    @Override
    public List<SysDictDataDO> getDictDatas() {
        List<SysDictDataDO> list = dictDataMapper.selectList();
        list.sort(COMPARATOR_TYPE_AND_SORT);
        return list;
    }

    @Override
    public PageResult<SysDictDataDO> getDictDataPage(SysDictDataPageReqVO reqVO) {
        return dictDataMapper.selectPage(reqVO);
    }

    @Override
    public List<SysDictDataDO> getDictDatas(SysDictDataExportReqVO reqVO) {
        List<SysDictDataDO> list = dictDataMapper.selectList(reqVO);
        list.sort(COMPARATOR_TYPE_AND_SORT);
        return list;
    }

    @Override
    public SysDictDataDO getDictData(Long id) {
        return dictDataMapper.selectById(id);
    }

    @Override
    public Long createDictData(SysDictDataCreateReqVO reqVO) {
        // 校验正确性
        this.checkCreateOrUpdate(null, reqVO.getValue(), reqVO.getDictType());
        // 插入字典类型
        SysDictDataDO dictData = SysDictDataConvert.INSTANCE.convert(reqVO);
        dictDataMapper.insert(dictData);
        // 发送刷新消息
        dictDataProducer.sendDictDataRefreshMessage();
        return dictData.getId();
    }

    @Override
    public void updateDictData(SysDictDataUpdateReqVO reqVO) {
        // 校验正确性
        this.checkCreateOrUpdate(reqVO.getId(), reqVO.getValue(), reqVO.getDictType());
        // 更新字典类型
        SysDictDataDO updateObj = SysDictDataConvert.INSTANCE.convert(reqVO);
        dictDataMapper.updateById(updateObj);
        // 发送刷新消息
        dictDataProducer.sendDictDataRefreshMessage();
    }

    @Override
    public void deleteDictData(Long id) {
        // 校验是否存在
        this.checkDictDataExists(id);
        // 删除字典数据
        dictDataMapper.deleteById(id);
        // 发送刷新消息
        dictDataProducer.sendDictDataRefreshMessage();
    }

    @Override
    public int countByDictType(String dictType) {
        return dictDataMapper.selectCountByDictType(dictType);
    }


    private void checkCreateOrUpdate(Long id, String value, String dictType) {
        // 校验自己存在
        checkDictDataExists(id);
        // 校验字典类型有效
        checkDictTypeValid(dictType);
        // 校验字典数据的值的唯一性
        checkDictDataValueUnique(id, dictType, value);
    }

    @VisibleForTesting
    public void checkDictDataValueUnique(Long id, String dictType, String value) {
        SysDictDataDO dictData = dictDataMapper.selectByDictTypeAndValue(dictType, value);
        if (dictData == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的字典数据
        if (id == null) {
            throw exception(DICT_DATA_VALUE_DUPLICATE);
        }
        if (!dictData.getId().equals(id)) {
            throw exception(DICT_DATA_VALUE_DUPLICATE);
        }
    }

    @VisibleForTesting
    public void checkDictDataExists(Long id) {
        if (id == null) {
            return;
        }
        SysDictDataDO dictData = dictDataMapper.selectById(id);
        if (dictData == null) {
            throw exception(DICT_DATA_NOT_EXISTS);
        }
    }

    @VisibleForTesting
    public void checkDictTypeValid(String type) {
        SysDictTypeDO dictType = dictTypeService.getDictType(type);
        if (dictType == null) {
            throw exception(DICT_TYPE_NOT_EXISTS);
        }
        if (!CommonStatusEnum.ENABLE.getStatus().equals(dictType.getStatus())) {
            throw exception(DICT_TYPE_NOT_ENABLE);
        }
    }

}
