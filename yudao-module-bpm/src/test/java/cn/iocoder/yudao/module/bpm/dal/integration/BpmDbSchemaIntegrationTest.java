package cn.iocoder.yudao.module.bpm.dal.integration;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbIntegrationTest;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmCategoryDO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmFormDO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.category.BpmCategoryMapper;
import cn.iocoder.yudao.module.bpm.dal.mysql.definition.BpmFormMapper;
import cn.iocoder.yudao.module.bpm.dal.mysql.definition.BpmProcessDefinitionInfoMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BPM 模块数据库结构集成测试
 */
@DisplayName("BPM模块-数据库结构集成测试")
public class BpmDbSchemaIntegrationTest extends BaseDbIntegrationTest {

    @Resource
    private BpmCategoryMapper categoryMapper;

    @Resource
    private BpmFormMapper formMapper;

    @Resource
    private BpmProcessDefinitionInfoMapper processDefinitionInfoMapper;

    @Test
    @DisplayName("验证 bpm_category 表结构")
    void testCategoryTable() {
        List<BpmCategoryDO> list = categoryMapper.selectList();
        assertNotNull(list, "流程分类列表不应为空");
        System.out.println("bpm_category 表记录数: " + list.size());
    }

    @Test
    @DisplayName("验证 bpm_form 表结构")
    void testFormTable() {
        List<BpmFormDO> list = formMapper.selectList();
        assertNotNull(list, "流程表单列表不应为空");
        System.out.println("bpm_form 表记录数: " + list.size());
    }

    @Test
    @DisplayName("验证 bpm_process_definition_info 表结构")
    void testProcessDefinitionInfoTable() {
        List<BpmProcessDefinitionInfoDO> list = processDefinitionInfoMapper.selectList();
        assertNotNull(list, "流程定义信息列表不应为空");
        System.out.println("bpm_process_definition_info 表记录数: " + list.size());
    }

    @Test
    @DisplayName("综合验证 - 所有核心表结构")
    void testAllCoreTables() {
        System.out.println("========== 开始综合验证 BPM 模块表结构 ==========");
        assertDoesNotThrow(() -> categoryMapper.selectList(), "bpm_category 表查询失败");
        assertDoesNotThrow(() -> formMapper.selectList(), "bpm_form 表查询失败");
        assertDoesNotThrow(() -> processDefinitionInfoMapper.selectList(), "bpm_process_definition_info 表查询失败");
        System.out.println("========== BPM 模块表结构验证通过 ==========");
    }
}