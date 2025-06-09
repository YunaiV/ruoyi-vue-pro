package cn.iocoder.yudao.module.wms.service.stockcheck;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stockcheck.bin.vo.WmsStockCheckProductExcelVO;
import cn.iocoder.yudao.module.wms.controller.admin.stockcheck.vo.WmsStockCheckPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stockcheck.vo.WmsStockCheckRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stockcheck.vo.WmsStockCheckSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stockcheck.WmsStockCheckDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.enums.stock.check.WmsStockCheckAuditStatus;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 盘点 Service 接口
 *
 * @author 李方捷
 */
public interface WmsStockCheckService {

    /**
     * 创建盘点
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsStockCheckDO createStockCheck(@Valid WmsStockCheckSaveReqVO createReqVO);

//    /**
//     * 更新盘点
//     *
//     * @param updateReqVO 更新信息
//     */
//    WmsStockCheckDO updateStockCheck(@Valid WmsStockCheckSaveReqVO updateReqVO);

    /**
     * 删除盘点
     *
     * @param id 编号
     */
    void deleteStockCheck(Long id);

    /**
     * 获得盘点
     *
     * @param id 编号
     * @return 盘点
     */
    WmsStockCheckDO getStockCheck(Long id);

    /**
     * 获得盘点分页
     *
     * @param pageReqVO 分页查询
     * @return 盘点分页
     */
    PageResult<WmsStockCheckDO> getStockCheckPage(WmsStockCheckPageReqVO pageReqVO);

    /**
     * 按 ID 集合查询 WmsStockCheckDO
     */
    List<WmsStockCheckDO> selectByIds(List<Long> idList);

    void assembleWarehouse(List<WmsStockCheckRespVO> list);

    WmsStockCheckDO updateOutboundAuditStatus(Long id, Integer status);

    void approve(WmsStockCheckAuditStatus.Event event, WmsApprovalReqVO approvalReqVO);

    WmsStockCheckDO validateStockCheckExists(Long id);

    void assembleApprovalHistory(List<WmsStockCheckRespVO> list);

    List<WmsStockBinRespVO> parseProductExcel(WmsWarehouseDO wmsWarehouseDO, List<WmsStockCheckProductExcelVO> impVOList);
}
