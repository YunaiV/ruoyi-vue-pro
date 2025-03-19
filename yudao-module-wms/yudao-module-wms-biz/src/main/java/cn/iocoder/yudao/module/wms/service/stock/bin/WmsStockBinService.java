package cn.iocoder.yudao.module.wms.service.stock.bin;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 仓位库存 Service 接口
 *
 * @author 李方捷
 */
public interface WmsStockBinService {

    /**
     * 创建仓位库存
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createStockBin(@Valid WmsStockBinSaveReqVO createReqVO);

    /**
     * 更新仓位库存
     *
     * @param updateReqVO 更新信息
     */
    void updateStockBin(@Valid WmsStockBinSaveReqVO updateReqVO);

    /**
     * 删除仓位库存
     *
     * @param id 编号
     */
    void deleteStockBin(Long id);

    /**
     * 获得仓位库存
     *
     * @param id 编号
     * @return 仓位库存
     */
    WmsStockBinDO getStockBin(Long id);

    /**
     * 获得仓位库存分页
     *
     * @param pageReqVO 分页查询
     * @return 仓位库存分页
     */
    PageResult<WmsStockBinDO> getStockBinPage(WmsStockBinPageReqVO pageReqVO);

}