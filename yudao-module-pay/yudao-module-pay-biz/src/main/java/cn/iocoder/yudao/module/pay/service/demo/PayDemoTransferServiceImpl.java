package cn.iocoder.yudao.module.pay.service.demo;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pay.api.transfer.PayTransferApi;
import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferCreateReqDTO;
import cn.iocoder.yudao.module.pay.controller.admin.demo.vo.transfer.PayDemoTransferCreateReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.demo.PayDemoTransferDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.transfer.PayTransferDO;
import cn.iocoder.yudao.module.pay.dal.mysql.demo.PayDemoTransferMapper;
import cn.iocoder.yudao.module.pay.enums.transfer.PayTransferStatusEnum;
import cn.iocoder.yudao.module.pay.service.transfer.PayTransferService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.*;

/**
 * 示例转账业务 Service 实现类
 *
 * @author jason
 */
@Service
@Validated
public class PayDemoTransferServiceImpl implements PayDemoTransferService {

    /**
     * 接入的支付应用标识
     *
     * 从 [支付管理 -> 应用信息] 里添加
     */
    private static final String PAY_APP_KEY = "demo";

    @Resource
    private PayDemoTransferMapper demoTransferMapper;

    @Resource
    private PayTransferService payTransferService;

    @Resource
    private PayTransferApi payTransferApi;

    @Override
    public Long createDemoTransfer(@Valid PayDemoTransferCreateReqVO reqVO) {
        // 1. 保存示例转账业务表
        PayDemoTransferDO demoTransfer = BeanUtils.toBean(reqVO, PayDemoTransferDO.class)
                .setTransferStatus(PayTransferStatusEnum.WAITING.getStatus());
        demoTransferMapper.insert(demoTransfer);

        // 2.1 创建支付单
        Long payTransferId = payTransferApi.createTransfer(new PayTransferCreateReqDTO()
                .setChannelCode(reqVO.getChannelCode())
                .setAppKey(PAY_APP_KEY).setUserIp(getClientIP()) // 支付应用
                .setMerchantTransferId(String.valueOf(demoTransfer.getId())) // 业务的订单编号
                .setSubject(reqVO.getSubject()).setPrice(demoTransfer.getPrice()) // 价格信息
                .setUserAccount(reqVO.getUserAccount()).setUserName(reqVO.getUserName())); // 收款信息
        // 2.2 更新转账单到 demo 示例转账业务表
        demoTransferMapper.updateById(new PayDemoTransferDO().setId(demoTransfer.getId())
               .setPayTransferId(payTransferId));
        return demoTransfer.getId();
    }

    @Override
    public PageResult<PayDemoTransferDO> getDemoTransferPage(PageParam pageVO) {
        return demoTransferMapper.selectPage(pageVO);
    }

    @Override
    public void updateDemoTransferStatus(Long id, Long payTransferId) {
        PayTransferDO payTransfer = validateDemoTransferStatusCanUpdate(id, payTransferId);
        // TODO @芋艿：这块，需要在优化下；
        // 更新示例订单状态
        if (payTransfer != null) {
            demoTransferMapper.updateById(new PayDemoTransferDO().setId(id)
                    .setPayTransferId(payTransferId)
                    .setTransferStatus(payTransfer.getStatus())
                    .setTransferTime(payTransfer.getSuccessTime()));
        }
    }

    private PayTransferDO validateDemoTransferStatusCanUpdate(Long id, Long payTransferId) {
        PayDemoTransferDO demoTransfer = demoTransferMapper.selectById(id);
        if (demoTransfer == null) {
            throw exception(DEMO_TRANSFER_NOT_FOUND);
        }
        // TODO @芋艿：这里也要更新下；
        // 无需更新返回 null
        if (PayTransferStatusEnum.isSuccess(demoTransfer.getTransferStatus())
                || PayTransferStatusEnum.isClosed(demoTransfer.getTransferStatus())) {
            return null;
        }
        PayTransferDO transfer = payTransferService.getTransfer(payTransferId);
        if (transfer == null) {
            throw exception(PAY_TRANSFER_NOT_FOUND);
        }
        if (!Objects.equals(demoTransfer.getPrice(), transfer.getPrice())) {
            throw exception(DEMO_TRANSFER_FAIL_PRICE_NOT_MATCH);
        }
        if (ObjectUtil.notEqual(transfer.getMerchantTransferId(), id.toString())) {
            throw exception(DEMO_TRANSFER_FAIL_TRANSFER_ID_ERROR);
        }
        // TODO 校验账号
        return transfer;
    }

}
