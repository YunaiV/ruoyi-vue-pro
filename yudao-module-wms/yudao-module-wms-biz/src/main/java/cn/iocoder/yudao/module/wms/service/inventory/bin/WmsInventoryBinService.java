package cn.iocoder.yudao.module.wms.service.inventory.bin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.bin.vo.WmsInventoryBinPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.bin.vo.WmsInventoryBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.bin.vo.WmsInventoryBinSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.bin.WmsInventoryBinDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 库位盘点 Service 接口
 *
 * @author 李方捷
 */
public interface WmsInventoryBinService {

    /**
     * 创建库位盘点
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsInventoryBinDO createInventoryBin(@Valid WmsInventoryBinSaveReqVO createReqVO);

    /**
     * 更新库位盘点
     *
     * @param updateReqVO 更新信息
     */
    WmsInventoryBinDO updateInventoryBin(@Valid WmsInventoryBinSaveReqVO updateReqVO);

    /**
     * 删除库位盘点
     *
     * @param id 编号
     */
    void deleteInventoryBin(Long id);

    /**
     * 获得库位盘点
     *
     * @param id 编号
     * @return 库位盘点
     */
    WmsInventoryBinDO getInventoryBin(Long id);

    /**
     * 获得库位盘点分页
     *
     * @param pageReqVO 分页查询
     * @return 库位盘点分页
     */
    PageResult<WmsInventoryBinDO> getInventoryBinPage(WmsInventoryBinPageReqVO pageReqVO);

    /**
     * 按 ID 集合查询 WmsInventoryBinDO
     */
    List<WmsInventoryBinDO> selectByIds(List<Long> idList);

    List<WmsInventoryBinDO> selectByInventoryId(Long id);

    void assembleProduct(List<WmsInventoryBinRespVO> binItemList);

    void assembleBin(List<WmsInventoryBinRespVO> binItemList);

    void updateActualQuantity(List<WmsInventoryBinSaveReqVO> updateReqVOList);
}
