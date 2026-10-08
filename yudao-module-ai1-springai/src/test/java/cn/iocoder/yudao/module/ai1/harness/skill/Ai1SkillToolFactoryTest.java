package cn.iocoder.yudao.module.ai1.harness.skill;

import cn.hutool.core.io.FileUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai1.dal.dataobject.agent.Ai1AgentDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.skill.Ai1SkillDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.skill.Ai1SkillFileDO;
import cn.iocoder.yudao.module.ai1.enums.skill.Ai1SkillFileTypeEnum;
import cn.iocoder.yudao.module.ai1.framework.ai.config.YudaoAi1Properties;
import cn.iocoder.yudao.module.ai1.service.skill.Ai1SkillFileService;
import cn.iocoder.yudao.module.ai1.service.skill.Ai1SkillService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.ai.tool.ToolCallback;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

/**
 * {@link Ai1SkillToolFactory} 的单元测试
 *
 * @author 芋道源码
 */
public class Ai1SkillToolFactoryTest extends BaseMockitoUnitTest {

    private static final Long AGENT_ID = 1L;
    private static final Long SKILL_ID = 10L;
    private static final String SKILL_CONTENT = "---\nname: pdf-reader\ndescription: 读取 PDF\n---\n\n# pdf-reader\n";

    @InjectMocks
    private Ai1SkillToolFactory skillToolFactory;

    @Spy
    private YudaoAi1Properties ai1Properties = new YudaoAi1Properties();
    @Mock
    private Ai1SkillService skillService;
    @Mock
    private Ai1SkillFileService skillFileService;

    @TempDir
    private Path tempDir;

    @BeforeEach
    public void before() {
        // 设置 SKILL 文件目录
        ai1Properties.getSkill().setRoot(tempDir.toString());
    }

    @Test
    public void testBuildTools_materialize() {
        // mock skillService 和 skillFileService 的方法
        mockSkill(CommonStatusEnum.ENABLE.getStatus(), LocalDateTime.of(2026, 10, 1, 10, 0));
        mockSkillFiles("echo hi");

        // 调用
        List<Object> tools = skillToolFactory.buildTools(buildAgent());
        // 断言
        assertEquals(6, tools.size());
        assertInstanceOf(ToolCallback.class, tools.get(0));
        Path skillDirectory = agentRoot().resolve("pdf-reader");
        assertEquals(SKILL_CONTENT, FileUtil.readUtf8String(skillDirectory.resolve("SKILL.md").toFile()));
        assertEquals("echo hi", FileUtil.readUtf8String(skillDirectory.resolve("scripts/run.sh").toFile()));
        assertTrue(FileUtil.exist(agentRoot().resolve(".fingerprint").toFile()));
    }

    @Test
    public void testBuildTools_fingerprintReuseAndRebuild() {
        // mock skillService 和 skillFileService 的方法
        mockSkill(CommonStatusEnum.ENABLE.getStatus(), LocalDateTime.of(2026, 10, 1, 10, 0));
        mockSkillFiles("echo v1");
        Ai1AgentDO agent = buildAgent();

        // 调用
        skillToolFactory.buildTools(agent);
        skillToolFactory.buildTools(agent);
        // 断言
        verify(skillFileService, times(1)).getSkillFileListBySkillId(SKILL_ID);

        // mock 残留文件
        FileUtil.writeUtf8String("stale", agentRoot().resolve("stale.txt").toFile());
        // mock 更新时间和文件内容变化
        mockSkill(CommonStatusEnum.ENABLE.getStatus(), LocalDateTime.of(2026, 10, 1, 11, 0));
        mockSkillFiles("echo v2");

        // 调用
        skillToolFactory.buildTools(agent);
        // 断言
        verify(skillFileService, times(2)).getSkillFileListBySkillId(SKILL_ID);
        assertEquals("echo v2", FileUtil.readUtf8String(agentRoot().resolve("pdf-reader/scripts/run.sh").toFile()));
        assertFalse(FileUtil.exist(agentRoot().resolve("stale.txt").toFile()));
    }

    @Test
    public void testBuildTools_rebuildWhenDirectoryRemoved() {
        // mock skillService 和 skillFileService 的方法
        mockSkill(CommonStatusEnum.ENABLE.getStatus(), LocalDateTime.of(2026, 10, 1, 10, 0));
        mockSkillFiles("echo hi");
        Ai1AgentDO agent = buildAgent();
        skillToolFactory.buildTools(agent);

        // mock 物化目录被删除
        FileUtil.del(agentRoot());

        // 调用
        skillToolFactory.buildTools(agent);
        // 断言
        verify(skillFileService, times(2)).getSkillFileListBySkillId(SKILL_ID);
        assertTrue(FileUtil.exist(agentRoot().resolve("pdf-reader/SKILL.md").toFile()));
    }

    @Test
    public void testBuildTools_disabledSkill() {
        // mock skillService 和 skillFileService 的方法
        mockSkill(CommonStatusEnum.DISABLE.getStatus(), LocalDateTime.of(2026, 10, 1, 10, 0));

        // 调用
        List<Object> tools = skillToolFactory.buildTools(buildAgent());
        // 断言只保留执行工具，不物化 SKILL 文件
        assertEquals(5, tools.size());
        assertTrue(tools.stream().noneMatch(ToolCallback.class::isInstance));
        assertFalse(FileUtil.exist(agentRoot().resolve("pdf-reader").toFile()));
        verify(skillFileService, never()).getSkillFileListBySkillId(anyLong());
    }

    @Test
    public void testBuildTools_noSkill() {
        // 调用，并断言
        assertTrue(skillToolFactory.buildTools(new Ai1AgentDO().setId(AGENT_ID).setSkillIds(List.of())).isEmpty());
        verify(skillService, never()).getSkillList(anyCollection());
    }

    @Test
    public void testEvict() {
        // mock skillService 和 skillFileService 的方法
        mockSkill(CommonStatusEnum.ENABLE.getStatus(), LocalDateTime.of(2026, 10, 1, 10, 0));
        mockSkillFiles("echo hi");
        Ai1AgentDO agent = buildAgent();
        skillToolFactory.buildTools(agent);
        assertTrue(FileUtil.exist(agentRoot().toFile()));

        // 调用
        skillToolFactory.evict(AGENT_ID);
        // 断言
        assertFalse(FileUtil.exist(agentRoot().toFile()));

        // 调用
        skillToolFactory.buildTools(agent);
        // 断言重新物化
        verify(skillFileService, times(2)).getSkillFileListBySkillId(SKILL_ID);
    }

    // ========== 测试数据 ==========

    private Path agentRoot() {
        return tempDir.resolve("agent_" + AGENT_ID);
    }

    private static Ai1AgentDO buildAgent() {
        return new Ai1AgentDO().setId(AGENT_ID).setSkillIds(List.of(SKILL_ID));
    }

    // ========== mock 方法 ==========

    private void mockSkill(Integer status, LocalDateTime updateTime) {
        Ai1SkillDO skill = new Ai1SkillDO().setId(SKILL_ID).setName("pdf-reader").setStatus(status);
        skill.setUpdateTime(updateTime);
        when(skillService.getSkillList(List.of(SKILL_ID))).thenReturn(List.of(skill));
    }

    private void mockSkillFiles(String scriptContent) {
        Ai1SkillFileDO skillFile = Ai1SkillFileDO.builder().id(100L).skillId(SKILL_ID).parentId(Ai1SkillFileDO.PARENT_ID_ROOT)
                .name(Ai1SkillFileDO.NAME_SKILL).type(Ai1SkillFileTypeEnum.FILE.getType()).content(SKILL_CONTENT).build();
        Ai1SkillFileDO scripts = Ai1SkillFileDO.builder().id(101L).skillId(SKILL_ID).parentId(Ai1SkillFileDO.PARENT_ID_ROOT)
                .name("scripts").type(Ai1SkillFileTypeEnum.DIRECTORY.getType()).build();
        Ai1SkillFileDO script = Ai1SkillFileDO.builder().id(102L).skillId(SKILL_ID).parentId(101L)
                .name("run.sh").type(Ai1SkillFileTypeEnum.FILE.getType()).content(scriptContent).build();
        when(skillFileService.getSkillFileListBySkillId(SKILL_ID)).thenReturn(Arrays.asList(skillFile, scripts, script));
    }

}
