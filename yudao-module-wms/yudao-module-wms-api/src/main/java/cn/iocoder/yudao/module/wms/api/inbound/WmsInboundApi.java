package cn.iocoder.yudao.module.wms.api.inbound;

import cn.iocoder.yudao.module.wms.api.inbound.dto.WmsInboundDTO;
import cn.iocoder.yudao.module.wms.api.inbound.dto.WmsInboundItemListForTmsReqDTO;
import cn.iocoder.yudao.module.wms.api.inbound.dto.WmsInboundItemRespDTO;
import cn.iocoder.yudao.module.wms.api.inbound.dto.WmsInboundSaveReqDTO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * @author: LeeFJ
 * @date: 2025/4/28 17:03
 * @description: 入库单 API
 */
@Validated
public interface WmsInboundApi {

    /**
     * 创建入库单
     * 直接进行审批操作，使wms入库单进入【待入库状态】
     * @param createReqDTO 入库单创建请求
     * @return 入库单ID
     */
    Long createInbound(@Valid WmsInboundSaveReqDTO createReqDTO);

    /**
     * 按 ID 查询入库单
     *
     * @param id 入库单更新请求
     * @return WmsInboundDTO
     */
    WmsInboundDTO getInbound(Long id);

    /**
     * 按 上游单据类型 和 上游单据ID 查询入库单
     *
     * @param upstreamType 入库单类型
     * @param upstreamId 上游单号
     * @return WmsInboundDTO
     */
    List<WmsInboundDTO> getInboundList(Integer upstreamType, Long upstreamId);


    /**
     * 作废入库单
     * @param id 入库单ID
     * @param comment 作废原因
     * @param billType 单据类型
     **/
    void abandonInbound(Long id, String comment, Integer billType);

    /**
     * 常规批次库存列表查询
     *
     * @param reqDTO 入参
     * @return 出库单列表
     */
    List<WmsInboundItemRespDTO> getInboundItemList(@Valid WmsInboundItemListForTmsReqDTO reqDTO);
}
