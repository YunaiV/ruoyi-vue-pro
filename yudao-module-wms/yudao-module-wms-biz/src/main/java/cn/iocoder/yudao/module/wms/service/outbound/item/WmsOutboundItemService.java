package cn.iocoder.yudao.module.wms.service.outbound.item;

import java.util.*;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemRespVO;
import jakarta.validation.*;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.item.WmsOutboundItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

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
}
