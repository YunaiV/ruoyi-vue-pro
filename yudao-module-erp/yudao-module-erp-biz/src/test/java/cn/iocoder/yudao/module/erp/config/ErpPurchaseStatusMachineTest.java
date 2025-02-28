package cn.iocoder.yudao.module.erp.config;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.erp.config.impl.ActionAuditImpl;
import cn.iocoder.yudao.module.erp.config.impl.ActionOffImpl;
import cn.iocoder.yudao.module.erp.config.impl.BaseActionImpl;
import cn.iocoder.yudao.module.erp.config.impl.BaseConditionImpl;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestMapper;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.ErpOffStatus;
import com.alibaba.cola.statemachine.StateMachine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@ComponentScan(value = "cn.iocoder.yudao.module.erp")
@ComponentScan(value = "cn.iocoder.yudao.module.erp.config.impl")
@ContextConfiguration(classes = {ErpPurchaseStatusMachine.class})
@Import({BaseActionImpl.class, BaseConditionImpl.class, ActionAuditImpl.class, ActionOffImpl.class})
class ErpPurchaseStatusMachineTest extends BaseDbUnitTest {

    @Resource
    ErpPurchaseRequestMapper mapper;
    @Resource
    @Qualifier("purchaseRequestOffStateMachine")
    StateMachine<ErpOffStatus, ErpEventEnum, ErpPurchaseRequestDO> offStatusMachine;
    @Resource
    @Qualifier("purchaseRequestStateMachine")
    StateMachine<ErpAuditStatus, ErpEventEnum, ErpPurchaseRequestDO> machine;

    @BeforeAll
    static void beforeAll() {

    }


    @Test
    void getPurchaseRequestStateMachine() {
//        StateMachine<ErpAuditStatus, ErpEventEnum, ErpPurchaseRequestDO> machine = StateMachineFactory.get("purchaseRequestStateMachine");
        System.out.println(machine.generatePlantUML());
        //
        ErpPurchaseRequestDO requestDO = mapper.selectById(32603L).setStatus(ErpAuditStatus.DRAFT.getCode());
        System.out.println("requestDO状态 = " + ErpAuditStatus.fromCode(requestDO.getStatus()).getDesc());
        mapper.update(requestDO, new LambdaQueryWrapperX<ErpPurchaseRequestDO>().eq(ErpPurchaseRequestDO::getId, requestDO.getId()));//设置初始化状态
        //
        machine.fireEvent(ErpAuditStatus.fromCode(requestDO.getStatus()), ErpEventEnum.INIT, requestDO);//初始化
        machine.fireEvent(ErpAuditStatus.fromCode(requestDO.getStatus()), ErpEventEnum.SUBMIT_FOR_REVIEW, requestDO);//提交审核
        machine.fireEvent(ErpAuditStatus.fromCode(requestDO.getStatus()), ErpEventEnum.AGREE, requestDO);//同意
        machine.fireEvent(ErpAuditStatus.fromCode(requestDO.getStatus()), ErpEventEnum.WITHDRAW_REVIEW, requestDO);//反审核
    }

    @Test
    void createPurchaseRequest() {
//        StateMachine<ErpAuditStatus, ErpEventEnum, ErpPurchaseRequestDO> machine = StateMachineFactory.get("purchaseRequestOffStateMachine");
        ErpPurchaseRequestDO requestDO = mapper.selectById(32603L);
        System.out.println(offStatusMachine.generatePlantUML());
    }
}