package cn.iocoder.yudao.module.wms.service.external.storage;

import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.wms.controller.admin.external.storage.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.external.storage.WmsExternalStorageDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.wms.dal.mysql.external.storage.WmsExternalStorageMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

/**
 * 外部存储库 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsExternalStorageServiceImpl implements WmsExternalStorageService {

    @Resource
    private WmsExternalStorageMapper externalStorageMapper;

    @Override
    public Long createExternalStorage(WmsExternalStorageSaveReqVO createReqVO) {
        // 插入
        WmsExternalStorageDO externalStorage = BeanUtils.toBean(createReqVO, WmsExternalStorageDO.class);
        externalStorageMapper.insert(externalStorage);
        // 返回
        return externalStorage.getId();
    }

    @Override
    public void updateExternalStorage(WmsExternalStorageSaveReqVO updateReqVO) {
        // 校验存在
        validateExternalStorageExists(updateReqVO.getId());
        // 更新
        WmsExternalStorageDO updateObj = BeanUtils.toBean(updateReqVO, WmsExternalStorageDO.class);
        externalStorageMapper.updateById(updateObj);
    }

    @Override
    public void deleteExternalStorage(Long id) {
        // 校验存在
        validateExternalStorageExists(id);
        // 删除
        externalStorageMapper.deleteById(id);
    }

    private void validateExternalStorageExists(Long id) {
        if (externalStorageMapper.selectById(id) == null) {
            // throw exception(EXTERNAL_STORAGE_NOT_EXISTS);
        }
    }

    @Override
    public WmsExternalStorageDO getExternalStorage(Long id) {
        return externalStorageMapper.selectById(id);
    }

    @Override
    public PageResult<WmsExternalStorageDO> getExternalStoragePage(WmsExternalStoragePageReqVO pageReqVO) {
        return externalStorageMapper.selectPage(pageReqVO);
    }

}