package cn.iocoder.yudao.module.erp.config.purchase.returned;

import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.erp.enums.status.ErpAuditStatus;
import cn.iocoder.yudao.module.srm.config.purchase.request.SrmPurchaseRequestStatusMachine;
import cn.iocoder.yudao.module.srm.config.purchase.request.impl.BaseConditionImpl;
import cn.iocoder.yudao.module.srm.config.purchase.request.impl.action.AuditActionImpl;
import cn.iocoder.yudao.module.srm.config.purchase.request.impl.action.OffActionImpl;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.request.req.SrmPurchaseRequestPageReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseRequestMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmOffStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmOrderStatus;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.PURCHASE_REQUEST_OFF_STATE_MACHINE_NAME;
import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.PURCHASE_REQUEST_ORDER_STATE_MACHINE_NAME;

@Slf4j
@Disabled
@ComponentScan(value = "cn.iocoder.yudao.module.erp")
@ContextConfiguration(classes = {SrmPurchaseRequestStatusMachine.class})
@Import({BaseConditionImpl.class, AuditActionImpl.class, OffActionImpl.class})
class SrmPurchaseReturnedStatusMachineTest extends BaseDbUnitTest {

    @Resource
    SrmPurchaseRequestMapper mapper;
    //    @Resource(name = ErpStateMachines.PURCHASE_REQUEST_AUDIT_STATE_MACHINE_NAME)
    StateMachine<ErpAuditStatus, SrmEventEnum, SrmPurchaseRequestDO> auditMachine;
    @Resource(name = PURCHASE_REQUEST_OFF_STATE_MACHINE_NAME)
    StateMachine<SrmOffStatus, SrmEventEnum, SrmPurchaseRequestDO> offMachine;

    @Resource(name = PURCHASE_REQUEST_ORDER_STATE_MACHINE_NAME)
    StateMachine<SrmOrderStatus, SrmEventEnum, SrmPurchaseRequestDO> orderMachine;

    @BeforeAll
    static void beforeAll() {

    }


    //    @Rollback(value = false)
    @Test
    void getPurchaseRequestStateMachine() {
//        StateMachine<ErpAuditStatus, SrmEventEnum, SrmPurchaseRequestDO> machine = StateMachineFactory.get("purchaseRequestStateMachine");
        System.out.println(auditMachine.generatePlantUML());
        //
        mapper.selectPage(new SrmPurchaseRequestPageReqVO()).getList().stream().findFirst().ifPresent(requestDO -> {
            System.out.println("requestDO状态 = " + ErpAuditStatus.fromCode(requestDO.getAuditStatus()).getDesc());
//        mapper.update(requestDO, new LambdaQueryWrapperX<SrmPurchaseRequestDO>().eq(SrmPurchaseRequestDO::getId, requestDO.getId()));
            //

            auditMachine.fireEvent(ErpAuditStatus.DRAFT, SrmEventEnum.AUDIT_INIT, requestDO);//初始化
            auditMachine.fireEvent(ErpAuditStatus.fromCode(mapper.selectById(32603L).getAuditStatus()), SrmEventEnum.SUBMIT_FOR_REVIEW, requestDO);//提交审核
            auditMachine.fireEvent(ErpAuditStatus.fromCode(mapper.selectById(32603L).getAuditStatus()), SrmEventEnum.AGREE, requestDO);//审核同意
            auditMachine.fireEvent(ErpAuditStatus.fromCode(mapper.selectById(32603L).getAuditStatus()), SrmEventEnum.WITHDRAW_REVIEW, requestDO);//反审核
            auditMachine.fireEvent(ErpAuditStatus.fromCode(mapper.selectById(32603L).getAuditStatus()), SrmEventEnum.SUBMIT_FOR_REVIEW, requestDO);//提交审核
            auditMachine.fireEvent(ErpAuditStatus.fromCode(mapper.selectById(32603L).getAuditStatus()), SrmEventEnum.AGREE, requestDO);//审核同意
        });
    }

    @Test
    void test1() {
        System.out.println(offMachine.generatePlantUML());
        //开关测试
//        StateMachine<ErpAuditStatus, SrmEventEnum, SrmPurchaseRequestDO> machine = StateMachineFactory.get("purchaseRequestOffStateMachine");
        SrmPurchaseRequestDO requestDO = mapper.selectById(32603L);
        //状态
        System.out.println("requestDO开关状态 = " + SrmOffStatus.fromCode(requestDO.getOffStatus()).getDesc());
        offMachine.fireEvent(SrmOffStatus.OPEN, SrmEventEnum.OFF_INIT, requestDO);//开关初始化
        offMachine.fireEvent(SrmOffStatus.fromCode(mapper.selectById(32603L).getOffStatus()), SrmEventEnum.MANUAL_CLOSE, requestDO);//手动关闭
        //开启
        offMachine.fireEvent(SrmOffStatus.fromCode(mapper.selectById(32603L).getOffStatus()), SrmEventEnum.ACTIVATE, requestDO);//开启
        offMachine.fireEvent(SrmOffStatus.fromCode(mapper.selectById(32603L).getOffStatus()), SrmEventEnum.AUTO_CLOSE, requestDO);//自动关闭
    }

    //订购测试
    @Test
    void test2() {
        System.out.println(orderMachine.generatePlantUML());
        //订购
        SrmPurchaseRequestDO requestDO = mapper.selectById(32603L);
        //状态
//        System.out.println("requestDO订购状态 = " + SrmOrderStatus.fromCode(requestDO.getOrderStatus()).getDesc());
        SrmPurchaseOrderDO orderDO = new SrmPurchaseOrderDO();
//        orderMachine.fireEvent(SrmOrderStatus.fromCode(mapper.selectById(32603L).getOrderStatus()), SrmEventEnum.ORDER_INIT, requestDO);//订购
    }

    //子表入库
    @Test
    void test3() {

    }
    //子表开关
}