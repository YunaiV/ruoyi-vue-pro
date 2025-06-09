package cn.iocoder.yudao.module.wms.service.exchange.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.item.vo.WmsExchangeItemPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.item.vo.WmsExchangeItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.item.vo.WmsExchangeItemSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.item.WmsExchangeItemDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 良次换货详情 Service 接口
 *
 * @author 李方捷
 */
public interface WmsExchangeItemService {

    /**
     * 创建良次换货详情
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsExchangeItemDO createExchangeItem(@Valid WmsExchangeItemSaveReqVO createReqVO);

    /**
     * 更新良次换货详情
     *
     * @param updateReqVO 更新信息
     */
    WmsExchangeItemDO updateExchangeItem(@Valid WmsExchangeItemSaveReqVO updateReqVO);

    /**
     * 删除良次换货详情
     *
     * @param id 编号
     */
    void deleteExchangeItem(Long id);

    /**
     * 获得良次换货详情
     *
     * @param id 编号
     * @return 良次换货详情
     */
    WmsExchangeItemDO getExchangeItem(Long id);

    /**
     * 获得良次换货详情分页
     *
     * @param pageReqVO 分页查询
     * @return 良次换货详情分页
     */
    PageResult<WmsExchangeItemDO> getExchangeItemPage(WmsExchangeItemPageReqVO pageReqVO);

    /**
     * 按 ID 集合查询 WmsExchangeItemDO
     */
    List<WmsExchangeItemDO> selectByIds(List<Long> idList);

    /**
     * 根据换货单ID查询换货详情
     */
    List<WmsExchangeItemDO> selectByExchangeId(Long id);

    /**
     * 装配仓位
     **/
    void assembleBins(List<WmsExchangeItemRespVO> itemList);

    /**
     * 装配产品
     **/
    void assembleProduct(List<WmsExchangeItemRespVO> itemList);
}
