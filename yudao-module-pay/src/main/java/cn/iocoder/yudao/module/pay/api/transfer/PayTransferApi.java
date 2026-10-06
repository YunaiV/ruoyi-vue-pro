package cn.iocoder.yudao.module.pay.api.transfer;

import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferCreateReqDTO;
import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferCreateRespDTO;
import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferRespDTO;
import jakarta.validation.Valid;

/**
 * 转账单 API 接口
 *
 * @author jason
 */
public interface PayTransferApi {

    /**
     * 创建转账单
     *
     * 注意：不加入调用方的事务，转账单、转账结果（包括转账通知）独立提交，调用方的事务回滚时不会回滚。
     * 调用方需要使用相同的 merchantTransferId 重新发起，并通过转账通知处理最终结果；渠道明确失败时，会抛出渠道错误的业务异常
     *
     * @param reqDTO 创建请求
     * @return 创建结果
     */
    PayTransferCreateRespDTO createTransfer(@Valid PayTransferCreateReqDTO reqDTO);

    /**
     * 获得转账单
     *
     * @param id 转账单编号
     * @return 转账单
     */
    PayTransferRespDTO getTransfer(Long id);

}
