package cn.iocoder.yudao.module.erp.config;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.erp.config.purchase.ErpPurchaseStatusMachine;
import cn.iocoder.yudao.module.erp.config.purchase.impl.BaseConditionImpl;
import cn.iocoder.yudao.module.erp.config.purchase.impl.action.ActionAuditImpl;
import cn.iocoder.yudao.module.erp.config.purchase.impl.action.ActionOffImpl;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestMapper;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.ErpStateMachines;
import cn.iocoder.yudao.module.erp.enums.status.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.status.ErpOffStatus;
import cn.iocoder.yudao.module.erp.enums.status.ErpOrderStatus;
import com.alibaba.cola.statemachine.StateMachine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@ComponentScan(value = "cn.iocoder.yudao.module.erp")
@ContextConfiguration(classes = {ErpPurchaseStatusMachine.class})
@Import({BaseConditionImpl.class, ActionAuditImpl.class, ActionOffImpl.class})
class ErpPurchaseStatusMachineTest extends BaseDbUnitTest {

    @Resource
    ErpPurchaseRequestMapper mapper;
    @Resource(name = ErpStateMachines.PURCHASE_REQUEST_STATE_MACHINE_NAME)
    StateMachine<ErpAuditStatus, ErpEventEnum, ErpPurchaseRequestDO> auditMachine;
    @Resource(name = ErpStateMachines.PURCHASE_REQUEST_OFF_STATE_MACHINE_NAME)
    StateMachine<ErpOffStatus, ErpEventEnum, ErpPurchaseRequestDO> offMachine;

    @Resource(name = ErpStateMachines.PURCHASE_ORDER_STATE_MACHINE_NAME)
    StateMachine<ErpOrderStatus, ErpEventEnum, ErpPurchaseRequestDO> orderMachine;

    @BeforeAll
    static void beforeAll() {

    }


    //    @Rollback(value = false)
    @Test
    void getPurchaseRequestStateMachine() {
//        StateMachine<ErpAuditStatus, ErpEventEnum, ErpPurchaseRequestDO> machine = StateMachineFactory.get("purchaseRequestStateMachine");
        System.out.println(auditMachine.generatePlantUML());
        //
        ErpPurchaseRequestDO requestDO = mapper.selectById(32603L);
        System.out.println("requestDO状态 = " + ErpAuditStatus.fromCode(requestDO.getStatus()).getDesc());
//        mapper.update(requestDO, new LambdaQueryWrapperX<ErpPurchaseRequestDO>().eq(ErpPurchaseRequestDO::getId, requestDO.getId()));
        //

        auditMachine.fireEvent(ErpAuditStatus.DRAFT, ErpEventEnum.INIT, requestDO);//初始化
        auditMachine.fireEvent(ErpAuditStatus.fromCode(mapper.selectById(32603L).getStatus()), ErpEventEnum.SUBMIT_FOR_REVIEW, requestDO);//提交审核
        auditMachine.fireEvent(ErpAuditStatus.fromCode(mapper.selectById(32603L).getStatus()), ErpEventEnum.AGREE, requestDO);//审核同意
        auditMachine.fireEvent(ErpAuditStatus.fromCode(mapper.selectById(32603L).getStatus()), ErpEventEnum.WITHDRAW_REVIEW, requestDO);//反审核
        auditMachine.fireEvent(ErpAuditStatus.fromCode(mapper.selectById(32603L).getStatus()), ErpEventEnum.SUBMIT_FOR_REVIEW, requestDO);//提交审核
        auditMachine.fireEvent(ErpAuditStatus.fromCode(mapper.selectById(32603L).getStatus()), ErpEventEnum.AGREE, requestDO);//审核同意
    }

    @Test
    void test1() {
        System.out.println(offMachine.generatePlantUML());
        //开关测试
//        StateMachine<ErpAuditStatus, ErpEventEnum, ErpPurchaseRequestDO> machine = StateMachineFactory.get("purchaseRequestOffStateMachine");
        ErpPurchaseRequestDO requestDO = mapper.selectById(32603L);
        //状态
        System.out.println("requestDO开关状态 = " + ErpOffStatus.fromCode(requestDO.getOffStatus()).getDesc());
        offMachine.fireEvent(ErpOffStatus.OPEN, ErpEventEnum.OFF_INIT, requestDO);//开关初始化
        offMachine.fireEvent(ErpOffStatus.fromCode(mapper.selectById(32603L).getOffStatus()), ErpEventEnum.MANUAL_CLOSE, requestDO);//手动关闭
        //开启
        offMachine.fireEvent(ErpOffStatus.fromCode(mapper.selectById(32603L).getOffStatus()), ErpEventEnum.ACTIVATE, requestDO);//开启
        offMachine.fireEvent(ErpOffStatus.fromCode(mapper.selectById(32603L).getOffStatus()), ErpEventEnum.AUTO_CLOSE, requestDO);//自动关闭
    }

    //订购测试
    @Test
    void test2() {
        System.out.println(orderMachine.generatePlantUML());
        //订购
        ErpPurchaseRequestDO requestDO = mapper.selectById(32603L);
        //状态
//        System.out.println("requestDO订购状态 = " + ErpOrderStatus.fromCode(requestDO.getOrderStatus()).getDesc());
        ErpPurchaseOrderDO orderDO = new ErpPurchaseOrderDO();
//        orderMachine.fireEvent(ErpOrderStatus.fromCode(mapper.selectById(32603L).getOrderStatus()), ErpEventEnum.ORDER_INIT, requestDO);//订购
    }

    //子表入库
    @Test
    void test3() {

    }
    //子表开关
}