package cn.iocoder.yudao.module.crm.dal.integration;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbIntegrationTest;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.clue.CrmClueDO;
import cn.iocoder.yudao.module.crm.dal.mysql.customer.CrmCustomerMapper;
import cn.iocoder.yudao.module.crm.dal.mysql.contract.CrmContractMapper;
import cn.iocoder.yudao.module.crm.dal.mysql.clue.CrmClueMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CRM 模块数据库结构集成测试
 */
@DisplayName("CRM模块-数据库结构集成测试")
public class CrmDbSchemaIntegrationTest extends BaseDbIntegrationTest {

    @Resource
    private CrmCustomerMapper customerMapper;

    @Resource
    private CrmContractMapper contractMapper;

    @Resource
    private CrmClueMapper clueMapper;

    @Test
    @DisplayName("验证 crm_customer 表结构")
    void testCustomerTable() {
        List<CrmCustomerDO> list = customerMapper.selectList();
        assertNotNull(list, "客户列表不应为空");
        System.out.println("crm_customer 表记录数: " + list.size());
    }

    @Test
    @DisplayName("验证 crm_contract 表结构")
    void testContractTable() {
        List<CrmContractDO> list = contractMapper.selectList();
        assertNotNull(list, "合同列表不应为空");
        System.out.println("crm_contract 表记录数: " + list.size());
    }

    @Test
    @DisplayName("验证 crm_clue 表结构")
    void testClueTable() {
        List<CrmClueDO> list = clueMapper.selectList();
        assertNotNull(list, "线索列表不应为空");
        System.out.println("crm_clue 表记录数: " + list.size());
    }

    @Test
    @DisplayName("综合验证 - 所有核心表结构")
    void testAllCoreTables() {
        System.out.println("========== 开始综合验证 CRM 模块表结构 ==========");
        assertDoesNotThrow(() -> customerMapper.selectList(), "crm_customer 表查询失败");
        assertDoesNotThrow(() -> contractMapper.selectList(), "crm_contract 表查询失败");
        assertDoesNotThrow(() -> clueMapper.selectList(), "crm_clue 表查询失败");
        System.out.println("========== CRM 模块表结构验证通过 ==========");
    }
}
