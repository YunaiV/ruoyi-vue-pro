package cn.iocoder.yudao.module.ai.controller.admin.skill;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.skill.vo.skill.AiSkillRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.skill.AiSkillDO;
import cn.iocoder.yudao.module.ai.service.model.AiChatRoleService;
import cn.iocoder.yudao.module.ai.service.skill.AiSkillFileService;
import cn.iocoder.yudao.module.ai.service.skill.AiSkillService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link AiSkillController} 的单元测试类
 *
 * @author 芋道源码
 */
public class AiSkillControllerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private AiSkillController skillController;

    @Mock
    private AiSkillService skillService;
    @Mock
    private AiSkillFileService skillFileService;
    @Mock
    private AiChatRoleService chatRoleService;

    @Test
    public void testGetSkill_fillFileCountAndRoleNames() {
        // mock 方法：技能 5 被角色 1、2 引用，角色 3 引用其它技能
        when(skillService.getSkill(5L)).thenReturn(new AiSkillDO().setId(5L).setName("order-analysis"));
        when(skillFileService.getSkillFileCountMap(singletonList(5L))).thenReturn(singletonMap(5L, 4));
        when(chatRoleService.getChatRoleListBySkillIds(singletonList(5L))).thenReturn(Arrays.asList(
                new AiChatRoleDO().setId(1L).setName("客服").setSkillIds(singletonList(5L)),
                new AiChatRoleDO().setId(2L).setName("分析师").setSkillIds(Arrays.asList(6L, 5L)),
                new AiChatRoleDO().setId(3L).setName("其它").setSkillIds(singletonList(6L))));

        // 调用
        CommonResult<AiSkillRespVO> result = skillController.getSkill(5L);
        // 断言：与分页一致，回填文件数量、引用的角色
        assertTrue(result.isSuccess());
        AiSkillRespVO skill = result.getData();
        assertEquals(5L, skill.getId());
        assertEquals("order-analysis", skill.getName());
        assertEquals(4, skill.getFileCount());
        assertEquals(Arrays.asList("客服", "分析师"), skill.getRoleNames());
    }

    @Test
    public void testGetSkill_notExists() {
        // 调用
        CommonResult<AiSkillRespVO> result = skillController.getSkill(5L);
        // 断言：不存在时返回空，不查询关联数据
        assertTrue(result.isSuccess());
        assertNull(result.getData());
        verifyNoInteractions(skillFileService, chatRoleService);
    }

}
