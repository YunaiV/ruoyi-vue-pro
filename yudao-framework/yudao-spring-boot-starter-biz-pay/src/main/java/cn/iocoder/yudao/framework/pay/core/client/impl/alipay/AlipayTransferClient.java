package cn.iocoder.yudao.framework.pay.core.client.impl.alipay;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.transfer.PayTransferRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.transfer.PayTransferUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.enums.channel.PayChannelEnum;
import cn.iocoder.yudao.framework.pay.core.enums.transfer.PayTransferTypeEnum;
import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayFundTransUniTransferModel;
import com.alipay.api.domain.Participant;
import com.alipay.api.request.AlipayFundTransUniTransferRequest;
import com.alipay.api.response.AlipayFundTransUniTransferResponse;
import lombok.extern.slf4j.Slf4j;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0;

/**
 * 支付宝转账的 PayClient 实现类
 *
 * @author jason
 */
@Slf4j
public class AlipayTransferClient extends AbstractAlipayPayClient {
    public AlipayTransferClient(Long channelId, AlipayPayClientConfig config) {
        super(channelId, PayChannelEnum.ALIPAY_TRANSFER.getCode(), config);
    }
    @Override
    protected PayOrderRespDTO doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO) {
        throw new UnsupportedOperationException("支付宝转账不支持统一下单请求");
    }
    @Override
    protected PayRefundRespDTO doUnifiedRefund(PayRefundUnifiedReqDTO reqDTO) {
        throw new UnsupportedOperationException("支付宝转账不支持统一退款请求");
    }
    @Override
    protected PayTransferRespDTO doUnifiedTransfer(PayTransferUnifiedReqDTO reqDTO) throws AlipayApiException {
        // 1.1 构建 AlipayFundTransUniTransferModel
        AlipayFundTransUniTransferModel model = new AlipayFundTransUniTransferModel();
        // ① 通用的参数
        model.setTransAmount(formatAmount(reqDTO.getPrice())); // 转账金额
        model.setOrderTitle(reqDTO.getTitle());               // 转账业务的标题，用于在支付宝用户的账单里显示。
        model.setOutBizNo(reqDTO.getOutTransferNo());
        model.setProductCode("TRANS_ACCOUNT_NO_PWD");    // 销售产品码。单笔无密转账固定为 TRANS_ACCOUNT_NO_PWD
        model.setBizScene("DIRECT_TRANSFER");           // 业务场景 单笔无密转账固定为 DIRECT_TRANSFER。
        model.setBusinessParams(JsonUtils.toJsonString(reqDTO.getChannelExtras()));
        PayTransferTypeEnum transferType = PayTransferTypeEnum.ofType(reqDTO.getType());
        switch(transferType){
            case WX_BALANCE :
            case WALLET_BALANCE : {
                log.error("[doUnifiedTransfer],支付宝转账不支持的转账类型{}", transferType);
                throw new UnsupportedOperationException(String.format("支付宝转账不支持转账类型: %s",transferType.getName()));
            }
            case ALIPAY_BALANCE : {
                // ② 个性化的参数
                Participant payeeInfo = new Participant();
                payeeInfo.setIdentityType("ALIPAY_LOGON_ID");
                String logonId = MapUtil.getStr(reqDTO.getPayeeInfo(), "ALIPAY_LOGON_ID");
                if (StrUtil.isEmpty(logonId)) {
                    throw exception0(BAD_REQUEST.getCode(), "支付包登录 ID 不能为空");
                }
                String accountName = MapUtil.getStr(reqDTO.getPayeeInfo(), "ALIPAY_ACCOUNT_NAME");
                if (StrUtil.isEmpty(accountName)) {
                    throw exception0(BAD_REQUEST.getCode(), "支付包账户名称不能为空");
                }
                payeeInfo.setIdentity(logonId); // 支付宝登录号
                payeeInfo.setName(accountName); // 支付宝账号姓名
                model.setPayeeInfo(payeeInfo);
                // 1.2 构建 AlipayFundTransUniTransferRequest
                AlipayFundTransUniTransferRequest request = new AlipayFundTransUniTransferRequest();
                request.setBizModel(model);
                // 执行请求
                AlipayFundTransUniTransferResponse response = client.certificateExecute(request);
                // 处理结果
                if (!response.isSuccess()) {
                    // 当出现 SYSTEM_ERROR, 转账可能成功也可能失败。 返回 WAIT 状态. 后续 job 会轮询
                    if (ObjectUtils.equalsAny(response.getSubCode(), "SYSTEM_ERROR", "ACQ.SYSTEM_ERROR")) {
                        return PayTransferRespDTO.waitingOf(null, reqDTO.getOutTransferNo(), response);
                    }
                    return PayTransferRespDTO.closedOf(response.getSubCode(), response.getSubMsg(),
                            reqDTO.getOutTransferNo(), response);
                }
                return  PayTransferRespDTO.successOf(response.getOrderId(), parseTime(response.getTransDate()),
                        response.getOutBizNo(), response);
            }
            case BANK_CARD : {
                Participant payeeInfo = new Participant();
                payeeInfo.setIdentityType("BANKCARD_ACCOUNT");
                throw new UnsupportedOperationException("待实现");
            }
            default: {
                throw new IllegalStateException("不正确的转账类型: " + transferType);
            }
        }
    }
}
