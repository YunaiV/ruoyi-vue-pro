package cn.iocoder.yudao.module.wms.service.outbound.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo.WmsOutboundItemImportExcelVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo.WmsOutboundItemPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo.WmsOutboundItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo.WmsOutboundItemSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.item.WmsOutboundItemDO;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 出库单详情 Service 接口
 *
 * @author 李方捷
 */
public interface WmsOutboundItemService {

    /**
     * 创建出库单详情
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsOutboundItemDO createOutboundItem(@Valid WmsOutboundItemSaveReqVO createReqVO);

    /**
     * 更新出库单详情
     *
     * @param updateReqVO 更新信息
     */
    WmsOutboundItemDO updateOutboundItem(@Valid WmsOutboundItemSaveReqVO updateReqVO);

    /**
     * 删除出库单详情
     *
     * @param id 编号
     */
    void deleteOutboundItem(Long id);

    /**
     * 获得出库单详情
     *
     * @param id 编号
     * @return 出库单详情
     */
    WmsOutboundItemDO getOutboundItem(Long id);

    /**
     * 获得出库单详情分页
     *
     * @param pageReqVO 分页查询
     * @return 出库单详情分页
     */
    PageResult<WmsOutboundItemDO> getOutboundItemPage(WmsOutboundItemPageReqVO pageReqVO);

    List<WmsOutboundItemDO> selectByOutboundId(Long outboundId);

    void assembleProducts(List<WmsOutboundItemRespVO> itemList);

    void updateActualQuantity(List<WmsOutboundItemSaveReqVO> updateReqVOList);

    /**
     * 按 ID 集合查询 WmsOutboundItemDO
     */
    List<WmsOutboundItemDO> selectByIds(List<Long> idList);

    /**
     * 装配库位
     **/
    void assembleBin(List<WmsOutboundItemRespVO> itemList);
    /**
     * 装配产品ID
     **/
    void assembleProductIds(List<WmsOutboundItemImportExcelVO> impVOList);

    /**
     * 装配部门
     **/
    void assembleDept(List<WmsOutboundItemRespVO> voList);

    /**
     * 装配公司
     **/
    void assembleCompany(List<WmsOutboundItemRespVO> voList);
}
