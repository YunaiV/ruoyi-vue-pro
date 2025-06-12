package cn.iocoder.yudao.module.wms.api.outbound;

import cn.iocoder.yudao.module.wms.api.outbound.dto.WmsOutboundDTO;
import cn.iocoder.yudao.module.wms.api.outbound.dto.WmsOutboundImportReqDTO;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * @author: LeeFJ
 * @date: 2025/4/28 17:03
 * @description: 出库单 API
 */
@Validated
public interface WmsOutboundApi {

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
     * 生成出库单
     *
     * @param importReqVO 入参
     */
    void generateOutbound(WmsOutboundImportReqDTO importReqVO);
}
