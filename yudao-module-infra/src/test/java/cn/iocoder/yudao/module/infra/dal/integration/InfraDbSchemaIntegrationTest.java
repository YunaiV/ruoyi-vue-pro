package cn.iocoder.yudao.module.infra.dal.integration;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbIntegrationTest;
import cn.iocoder.yudao.module.infra.dal.dataobject.config.ConfigDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileConfigDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.job.JobDO;
import cn.iocoder.yudao.module.infra.dal.mysql.config.ConfigMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.file.FileConfigMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.job.JobMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Infra 模块数据库结构集成测试
 */
@Import({
        ConfigMapper.class,
        FileConfigMapper.class,
        JobMapper.class
})
@DisplayName("Infra模块-数据库结构集成测试")
public class InfraDbSchemaIntegrationTest extends BaseDbIntegrationTest {

    @Resource
    private ConfigMapper configMapper;

    @Resource
    private FileConfigMapper fileConfigMapper;

    @Resource
    private JobMapper jobMapper;

    @Test
    @DisplayName("验证 infra_config 表结构")
    void testConfigTable() {
        List<ConfigDO> list = configMapper.selectList();
        assertNotNull(list, "配置列表不应为空");
        System.out.println("infra_config 表记录数: " + list.size());
        if (!list.isEmpty()) {
            ConfigDO config = list.get(0);
            assertNotNull(config.getId(), "ID不应为空");
            System.out.println("示例配置: key=" + config.getConfigKey());
        }
    }

    @Test
    @DisplayName("验证 infra_file_config 表结构")
    void testFileConfigTable() {
        List<FileConfigDO> list = fileConfigMapper.selectList();
        assertNotNull(list, "文件配置列表不应为空");
        System.out.println("infra_file_config 表记录数: " + list.size());
    }

    @Test
    @DisplayName("验证 infra_job 表结构")
    void testJobTable() {
        List<JobDO> list = jobMapper.selectList();
        assertNotNull(list, "定时任务列表不应为空");
        System.out.println("infra_job 表记录数: " + list.size());
    }

    @Test
    @DisplayName("综合验证 - 所有核心表结构")
    void testAllCoreTables() {
        System.out.println("========== 开始综合验证 Infra 模块表结构 ==========");
        assertDoesNotThrow(() -> configMapper.selectList(), "infra_config 表查询失败");
        assertDoesNotThrow(() -> fileConfigMapper.selectList(), "infra_file_config 表查询失败");
        assertDoesNotThrow(() -> jobMapper.selectList(), "infra_job 表查询失败");
        System.out.println("========== Infra 模块表结构验证通过 ==========");
    }
}
