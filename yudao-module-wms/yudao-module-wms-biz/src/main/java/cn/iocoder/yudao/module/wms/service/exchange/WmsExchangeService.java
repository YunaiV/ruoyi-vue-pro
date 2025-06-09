package cn.iocoder.yudao.module.wms.service.exchange;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.vo.WmsExchangePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.vo.WmsExchangeRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.vo.WmsExchangeSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.WmsExchangeDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.item.WmsExchangeItemDO;
import cn.iocoder.yudao.module.wms.enums.exchange.WmsExchangeAuditStatus;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 换货单 Service 接口
 *
 * @author 李方捷
 */
public interface WmsExchangeService {

    /**
     * 创建换货单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsExchangeDO createExchange(@Valid WmsExchangeSaveReqVO createReqVO);

    /**
     * 更新换货单
     *
     * @param updateReqVO 更新信息
     */
    WmsExchangeDO updateExchange(@Valid WmsExchangeSaveReqVO updateReqVO);

    /**
     * 删除换货单
     *
     * @param id 编号
     */
    void deleteExchange(Long id);

    /**
     * 获得换货单
     *
     * @param id 编号
     * @return 换货单
     */
    WmsExchangeDO getExchange(Long id);

    /**
     * 获得换货单分页
     *
     * @param pageReqVO 分页查询
     * @return 换货单分页
     */
    PageResult<WmsExchangeDO> getExchangePage(WmsExchangePageReqVO pageReqVO);

    /**
     * 按 ID 集合查询 WmsExchangeDO
     */
    List<WmsExchangeDO> selectByIds(List<Long> idList);

    /**
     * 更新换货审批状态
     **/
    WmsExchangeDO updateExchangeAuditStatus(Long id, Integer to);

    /**
     * 审批
     **/
    void approve(WmsExchangeAuditStatus.Event event, WmsApprovalReqVO approvalReqVO);

    /**
     * 完成换货
     **/
    void finishExchange(WmsExchangeDO exchangeDO, List<WmsExchangeItemDO> exchangeItemDOList);

    /**
     * 组装仓库
     **/
    void assembleWarehouse(List<WmsExchangeRespVO> list);
}
