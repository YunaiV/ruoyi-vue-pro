package cn.iocoder.yudao.module.tms.api.first.mile;

import cn.iocoder.yudao.module.tms.api.transfer.dto.TmsOutboundReqDTO;
import org.springframework.validation.annotation.Validated;

/**
 * 头程单 API 接口
 */
@Validated
public interface TmsFistMileApi {

    /**
     * 出库单审批通过-回调
     * <p>
     * 回填头程单数据
     */
    void outboundAudit(TmsOutboundReqDTO dto);
}
