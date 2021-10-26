package cn.iocoder.yudao.coreservice.modules.pay.service.notify.impl;

import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.notify.PayNotifyTaskDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.mysql.notify.PayNotifyTaskCoreMapper;
import cn.iocoder.yudao.coreservice.modules.pay.enums.notify.PayNotifyStatusEnum;
import cn.iocoder.yudao.coreservice.modules.pay.enums.notify.PayNotifyTypeEnum;
import cn.iocoder.yudao.coreservice.modules.pay.service.notify.PayNotifyCoreService;
import cn.iocoder.yudao.coreservice.modules.pay.service.notify.dto.PayNotifyTaskCreateReqDTO;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.PayOrderCoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Date;
import java.util.Objects;

/**
 * 支付通知 Core Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Valid
@Slf4j
public class PayNotifyCoreServiceImpl implements PayNotifyCoreService {

    @Resource
    @Lazy // 循环依赖，避免报错
    private PayOrderCoreService payOrderCoreService;

    @Resource
    private PayNotifyTaskCoreMapper payNotifyTaskCoreMapper;

    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor; // TODO 芋艿：未来提供独立的线程池

    @Override
    public void createPayNotifyTask(PayNotifyTaskCreateReqDTO reqDTO) {
        PayNotifyTaskDO task = new PayNotifyTaskDO();
        task.setType(reqDTO.getType()).setDataId(reqDTO.getDataId());
        task.setStatus(PayNotifyStatusEnum.WAITING.getStatus()).setNextNotifyTime(new Date())
                .setNotifyTimes(0).setMaxNotifyTimes(PayNotifyTaskDO.NOTIFY_FREQUENCY.length + 1);
        // 补充 merchantId + appId + notifyUrl 字段
        if (Objects.equals(task.getType(), PayNotifyTypeEnum.ORDER.getType())) {
            PayOrderDO order = payOrderCoreService.getPayOrder(task.getDataId()); // 不进行非空判断，有问题直接异常
            task.setMerchantId(order.getMerchantId()).setAppId(order.getAppId()).
                    setMerchantOrderId(order.getMerchantOrderId()).setNotifyUrl(order.getNotifyUrl());
        } else if (Objects.equals(task.getType(), PayNotifyTypeEnum.REFUND.getType())) {
            // TODO 芋艿，需要实现下哈
            throw new UnsupportedOperationException("需要实现");
        }

        // 执行插入
        payNotifyTaskCoreMapper.insert(task);
    }

    @Override
    public void executeNotify() {

    }

}
