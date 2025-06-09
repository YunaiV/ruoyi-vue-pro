package cn.iocoder.yudao.module.wms.api.outbound;

import cn.iocoder.yudao.module.wms.api.outbound.dto.WmsOutboundDTO;
import cn.iocoder.yudao.module.wms.api.outbound.dto.WmsOutboundImportReqDTO;
import cn.iocoder.yudao.module.wms.api.outbound.dto.WmsOutboundSaveReqDTO;

import java.util.List;

/**
 * @author: LeeFJ
 * @date: 2025/4/28 17:03
 * @description: 出库单 API
 */
public interface WmsOutboundApi {

    /**
     * 创建出库单
     *
     * @param createReqDTO 出库单创建请求
     * @return 出库单ID
     */
    Long createOutbound(WmsOutboundSaveReqDTO createReqDTO);

    /**
     * 按 ID 查询出库单
     *
     * @param id 出库单更新请求
     * @return WmsInboundDTO
     */
    WmsOutboundDTO getOutbound(Long id);

    /**
     * 按 上游单据类型 和 上游单据ID 查询出库单
     *
     * @param upstreamType 出库单类型
     * @param upstreamId 上游单号
     * @return WmsInboundDTO
     */
    List<WmsOutboundDTO> getOutboundList(Integer upstreamType, Long upstreamId);

    /**
     * 出库单作废
     * @param id 出库单ID
     * @param comment 作废原因
     **/
    void abandonOutbound(Long id,String comment);

    /**
     * 根据入库单生成出库单
     * @param importReqVO 入参
     * @return 出参
     */
    WmsOutboundDTO generateOutbound(WmsOutboundImportReqDTO importReqVO);
}
