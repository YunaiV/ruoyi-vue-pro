package cn.iocoder.yudao.module.wms.service.stockcheck.bin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.stockcheck.bin.vo.WmsStockCheckBinPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stockcheck.bin.vo.WmsStockCheckBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stockcheck.bin.vo.WmsStockCheckBinSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stockcheck.WmsStockCheckDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stockcheck.bin.WmsStockCheckBinDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 库位盘点 Service 接口
 *
 * @author 李方捷
 */
public interface WmsStockCheckBinService {

    /**
     * 创建库位盘点
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsStockCheckBinDO createStockCheckBin(@Valid WmsStockCheckBinSaveReqVO createReqVO);

    /**
     * 更新库位盘点
     *
     * @param updateReqVO 更新信息
     */
    WmsStockCheckBinDO updateStockCheckBin(@Valid WmsStockCheckBinSaveReqVO updateReqVO);

    /**
     * 删除库位盘点
     *
     * @param id 编号
     */
    void deleteStockCheckBin(Long id);

    /**
     * 获得库位盘点
     *
     * @param id 编号
     * @return 库位盘点
     */
    WmsStockCheckBinDO getStockCheckBin(Long id);

    /**
     * 获得库位盘点分页
     *
     * @param pageReqVO 分页查询
     * @return 库位盘点分页
     */
    PageResult<WmsStockCheckBinDO> getStockCheckBinPage(WmsStockCheckBinPageReqVO pageReqVO);

    /**
     * 按 ID 集合查询 WmsStockCheckBinDO
     */
    List<WmsStockCheckBinDO> selectByIds(List<Long> idList);

    List<WmsStockCheckBinDO> selectByStockCheckId(Long id);

    void assembleProduct(List<WmsStockCheckBinRespVO> binItemList);

    void assembleBin(List<WmsStockCheckBinRespVO> binItemList);

    void updateActualQuantity(List<WmsStockCheckBinSaveReqVO> updateReqVOList);

    void saveStockCheckBinList(WmsStockCheckDO stockCheck, List<WmsStockCheckBinDO> doList);

    /**
     * 追加盘点库位
     */
    Boolean appendStockCheckBin(@Valid List<WmsStockCheckBinSaveReqVO> createReqVOList);

    void updateBatch(List<WmsStockCheckBinDO> wmsStockCheckBinDOList);
}
