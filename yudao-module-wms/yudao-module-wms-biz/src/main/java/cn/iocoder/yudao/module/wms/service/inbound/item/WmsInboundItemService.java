package cn.iocoder.yudao.module.wms.service.inbound.item;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 入库单详情 Service 接口
 *
 * @author 李方捷
 */
public interface WmsInboundItemService {

    /**
     * 创建入库单详情
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsInboundItemDO createInboundItem(@Valid WmsInboundItemSaveReqVO createReqVO);

    /**
     * 更新入库单详情
     *
     * @param updateReqVO 更新信息
     */
    WmsInboundItemDO updateInboundItem(@Valid WmsInboundItemSaveReqVO updateReqVO);

    /**
     * 删除入库单详情
     *
     * @param id 编号
     */
    void deleteInboundItem(Long id);

    /**
     * 获得入库单详情
     *
     * @param id 编号
     * @return 入库单详情
     */
    WmsInboundItemDO getInboundItem(Long id);

    /**
     * 获得入库单详情分页
     *
     * @param pageReqVO 分页查询
     * @return 入库单详情分页
     */
    PageResult<WmsInboundItemDO> getInboundItemPage(WmsInboundItemPageReqVO pageReqVO);

    /**
     * 按 inboundId 查询 WmsInboundItemDO
     */
    List<WmsInboundItemDO> selectByInboundId(Long inboundId, int limit);

    /**
     * 按 inboundId 查询 WmsInboundItemDO
     */
    default List<WmsInboundItemDO> selectByInboundId(Long inboundId) {
        return selectByInboundId(inboundId, Integer.MAX_VALUE);
    }

    void updateActualQuantity(List<WmsInboundItemSaveReqVO> updateReqVOList);

    List<WmsInboundItemDO> selectByIds(List<Long> ids);

    void updateById(WmsInboundItemDO inboundItemDO);
}
