package cn.iocoder.yudao.module.erp.config;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.erp.config.purchase.ErpPurchaseStatusMachine;
import cn.iocoder.yudao.module.erp.config.purchase.impl.BaseActionImpl;
import cn.iocoder.yudao.module.erp.config.purchase.impl.BaseConditionImpl;
import cn.iocoder.yudao.module.erp.config.purchase.impl.action.ActionAuditImpl;
import cn.iocoder.yudao.module.erp.config.purchase.impl.action.ActionOffImpl;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestMapper;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.status.ErpOffStatus;
import com.alibaba.cola.statemachine.StateMachine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@ComponentScan(value = "cn.iocoder.yudao.module.erp")
@ContextConfiguration(classes = {ErpPurchaseStatusMachine.class})
@Import({BaseActionImpl.class, BaseConditionImpl.class, ActionAuditImpl.class, ActionOffImpl.class})
class ErpPurchaseStatusMachineTest extends BaseDbUnitTest {

    @Resource
    ErpPurchaseRequestMapper mapper;
    @Autowired
    @Qualifier("purchaseRequestOffStateMachine")
    StateMachine<ErpOffStatus, ErpEventEnum, ErpPurchaseRequestDO> offStatusMachine;
    @Autowired
    @Qualifier("purchaseRequestAuditStateMachine")
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

        machine.fireEvent(ErpAuditStatus.DRAFT, ErpEventEnum.INIT, requestDO);//初始化
        machine.fireEvent(ErpAuditStatus.fromCode(mapper.selectById(32603L).getStatus()), ErpEventEnum.SUBMIT_FOR_REVIEW, requestDO);//提交审核
        machine.fireEvent(ErpAuditStatus.fromCode(mapper.selectById(32603L).getStatus()), ErpEventEnum.AGREE, requestDO);//审核同意
        machine.fireEvent(ErpAuditStatus.fromCode(mapper.selectById(32603L).getStatus()), ErpEventEnum.WITHDRAW_REVIEW, requestDO);//反审核
        machine.fireEvent(ErpAuditStatus.fromCode(mapper.selectById(32603L).getStatus()), ErpEventEnum.SUBMIT_FOR_REVIEW, requestDO);//提交审核
        machine.fireEvent(ErpAuditStatus.fromCode(mapper.selectById(32603L).getStatus()), ErpEventEnum.AGREE, requestDO);//审核同意
    }

    @Test
    void createPurchaseRequest() {
//        StateMachine<ErpAuditStatus, ErpEventEnum, ErpPurchaseRequestDO> machine = StateMachineFactory.get("purchaseRequestOffStateMachine");
        ErpPurchaseRequestDO requestDO = mapper.selectById(32603L);
        System.out.println(offStatusMachine.generatePlantUML());
    }
}