package cn.iocoder.yudao.module.ai1.service.skill;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.skill.Ai1SkillSaveReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.skill.Ai1SkillDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.skill.Ai1SkillMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

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

    @Test
    public void testCreateSkill_nameInvalid() {
        // 准备参数：名称包含路径分隔符
        Ai1SkillSaveReqVO reqVO = buildSkillSaveReqVO("pdf/reader");

        // 调用，并断言异常
        assertServiceException(() -> skillService.createSkill(reqVO), SKILL_NAME_INVALID);
    }

    @Test
    public void testCreateSkill_success() {
        // 准备参数：名称带首尾空白
        Ai1SkillSaveReqVO reqVO = buildSkillSaveReqVO(" pdf-reader ");

        // 调用
        Long id = skillService.createSkill(reqVO);

        // 断言：名称去空白后落库，并播种固定文件
        Ai1SkillDO skill = skillMapper.selectById(id);
        assertEquals("pdf-reader", skill.getName());
        verify(skillFileService).createSkillSeedFiles(any(Ai1SkillDO.class));
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

        // 调用：删除后，同名 SKILL 可以重建（删除时写入 deleted_at，退出唯一键的未删除区间）
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

        // 准备参数：改成已存在的名称
        Ai1SkillSaveReqVO reqVO = buildSkillSaveReqVO("pdf-reader").setId(id);

        // 调用，并断言异常
        assertServiceException(() -> skillService.updateSkill(reqVO), SKILL_NAME_DUPLICATE, "pdf-reader");
    }

    // ========== 随机对象 ==========

    private static Ai1SkillSaveReqVO buildSkillSaveReqVO(String name) {
        return new Ai1SkillSaveReqVO().setName(name).setDescription("读取 PDF").setVersion("1.0")
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
    }

}
