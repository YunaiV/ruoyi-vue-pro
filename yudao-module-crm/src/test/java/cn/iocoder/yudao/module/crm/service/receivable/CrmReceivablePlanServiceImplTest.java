package cn.iocoder.yudao.module.crm.service.receivable;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.plan.CrmReceivablePlanSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.CrmReceivablePlanDO;
import cn.iocoder.yudao.module.crm.dal.mysql.receivable.CrmReceivablePlanMapper;
import cn.iocoder.yudao.module.crm.service.contract.CrmContractService;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CONTRACT_NOT_EXISTS;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

public class CrmReceivablePlanServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private CrmReceivablePlanServiceImpl receivablePlanService;
    @Mock
    private CrmReceivablePlanMapper receivablePlanMapper;
    @Mock
    private CrmContractService contractService;
    @Mock
    private CrmPermissionService permissionService;
    @Mock
    private AdminUserApi adminUserApi;

    @Test
    public void testCreateReceivablePlan_contractNotExists() {
        // 准备参数
        CrmReceivablePlanSaveReqVO reqVO = new CrmReceivablePlanSaveReqVO().setContractId(9L);
        // mock 合同不存在
        when(contractService.validateContract(9L)).thenThrow(exception(CONTRACT_NOT_EXISTS));

        // 调用，并断言异常
        assertServiceException(() -> receivablePlanService.createReceivablePlan(reqVO), CONTRACT_NOT_EXISTS);
        // 断言未写入回款计划和数据权限
        verifyNoInteractions(receivablePlanMapper, permissionService);
    }

    @Test
    public void testCreateReceivablePlan_useContractCustomerId() {
        // 准备参数：入参的客户编号应以合同为准
        CrmReceivablePlanSaveReqVO reqVO = new CrmReceivablePlanSaveReqVO()
                .setContractId(9L).setCustomerId(999L);
        // mock 合同
        when(contractService.validateContract(9L)).thenReturn(new CrmContractDO().setId(9L).setCustomerId(20L));

        // 调用
        receivablePlanService.createReceivablePlan(reqVO);
        // 断言
        verify(receivablePlanMapper).insert(argThat((CrmReceivablePlanDO row) ->
                Objects.equals(row.getCustomerId(), 20L) && Objects.equals(row.getPeriod(), 1)));
    }
}
