package cn.iocoder.yudao.module.tms.api.transfer;

import cn.iocoder.yudao.module.tms.api.transfer.dto.TmsInboundReqDTO;
import cn.iocoder.yudao.module.tms.api.transfer.dto.TmsOutboundReqDTO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

/**
 * 调拨单API
 */
@Validated
public interface TmsTransferApi {

    /**
     * 1.0 出库单审核后回调
     * <p>
     * 回填数据，创建入库单
     *
     * @param reqDTO 出库单信息
     */
    void afterOutboundAudit(@Valid TmsOutboundReqDTO reqDTO);

    /**
     * 2.0 入库单审核后回调
     * <p>
     * 回填入库时间，入库数量,入库ID，入库编码
     *
     * @param reqDTO 入库单信息
     */
    void afterInboundAudit(@Valid TmsInboundReqDTO reqDTO);
}
