package cn.iocoder.yudao.module.ai.service.skill;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.skill.vo.skill.AiSkillSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.skill.AiSkillDO;
import cn.iocoder.yudao.module.ai.dal.mysql.skill.AiSkillMapper;
import cn.iocoder.yudao.module.ai.service.model.AiChatRoleService;
import cn.iocoder.yudao.module.ai.tool.skill.AiSkillToolCallback;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link AiSkillServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(AiSkillServiceImpl.class)
public class AiSkillServiceImplTest extends BaseDbUnitTest {

    private static final Long ROLE_ID = 10L;

    @Resource
    private AiSkillServiceImpl skillService;

    @Resource
    private AiSkillMapper skillMapper;

    @MockBean
    private AiSkillFileService skillFileService;
    @MockBean
    private AiChatRoleService chatRoleService;

    // ========== 管理 ==========

    @Test
    public void testCreateSkill_success() {
        // 准备参数
        AiSkillSaveReqVO reqVO = buildSaveReqVO("order-analysis", "订单统计\n口径");

        // 调用
        Long id = skillService.createSkill(reqVO);
        // 断言：描述保持单行
        AiSkillDO skill = skillMapper.selectById(id);
        assertEquals("order-analysis", skill.getName());
        assertEquals("订单统计 口径", skill.getDescription());
        assertEquals("1.0.0", skill.getVersion());
        // 断言：创建固定文件
        verify(skillFileService).createDefaultSkillFileList(argThat(item -> id.equals(item.getId())));
    }

    @Test
    public void testCreateSkill_nameDuplicate() {
        // mock 数据
        insertSkill("order-analysis", CommonStatusEnum.ENABLE.getStatus());

        // 调用，并断言异常
        assertServiceException(() -> skillService.createSkill(buildSaveReqVO("order-analysis", "描述")),
                SKILL_NAME_DUPLICATE, "order-analysis");
    }

    @Test
    public void testUpdateSkill_success() {
        // mock 数据
        AiSkillDO dbSkill = insertSkill("order-analysis", CommonStatusEnum.ENABLE.getStatus());
        // 准备参数
        AiSkillSaveReqVO reqVO = buildSaveReqVO("other-name", "其它描述");
        reqVO.setId(dbSkill.getId());
        reqVO.setVersion("1.1.0");
        reqVO.setStatus(CommonStatusEnum.DISABLE.getStatus());

        // 调用
        skillService.updateSkill(reqVO);
        // 断言：名称不可修改，描述以 SKILL.md 为准
        AiSkillDO skill = skillMapper.selectById(dbSkill.getId());
        assertEquals("order-analysis", skill.getName());
        assertEquals(dbSkill.getDescription(), skill.getDescription());
        assertEquals("1.1.0", skill.getVersion());
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), skill.getStatus());
    }

    @Test
    public void testDeleteSkill_success() {
        // mock 数据
        AiSkillDO dbSkill = insertSkill("order-analysis", CommonStatusEnum.ENABLE.getStatus());

        // 调用
        skillService.deleteSkill(dbSkill.getId());
        // 断言
        assertNull(skillMapper.selectById(dbSkill.getId()));
        verify(skillFileService).deleteSkillFileListBySkillId(eq(dbSkill.getId()));
    }

    @Test
    public void testDeleteSkill_notExists() {
        assertServiceException(() -> skillService.deleteSkill(1L), SKILL_NOT_EXISTS);
    }

    @Test
    public void testValidateSkillListEnable() {
        // mock 数据
        AiSkillDO enable = insertSkill("order-analysis", CommonStatusEnum.ENABLE.getStatus());
        AiSkillDO disable = insertSkill("refund-policy", CommonStatusEnum.DISABLE.getStatus());

        // 调用：启用的技能，通过
        skillService.validateSkillListEnable(singletonList(enable.getId()));
        // 调用，并断言异常：停用
        assertServiceException(() -> skillService.validateSkillListEnable(Arrays.asList(enable.getId(), disable.getId())),
                SKILL_DISABLE, "refund-policy");
        // 调用，并断言异常：不存在（包括其它租户的技能）
        assertServiceException(() -> skillService.validateSkillListEnable(singletonList(disable.getId() + 100)),
                SKILL_NOT_EXISTS);
    }

    // ========== 对话 ==========

    @Test
    public void testGetSkillToolCallbackList() {
        // mock 数据
        AiSkillDO enable = insertSkill("order-analysis", CommonStatusEnum.ENABLE.getStatus());
        AiSkillDO disable = insertSkill("refund-policy", CommonStatusEnum.DISABLE.getStatus());
        // 准备参数
        AiChatRoleDO role = new AiChatRoleDO().setId(ROLE_ID).setSkillIds(Arrays.asList(enable.getId(), disable.getId()));

        // 调用
        List<ToolCallback> toolCallbacks = skillService.getSkillToolCallbackList(role);
        // 断言：两个只读工具，只包含启用的技能
        assertEquals(Arrays.asList(AiSkillToolCallback.NAME_READ_SKILL, AiSkillToolCallback.NAME_READ_SKILL_FILE),
                toolCallbacks.stream().map(callback -> callback.getToolDefinition().name()).collect(Collectors.toList()));
        String systemMessage = AiSkillToolCallback.appendSkillPrompt("你是数据分析师", toolCallbacks);
        assertTrue(systemMessage.startsWith("你是数据分析师\n\n## 可用技能\n"));
        assertTrue(systemMessage.contains("- order-analysis：" + enable.getDescription()));
        assertFalse(systemMessage.contains("refund-policy"));
        // 断言：全部停用时，不返回工具
        assertTrue(skillService.getSkillToolCallbackList(new AiChatRoleDO().setId(ROLE_ID)
                .setSkillIds(singletonList(disable.getId()))).isEmpty());
    }

    @Test
    public void testReadSkillFile_skillFile() {
        // mock 数据
        AiSkillDO skill = insertSkill("order-analysis", CommonStatusEnum.ENABLE.getStatus());
        mockChatRole(skill.getId());
        when(skillFileService.readSkillFile(argThat(item -> skill.getId().equals(item.getId())), eq("SKILL.md")))
                .thenReturn("# 订单统计");
        when(skillFileService.getSkillFilePathList(eq(skill.getId())))
                .thenReturn(Arrays.asList("SKILL.md", "reference/guide.md"));

        // 调用
        String result = skillService.readSkillFile(ROLE_ID, "order-analysis", null);
        // 断言：其它文件列表 + SKILL.md 正文
        assertEquals("技能内的其它文件（需要时使用 read_skill_file 读取）：\n- reference/guide.md\n\n---\n# 订单统计", result);
    }

    @Test
    public void testReadSkillFile_skillFileTruncate() {
        // mock 数据：SKILL.md 正文超过 16000 字
        AiSkillDO skill = insertSkill("order-analysis", CommonStatusEnum.ENABLE.getStatus());
        mockChatRole(skill.getId());
        when(skillFileService.readSkillFile(any(), eq("SKILL.md"))).thenReturn(StrUtil.repeat('a', 20000));
        when(skillFileService.getSkillFilePathList(eq(skill.getId())))
                .thenReturn(Arrays.asList("SKILL.md", "reference/guide.md"));

        // 调用
        String result = skillService.readSkillFile(ROLE_ID, "order-analysis", null);
        // 断言：截断后仍保留文件列表，并明确提示
        assertTrue(result.startsWith("技能内的其它文件（需要时使用 read_skill_file 读取）：\n- reference/guide.md\n\n---\naaa"));
        assertTrue(result.endsWith("本次仅返回前 16000 字]"));
    }

    @Test
    public void testReadSkillFile_truncate() {
        // mock 数据
        AiSkillDO skill = insertSkill("order-analysis", CommonStatusEnum.ENABLE.getStatus());
        mockChatRole(skill.getId());
        when(skillFileService.readSkillFile(any(), eq("reference/guide.md"))).thenReturn(StrUtil.repeat('a', 16001));

        // 调用
        String result = skillService.readSkillFile(ROLE_ID, "order-analysis", "reference/guide.md");
        // 断言：超长截断，并明确提示
        assertTrue(result.startsWith(StrUtil.repeat('a', 16000) + "\n\n[内容过长已截断：共 16001 字，本次仅返回前 16000 字]"));
    }

    @Test
    public void testReadSkillFile_refused() {
        // mock 数据
        AiSkillDO enable = insertSkill("order-analysis", CommonStatusEnum.ENABLE.getStatus());
        AiSkillDO disable = insertSkill("refund-policy", CommonStatusEnum.DISABLE.getStatus());
        insertSkill("not-bound", CommonStatusEnum.ENABLE.getStatus());
        mockChatRole(enable.getId(), disable.getId());

        // 调用，并断言：停用、未绑定、不存在，都拒绝
        assertEquals("技能(refund-policy)已停用或不可用", skillService.readSkillFile(ROLE_ID, "refund-policy", null));
        assertEquals("技能(not-bound)已停用或不可用", skillService.readSkillFile(ROLE_ID, "not-bound", null));
        assertEquals("技能(none)已停用或不可用", skillService.readSkillFile(ROLE_ID, "none", null));
        // 调用，并断言：角色不存在
        assertEquals("技能(order-analysis)已停用或不可用", skillService.readSkillFile(ROLE_ID + 1, "order-analysis", null));
        verify(skillFileService, never()).readSkillFile(any(), any());
    }

    @Test
    public void testSkillToolCallback_call() {
        // mock 数据
        AiSkillDO skill = insertSkill("order-analysis", CommonStatusEnum.ENABLE.getStatus());
        mockChatRole(skill.getId());
        when(skillFileService.readSkillFile(any(), any())).thenReturn("内容");
        List<ToolCallback> toolCallbacks = skillService.getSkillToolCallbackList(
                new AiChatRoleDO().setId(ROLE_ID).setSkillIds(singletonList(skill.getId())));
        ToolCallback readSkill = toolCallbacks.get(0);
        ToolCallback readSkillFile = toolCallbacks.get(1);

        // 调用，并断言：read_skill 读取 SKILL.md
        assertEquals("内容", readSkill.call("{\"name\":\"order-analysis\"}"));
        verify(skillFileService).readSkillFile(any(), eq("SKILL.md"));
        // 调用，并断言：read_skill_file 读取指定文件
        assertEquals("内容", readSkillFile.call("{\"name\":\"order-analysis\",\"path\":\"reference/guide.md\"}"));
        verify(skillFileService).readSkillFile(any(), eq("reference/guide.md"));
        // 调用，并断言：read_skill_file 缺少 path
        assertEquals("参数 path 不能为空，请传入技能内文件的相对路径，例如 reference/guide.md",
                readSkillFile.call("{\"name\":\"order-analysis\"}"));
        // 调用，并断言：每次读取都重新校验，停用后拒绝
        skillMapper.updateById(new AiSkillDO().setId(skill.getId()).setStatus(CommonStatusEnum.DISABLE.getStatus()));
        assertEquals("技能(order-analysis)已停用或不可用", readSkill.call("{\"name\":\"order-analysis\"}"));
        verify(skillFileService, times(2)).readSkillFile(any(), any());
    }

    // ========== 辅助方法 ==========

    private void mockChatRole(Long... skillIds) {
        when(chatRoleService.getChatRole(eq(ROLE_ID)))
                .thenReturn(new AiChatRoleDO().setId(ROLE_ID).setSkillIds(Arrays.asList(skillIds)));
    }

    private static AiSkillSaveReqVO buildSaveReqVO(String name, String description) {
        AiSkillSaveReqVO reqVO = new AiSkillSaveReqVO();
        reqVO.setName(name);
        reqVO.setDescription(description);
        reqVO.setVersion("1.0.0");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return reqVO;
    }

    private AiSkillDO insertSkill(String name, Integer status) {
        AiSkillDO skill = AiSkillDO.builder().name(name).description(name + " 的描述")
                .version("1.0.0").status(status).build();
        skillMapper.insert(skill);
        return skill;
    }

}
