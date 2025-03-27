package cn.iocoder.yudao.module.erp.config.purchase.returned;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.erp.enums.status.ErpAuditStatus;
import cn.iocoder.yudao.module.srm.config.purchase.request.ErpPurchaseRequestStatusMachine;
import cn.iocoder.yudao.module.srm.config.purchase.request.impl.BaseConditionImpl;
import cn.iocoder.yudao.module.srm.config.purchase.request.impl.action.ActionAuditImpl;
import cn.iocoder.yudao.module.srm.config.purchase.request.impl.action.ActionOffImpl;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.ErpPurchaseRequestMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.ErpOffStatus;
import cn.iocoder.yudao.module.srm.enums.status.ErpOrderStatus;
import com.alibaba.cola.statemachine.StateMachine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.PURCHASE_REQUEST_OFF_STATE_MACHINE_NAME;
import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.PURCHASE_REQUEST_ORDER_STATE_MACHINE_NAME;

@Slf4j
@ComponentScan(value = "cn.iocoder.yudao.module.erp")
@ContextConfiguration(classes = {ErpPurchaseRequestStatusMachine.class})
@Import({BaseConditionImpl.class, ActionAuditImpl.class, ActionOffImpl.class})
class ErpPurchaseReturnedStatusMachineTest extends BaseDbUnitTest {

    @Resource
    ErpPurchaseRequestMapper mapper;
    //    @Resource(name = ErpStateMachines.PURCHASE_REQUEST_AUDIT_STATE_MACHINE_NAME)
    StateMachine<ErpAuditStatus, SrmEventEnum, ErpPurchaseRequestDO> auditMachine;
    @Resource(name = PURCHASE_REQUEST_OFF_STATE_MACHINE_NAME)
    StateMachine<ErpOffStatus, SrmEventEnum, ErpPurchaseRequestDO> offMachine;

    @Resource(name = PURCHASE_REQUEST_ORDER_STATE_MACHINE_NAME)
    StateMachine<ErpOrderStatus, SrmEventEnum, ErpPurchaseRequestDO> orderMachine;

    @BeforeAll
    static void beforeAll() {

    }


    //    @Rollback(value = false)
    @Test
    void getPurchaseRequestStateMachine() {
//        StateMachine<ErpAuditStatus, SrmEventEnum, ErpPurchaseRequestDO> machine = StateMachineFactory.get("purchaseRequestStateMachine");
        System.out.println(auditMachine.generatePlantUML());
        //
        ErpPurchaseRequestDO requestDO = mapper.selectById(32603L);
        System.out.println("requestDO状态 = " + ErpAuditStatus.fromCode(requestDO.getStatus()).getDesc());
//        mapper.update(requestDO, new LambdaQueryWrapperX<ErpPurchaseRequestDO>().eq(ErpPurchaseRequestDO::getId, requestDO.getId()));
        //

        auditMachine.fireEvent(ErpAuditStatus.DRAFT, SrmEventEnum.AUDIT_INIT, requestDO);//初始化
        auditMachine.fireEvent(ErpAuditStatus.fromCode(mapper.selectById(32603L).getStatus()), SrmEventEnum.SUBMIT_FOR_REVIEW, requestDO);//提交审核
        auditMachine.fireEvent(ErpAuditStatus.fromCode(mapper.selectById(32603L).getStatus()), SrmEventEnum.AGREE, requestDO);//审核同意
        auditMachine.fireEvent(ErpAuditStatus.fromCode(mapper.selectById(32603L).getStatus()), SrmEventEnum.WITHDRAW_REVIEW, requestDO);//反审核
        auditMachine.fireEvent(ErpAuditStatus.fromCode(mapper.selectById(32603L).getStatus()), SrmEventEnum.SUBMIT_FOR_REVIEW, requestDO);//提交审核
        auditMachine.fireEvent(ErpAuditStatus.fromCode(mapper.selectById(32603L).getStatus()), SrmEventEnum.AGREE, requestDO);//审核同意
    }

    @Test
    void test1() {
        System.out.println(offMachine.generatePlantUML());
        //开关测试
//        StateMachine<ErpAuditStatus, SrmEventEnum, ErpPurchaseRequestDO> machine = StateMachineFactory.get("purchaseRequestOffStateMachine");
        ErpPurchaseRequestDO requestDO = mapper.selectById(32603L);
        //状态
        System.out.println("requestDO开关状态 = " + ErpOffStatus.fromCode(requestDO.getOffStatus()).getDesc());
        offMachine.fireEvent(ErpOffStatus.OPEN, SrmEventEnum.OFF_INIT, requestDO);//开关初始化
        offMachine.fireEvent(ErpOffStatus.fromCode(mapper.selectById(32603L).getOffStatus()), SrmEventEnum.MANUAL_CLOSE, requestDO);//手动关闭
        //开启
        offMachine.fireEvent(ErpOffStatus.fromCode(mapper.selectById(32603L).getOffStatus()), SrmEventEnum.ACTIVATE, requestDO);//开启
        offMachine.fireEvent(ErpOffStatus.fromCode(mapper.selectById(32603L).getOffStatus()), SrmEventEnum.AUTO_CLOSE, requestDO);//自动关闭
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
//        orderMachine.fireEvent(ErpOrderStatus.fromCode(mapper.selectById(32603L).getOrderStatus()), SrmEventEnum.ORDER_INIT, requestDO);//订购
    }

    //子表入库
    @Test
    void test3() {

    }
    //子表开关
}