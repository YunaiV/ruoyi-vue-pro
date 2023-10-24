package cn.iocoder.yudao.module.pay.service.demo;

import cn.iocoder.yudao.module.pay.controller.admin.demo.vo.transfer.PayDemoTransferCreateReqVO;
import cn.iocoder.yudao.module.pay.convert.demo.PayDemoTransferConvert;
import cn.iocoder.yudao.module.pay.dal.dataobject.demo.PayDemoTransferDO;
import cn.iocoder.yudao.module.pay.dal.mysql.demo.PayDemoTransferMapper;
import cn.iocoder.yudao.module.pay.service.transfer.PayTransferService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.Validator;

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
    @Resource
    private Validator validator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDemoTransfer(Long userId, @Valid PayDemoTransferCreateReqVO vo) {
        // 1 校验参数
        vo.validate(validator);
        // 2 保存示例转账业务表
        PayDemoTransferDO demoTransfer = PayDemoTransferConvert.INSTANCE.convert(vo)
                .setUserId(userId).setTransferStatus(WAITING.getStatus());
        demoTransferMapper.insert(demoTransfer);
        return demoTransfer.getId();
    }
}
