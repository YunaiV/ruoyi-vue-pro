package cn.iocoder.yudao.module.pay.service.demo;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.pay.core.enums.transfer.PayTransferTypeEnum;
import cn.iocoder.yudao.module.pay.controller.admin.demo.vo.transfer.PayDemoTransferCreateReqVO;
import cn.iocoder.yudao.module.pay.convert.transfer.PayTransferConvert;
import cn.iocoder.yudao.module.pay.dal.dataobject.demo.PayDemoTransferDO;
import cn.iocoder.yudao.module.pay.dal.mysql.demo.PayDemoTransferMapper;
import cn.iocoder.yudao.module.pay.service.transfer.PayTransferService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.pay.core.enums.transfer.PayTransferTypeEnum.*;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.PAY_TRANSFER_ALIPAY_ACCOUNT_NAME_IS_EMPTY;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.PAY_TRANSFER_ALIPAY_LOGIN_ID_IS_EMPTY;
import static cn.iocoder.yudao.module.pay.enums.transfer.PayTransferStatusEnum.WAITING;

/**
 * 示例转账业务 Service 实现类
 *
 * @author jason
 */
@Service
@Validated
public class PayDemoTransferServiceImpl implements PayDemoTransferService {

    /**
     * 接入的实力应用编号

     * 从 [支付管理 -> 应用信息] 里添加
     */
    private static final Long TRANSFER_APP_ID = 8L;
    @Resource
    private PayDemoTransferMapper demoTransferMapper;
    @Resource
    private PayTransferService transferService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDemoTransfer(Long userId, @Valid PayDemoTransferCreateReqVO vo) {
        // 1 校验收款账号
        validatePayeeInfo(vo.getType(), vo.getPayeeInfo());

        // 2 保存示例转账业务表
        PayDemoTransferDO demoTransfer = new PayDemoTransferDO().setUserId(userId).setType(vo.getType())
                .setPrice(vo.getPrice()).setPayeeInfo(vo.getPayeeInfo())
                .setTransferStatus(WAITING.getStatus());
        demoTransferMapper.insert(demoTransfer);

        // 3.1 创建转账单
        Long transferId = transferService.createTransfer(PayTransferConvert.INSTANCE.convert(vo)
                .setAppId(TRANSFER_APP_ID).setTitle("示例转账")
                .setMerchantOrderId(String.valueOf(demoTransfer.getId())));
        // 3.2 更新转账单编号
        demoTransferMapper.updateById(new PayDemoTransferDO().setId(demoTransfer.getId())
                .setPayTransferId(transferId));
        return demoTransfer.getId();
    }

    // TODO @jason：可以参考 AppBrokerageWithdrawCreateReqVO 搞下字段哈，进行校验
    // @jason payeeinfo 字段确定改一下
    private void validatePayeeInfo(Integer transferType, Map<String, String> payeeInfo) {
        PayTransferTypeEnum transferTypeEnum = typeOf(transferType);
        switch (transferTypeEnum) {
            case ALIPAY_BALANCE: {
                if (StrUtil.isEmpty(MapUtil.getStr(payeeInfo, ALIPAY_LOGON_ID))) {
                    throw exception(PAY_TRANSFER_ALIPAY_LOGIN_ID_IS_EMPTY);
                }
                if (StrUtil.isEmpty(MapUtil.getStr(payeeInfo, ALIPAY_ACCOUNT_NAME))) {
                    throw exception(PAY_TRANSFER_ALIPAY_ACCOUNT_NAME_IS_EMPTY);
                }
                break;
            }
            case WX_BALANCE:
            case BANK_CARD:
            case WALLET_BALANCE: {
                throw new UnsupportedOperationException("待实现");
            }
        }
    }

}
