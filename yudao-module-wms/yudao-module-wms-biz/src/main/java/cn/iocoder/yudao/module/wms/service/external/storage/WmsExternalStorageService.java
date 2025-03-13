package cn.iocoder.yudao.module.wms.service.external.storage;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.wms.controller.admin.external.storage.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.external.storage.WmsExternalStorageDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 外部存储库 Service 接口
 *
 * @author 李方捷
 */
public interface WmsExternalStorageService {

    /**
     * 创建外部存储库
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsExternalStorageDO createExternalStorage(@Valid WmsExternalStorageSaveReqVO createReqVO);

    /**
     * 更新外部存储库
     *
     * @param updateReqVO 更新信息
     */
    WmsExternalStorageDO updateExternalStorage(@Valid WmsExternalStorageSaveReqVO updateReqVO);

    /**
     * 删除外部存储库
     *
     * @param id 编号
     */
    void deleteExternalStorage(Long id);

    /**
     * 获得外部存储库
     *
     * @param id 编号
     * @return 外部存储库
     */
    WmsExternalStorageDO getExternalStorage(Long id);

    /**
     * 获得外部存储库分页
     *
     * @param pageReqVO 分页查询
     * @return 外部存储库分页
     */
    PageResult<WmsExternalStorageDO> getExternalStoragePage(WmsExternalStoragePageReqVO pageReqVO);
}
