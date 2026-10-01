package cn.iocoder.yudao.module.ai1.service.skill;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.file.Ai1SkillFileContentReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.file.Ai1SkillFileCreateReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.file.Ai1SkillFileMoveReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.file.Ai1SkillFileRenameReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.skill.Ai1SkillDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.skill.Ai1SkillFileDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.skill.Ai1SkillFileMapper;
import cn.iocoder.yudao.module.ai1.enums.skill.Ai1SkillFileTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springaicommunity.agent.utils.MarkdownParser;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link Ai1SkillFileServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(Ai1SkillFileServiceImpl.class)
public class Ai1SkillFileServiceImplTest extends BaseDbUnitTest {

    private static final Long SKILL_ID = 1L;

    @Resource
    private Ai1SkillFileServiceImpl skillFileService;

    @Resource
    private Ai1SkillFileMapper skillFileMapper;

    @MockitoBean
    private Ai1SkillService skillService;

    @Test
    public void testCreateDefaultSkillFileList() {
        // 准备参数
        Ai1SkillDO skill = new Ai1SkillDO().setId(SKILL_ID).setName("pdf-reader").setDescription("读取 PDF");

        // 调用
        skillFileService.createDefaultSkillFileList(skill);
        // 断言
        List<Ai1SkillFileDO> files = skillFileMapper.selectListBySkillId(SKILL_ID);
        assertEquals(3, files.size());
        assertTrue(files.stream().allMatch(file -> Boolean.TRUE.equals(file.getLocked())
                && Ai1SkillFileDO.PARENT_ID_ROOT.equals(file.getParentId())));
        Ai1SkillFileDO skillFile = CollUtil.findOne(files, file -> Ai1SkillFileDO.NAME_SKILL.equals(file.getName()));
        assertNotNull(skillFile);
        assertTrue(skillFile.getContent().startsWith("---\nname: pdf-reader\ndescription: 读取 PDF\n---"));
    }

    @Test
    public void testCreateDefaultSkillFileList_descriptionWithNewlines() {
        // 准备参数
        Ai1SkillDO skill = new Ai1SkillDO().setId(SKILL_ID).setName("pdf-reader")
                .setDescription("读取 PDF\r\nversion: 2");

        // 调用
        skillFileService.createDefaultSkillFileList(skill);
        // 断言
        Ai1SkillFileDO skillFile = skillFileMapper.selectBySkillIdAndParentIdAndName(
                SKILL_ID, Ai1SkillFileDO.PARENT_ID_ROOT, Ai1SkillFileDO.NAME_SKILL);
        MarkdownParser parser = new MarkdownParser(skillFile.getContent());
        assertEquals(Map.of("name", "pdf-reader", "description", "读取 PDF  version: 2"), parser.getFrontMatter());
        assertTrue(parser.getContent().startsWith("# pdf-reader\n\n"));
    }

    @Test
    public void testCreateSkillFile_success() {
        // 准备参数
        Ai1SkillFileCreateReqVO reqVO = buildCreateReqVO(Ai1SkillFileDO.PARENT_ID_ROOT, " guide.MD ",
                Ai1SkillFileTypeEnum.FILE.getType());

        // 调用
        Long id = skillFileService.createSkillFile(reqVO);
        // 断言
        Ai1SkillFileDO file = skillFileMapper.selectById(id);
        assertEquals("guide.MD", file.getName());
        assertEquals("md", file.getFileType());
        assertTrue(file.getContent().startsWith("# 新文档"));
        assertFalse(file.getLocked());
        verify(skillService).touchSkill(SKILL_ID);
    }

    @Test
    public void testCreateSkillFile_nameInvalid() {
        // 调用，并断言异常
        assertServiceException(() -> skillFileService.createSkillFile(buildCreateReqVO(Ai1SkillFileDO.PARENT_ID_ROOT, "..",
                Ai1SkillFileTypeEnum.FILE.getType())), SKILL_FILE_NAME_INVALID);
        assertServiceException(() -> skillFileService.createSkillFile(buildCreateReqVO(Ai1SkillFileDO.PARENT_ID_ROOT, "a/b",
                Ai1SkillFileTypeEnum.FILE.getType())), SKILL_FILE_NAME_INVALID);
    }

    @Test
    public void testCreateSkillFile_parentIsFile() {
        // mock 数据
        Ai1SkillFileDO parent = insertSkillFile(Ai1SkillFileDO.PARENT_ID_ROOT, "a.txt", Ai1SkillFileTypeEnum.FILE.getType(), false);

        // 调用，并断言异常
        assertServiceException(() -> skillFileService.createSkillFile(buildCreateReqVO(parent.getId(), "b.txt",
                Ai1SkillFileTypeEnum.FILE.getType())), SKILL_FILE_PARENT_INVALID);
    }

    @Test
    public void testCreateSkillFile_duplicate() {
        // mock 数据
        insertSkillFile(Ai1SkillFileDO.PARENT_ID_ROOT, "a.txt", Ai1SkillFileTypeEnum.FILE.getType(), false);

        // 调用，并断言异常
        assertServiceException(() -> skillFileService.createSkillFile(buildCreateReqVO(Ai1SkillFileDO.PARENT_ID_ROOT, "a.txt",
                Ai1SkillFileTypeEnum.FILE.getType())), SKILL_FILE_NAME_DUPLICATE, "a.txt");
    }

    @Test
    public void testCreateSkillFile_parentNotExists() {
        // 调用，并断言异常
        assertServiceException(() -> skillFileService.createSkillFile(buildCreateReqVO(99999L, "b.txt",
                Ai1SkillFileTypeEnum.FILE.getType())), SKILL_FILE_PARENT_NOT_EXISTS);
    }

    @Test
    public void testRenameSkillFile_locked() {
        // mock 数据
        Ai1SkillFileDO file = insertSkillFile(Ai1SkillFileDO.PARENT_ID_ROOT, "SKILL.md", Ai1SkillFileTypeEnum.FILE.getType(), true);

        // 调用，并断言异常
        assertServiceException(() -> skillFileService.renameSkillFile(new Ai1SkillFileRenameReqVO().setId(file.getId())
                .setName("README.md")), SKILL_FILE_LOCKED, "重命名");
    }

    @Test
    public void testMoveSkillFile_intoDescendant() {
        // mock 数据
        Ai1SkillFileDO a = insertSkillFile(Ai1SkillFileDO.PARENT_ID_ROOT, "a", Ai1SkillFileTypeEnum.DIRECTORY.getType(), false);
        Ai1SkillFileDO b = insertSkillFile(a.getId(), "b", Ai1SkillFileTypeEnum.DIRECTORY.getType(), false);

        // 调用，并断言异常
        assertServiceException(() -> skillFileService.moveSkillFile(new Ai1SkillFileMoveReqVO().setId(a.getId())
                .setParentId(b.getId())), SKILL_FILE_MOVE_TO_DESCENDANT);
        assertServiceException(() -> skillFileService.moveSkillFile(new Ai1SkillFileMoveReqVO().setId(a.getId())
                .setParentId(a.getId())), SKILL_FILE_MOVE_TO_SELF);
    }

    @Test
    public void testMoveSkillFile_success() {
        // mock 数据
        Ai1SkillFileDO a = insertSkillFile(Ai1SkillFileDO.PARENT_ID_ROOT, "a", Ai1SkillFileTypeEnum.DIRECTORY.getType(), false);
        Ai1SkillFileDO c = insertSkillFile(Ai1SkillFileDO.PARENT_ID_ROOT, "c.txt", Ai1SkillFileTypeEnum.FILE.getType(), false);

        // 调用
        skillFileService.moveSkillFile(new Ai1SkillFileMoveReqVO().setId(c.getId()).setParentId(a.getId()));
        // 断言
        assertEquals(a.getId(), skillFileMapper.selectById(c.getId()).getParentId());
    }

    @Test
    public void testUpdateSkillFileContent_directory() {
        // mock 数据
        Ai1SkillFileDO a = insertSkillFile(Ai1SkillFileDO.PARENT_ID_ROOT, "a", Ai1SkillFileTypeEnum.DIRECTORY.getType(), false);

        // 调用，并断言异常
        assertServiceException(() -> skillFileService.updateSkillFileContent(new Ai1SkillFileContentReqVO().setId(a.getId())
                .setContent("x")), SKILL_FILE_NOT_FILE);
    }

    @Test
    public void testDeleteSkillFile_cascadeAndRecreate() {
        // mock 数据
        Ai1SkillFileDO a = insertSkillFile(Ai1SkillFileDO.PARENT_ID_ROOT, "a", Ai1SkillFileTypeEnum.DIRECTORY.getType(), false);
        Ai1SkillFileDO b = insertSkillFile(a.getId(), "b", Ai1SkillFileTypeEnum.DIRECTORY.getType(), false);
        insertSkillFile(b.getId(), "c.txt", Ai1SkillFileTypeEnum.FILE.getType(), false);
        Ai1SkillFileDO d = insertSkillFile(Ai1SkillFileDO.PARENT_ID_ROOT, "d.txt", Ai1SkillFileTypeEnum.FILE.getType(), false);

        // 调用
        skillFileService.deleteSkillFile(a.getId());
        Long newId = skillFileService.createSkillFile(buildCreateReqVO(Ai1SkillFileDO.PARENT_ID_ROOT, "a",
                Ai1SkillFileTypeEnum.DIRECTORY.getType()));
        // 断言
        List<Ai1SkillFileDO> files = skillFileMapper.selectListBySkillId(SKILL_ID);
        assertEquals(2, files.size());
        assertTrue(convertList(files, Ai1SkillFileDO::getId).containsAll(Arrays.asList(d.getId(), newId)));
    }

    // ========== 测试数据 ==========

    private static Ai1SkillFileCreateReqVO buildCreateReqVO(Long parentId, String name, Integer type) {
        return new Ai1SkillFileCreateReqVO().setSkillId(SKILL_ID).setParentId(parentId).setName(name).setType(type);
    }

    private Ai1SkillFileDO insertSkillFile(Long parentId, String name, Integer type, boolean locked) {
        Ai1SkillFileDO file = Ai1SkillFileDO.builder().skillId(SKILL_ID).parentId(parentId).name(name).type(type)
                .locked(locked).sort(1).content(Ai1SkillFileTypeEnum.isFile(type) ? "" : null).build();
        skillFileMapper.insert(file);
        return file;
    }

}
