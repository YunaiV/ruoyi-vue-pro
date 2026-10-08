package cn.iocoder.yudao.module.ai1.service.skill;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.skill.Ai1SkillSaveReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.skill.Ai1SkillDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.skill.Ai1SkillMapper;
import cn.iocoder.yudao.module.ai1.service.agent.Ai1AgentService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link Ai1SkillServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(Ai1SkillServiceImpl.class)
public class Ai1SkillServiceImplTest extends BaseDbUnitTest {

    @Resource
    private Ai1SkillServiceImpl skillService;

    @Resource
    private Ai1SkillMapper skillMapper;

    @MockitoBean
    private Ai1SkillFileService skillFileService;
    @MockitoBean
    private Ai1AgentService agentService;

    @Test
    public void testCreateSkill_success() {
        // 准备参数
        Ai1SkillSaveReqVO reqVO = buildSkillSaveReqVO("pdf-reader");

        // 调用
        Long id = skillService.createSkill(reqVO);
        // 断言
        Ai1SkillDO skill = skillMapper.selectById(id);
        assertEquals("pdf-reader", skill.getName());
        verify(skillFileService).createDefaultSkillFileList(any(Ai1SkillDO.class));
    }

    @Test
    public void testCreateSkill_duplicate() {
        // mock 数据
        skillService.createSkill(buildSkillSaveReqVO("pdf-reader"));

        // 调用，并断言异常
        assertServiceException(() -> skillService.createSkill(buildSkillSaveReqVO("pdf-reader")),
                SKILL_NAME_DUPLICATE, "pdf-reader");
    }

    @Test
    public void testDeleteSkill_thenRecreateSameName() {
        // mock 数据
        Long id = skillService.createSkill(buildSkillSaveReqVO("pdf-reader"));

        // 调用
        skillService.deleteSkill(id);
        Long newId = skillService.createSkill(buildSkillSaveReqVO("pdf-reader"));
        // 断言
        assertNull(skillMapper.selectById(id));
        assertNotEquals(id, newId);
        assertEquals("pdf-reader", skillMapper.selectById(newId).getName());
        verify(skillFileService).deleteSkillFileListBySkillIds(eq(Collections.singletonList(id)));
    }

    @Test
    public void testUpdateSkill_duplicate() {
        // mock 数据
        skillService.createSkill(buildSkillSaveReqVO("pdf-reader"));
        Long id = skillService.createSkill(buildSkillSaveReqVO("excel-reader"));

        // 准备参数
        Ai1SkillSaveReqVO reqVO = buildSkillSaveReqVO("pdf-reader").setId(id);

        // 调用，并断言异常
        assertServiceException(() -> skillService.updateSkill(reqVO), SKILL_NAME_DUPLICATE, "pdf-reader");
    }

    @Test
    public void testDeleteSkill_usedByAgent() {
        // mock 数据
        Long id = skillService.createSkill(buildSkillSaveReqVO("pdf-reader"));
        // mock agentService 的方法
        when(agentService.getAgentCountBySkillIds(Collections.singletonList(id))).thenReturn(1L);

        // 调用，并断言异常
        assertServiceException(() -> skillService.deleteSkill(id), SKILL_USED_BY_AGENT);
        // 断言
        assertNotNull(skillMapper.selectById(id));
        verify(skillFileService, never()).deleteSkillFileListBySkillIds(any());
    }

    @Test
    public void testUpdateSkill_managementInfoOnly() {
        // mock 数据
        Long id = skillService.createSkill(buildSkillSaveReqVO("pdf-reader"));
        clearInvocations(skillFileService);

        // 调用
        skillService.updateSkill(buildSkillSaveReqVO("财务 PDF 工具").setId(id).setDescription("给财务人员使用"));
        // 断言
        Ai1SkillDO skill = skillMapper.selectById(id);
        assertEquals("财务 PDF 工具", skill.getName());
        assertEquals("给财务人员使用", skill.getDescription());
        verifyNoInteractions(skillFileService);
    }

    // ========== 测试数据 ==========

    private static Ai1SkillSaveReqVO buildSkillSaveReqVO(String name) {
        return new Ai1SkillSaveReqVO().setName(name).setDescription("读取 PDF").setVersion("1.0")
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
    }

}
