package cn.iocoder.yudao.module.pay.service.transfer;

import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferCreateReqDTO;
import cn.iocoder.yudao.module.pay.controller.admin.transfer.vo.PayTransferSubmitReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.transfer.vo.PayTransferSubmitRespVO;

import javax.validation.Valid;

/**
 * 转账 Service 接口
 *
 * @author jason
 */
public interface PayTransferService {

    /**
     * 提交转账单
     *
     * 此时，会发起支付渠道的调用
     *
     * @param reqVO 请求
     * @param userIp 用户 ip
     * @return 渠道的返回结果
     */
    PayTransferSubmitRespVO submitTransfer(@Valid PayTransferSubmitReqVO reqVO, String userIp);

    /**
     * 创建转账单
     *
     * @param reqDTO 创建请求
     * @return 转账单编号
     */
    Long createTransfer(@Valid PayTransferCreateReqDTO reqDTO);

}
