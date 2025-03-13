package cn.iocoder.yudao.module.wms.service.external.storage;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.external.storage.vo.WmsExternalStoragePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.external.storage.vo.WmsExternalStorageSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.external.storage.WmsExternalStorageDO;
import cn.iocoder.yudao.module.wms.dal.mysql.external.storage.WmsExternalStorageMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
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

    /**
     * @sign : 8D1743708CE9B1E6
     */
    @Override
    public WmsExternalStorageDO createExternalStorage(WmsExternalStorageSaveReqVO createReqVO) {
        if (externalStorageMapper.getByName(createReqVO.getName(), true) != null) {
            throw exception(EXTERNAL_STORAGE_NAME_DUPLICATE);
        }
        if (externalStorageMapper.getByCode(createReqVO.getCode(), true) != null) {
            throw exception(EXTERNAL_STORAGE_CODE_DUPLICATE);
        }
        // 插入
        WmsExternalStorageDO externalStorage = BeanUtils.toBean(createReqVO, WmsExternalStorageDO.class);
        externalStorageMapper.insert(externalStorage);
        // 返回
        return externalStorage;
    }

    /**
     * @sign : 95F5B60A0E67165D
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
        // 插入
        WmsExternalStorageDO externalStorage = BeanUtils.toBean(updateReqVO, WmsExternalStorageDO.class);
        externalStorageMapper.updateById(externalStorage);
        // 返回
        return externalStorage;
    }

    @Override
    public void deleteExternalStorage(Long id) {
        // 校验存在
        validateExternalStorageExists(id);
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
