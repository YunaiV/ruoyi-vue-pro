package cn.iocoder.yudao.module.wms.service.external.storage;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.external.storage.vo.WmsExternalStoragePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.external.storage.vo.WmsExternalStorageSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.external.storage.WmsExternalStorageDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.mysql.external.storage.WmsExternalStorageMapper;
import cn.iocoder.yudao.module.wms.service.warehouse.WmsWarehouseService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import java.util.List;
import java.util.Objects;
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

    @Resource
    private WmsWarehouseService warehouseService;

    /**
     * @sign : DB2F714982511B63
     */
    @Override
    public WmsExternalStorageDO createExternalStorage(WmsExternalStorageSaveReqVO createReqVO) {
        if (externalStorageMapper.getByName(createReqVO.getName()) != null) {
            throw exception(EXTERNAL_STORAGE_NAME_DUPLICATE);
        }
        if (externalStorageMapper.getByCode(createReqVO.getCode()) != null) {
            throw exception(EXTERNAL_STORAGE_CODE_DUPLICATE);
        }
        // 插入
        WmsExternalStorageDO externalStorage = BeanUtils.toBean(createReqVO, WmsExternalStorageDO.class);
        externalStorageMapper.insert(externalStorage);
        // 返回
        return externalStorage;
    }

    /**
     * @sign : 1A82B213779A9B14
     */
    @Override
    public WmsExternalStorageDO updateExternalStorage(WmsExternalStorageSaveReqVO updateReqVO) {
        // 校验存在
        WmsExternalStorageDO exists = validateExternalStorageExists(updateReqVO.getId());
        if (!Objects.equals(updateReqVO.getId(), exists.getId()) && Objects.equals(updateReqVO.getName(), exists.getName())) {
            throw exception(EXTERNAL_STORAGE_NAME_DUPLICATE);
        }
        if (!Objects.equals(updateReqVO.getId(), exists.getId()) && Objects.equals(updateReqVO.getCode(), exists.getCode())) {
            throw exception(EXTERNAL_STORAGE_CODE_DUPLICATE);
        }
        // 更新
        WmsExternalStorageDO externalStorage = BeanUtils.toBean(updateReqVO, WmsExternalStorageDO.class);
        externalStorageMapper.updateById(externalStorage);
        // 返回
        return externalStorage;
    }

    /**
     * @sign : 27B44F0FFD1CDC7F
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteExternalStorage(Long id) {
        // 校验存在
        WmsExternalStorageDO externalStorage = validateExternalStorageExists(id);
        // 校验是否被仓库表引用
        List<WmsWarehouseDO> warehouseList = warehouseService.selectByExternalStorageId(id, 1);
        if (!CollectionUtils.isEmpty(warehouseList)) {
            throw exception(EXTERNAL_STORAGE_BE_REFERRED);
        }
        // 唯一索引去重
        externalStorage.setName(externalStorageMapper.flagUKeyAsLogicDelete(externalStorage.getName()));
        externalStorage.setCode(externalStorageMapper.flagUKeyAsLogicDelete(externalStorage.getCode()));
        externalStorageMapper.updateById(externalStorage);
        // 删除
        externalStorageMapper.deleteById(id);
    }

    /**
     * @sign : 8F00B204E9800998
     */
    private WmsExternalStorageDO validateExternalStorageExists(Long id) {
        WmsExternalStorageDO externalStorage = externalStorageMapper.selectById(id);
        if (externalStorage == null) {
            throw exception(EXTERNAL_STORAGE_NOT_EXISTS);
        }
        return externalStorage;
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
