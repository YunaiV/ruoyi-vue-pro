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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.List;

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
        // 调用
        skillFileService.createDefaultSkillFileList(new Ai1SkillDO().setId(SKILL_ID).setName("pdf-reader").setDescription("读取 PDF"));

        // 断言：SKILL.md、scripts/、reference/ 均为根级锁定节点；SKILL.md 含 frontmatter
        List<Ai1SkillFileDO> files = skillFileMapper.selectListBySkillId(SKILL_ID);
        assertEquals(3, files.size());
        assertTrue(files.stream().allMatch(file -> Boolean.TRUE.equals(file.getLocked())
                && Ai1SkillFileDO.PARENT_ID_ROOT.equals(file.getParentId())));
        Ai1SkillFileDO skillFile = CollUtil.findOne(files, file -> Ai1SkillFileDO.NAME_SKILL.equals(file.getName()));
        assertNotNull(skillFile);
        assertTrue(skillFile.getContent().startsWith("---\nname: pdf-reader\ndescription: 读取 PDF\n---"));
    }

    @Test
    public void testCreateSkillFile_success() {
        // 调用：根级新建 md 文件
        Long id = skillFileService.createSkillFile(buildCreateReqVO(Ai1SkillFileDO.PARENT_ID_ROOT, " guide.MD ",
                Ai1SkillFileTypeEnum.FILE.getType()));

        // 断言：名称去空白、扩展名小写、默认内容；并刷新 SKILL 更新时间
        Ai1SkillFileDO file = skillFileMapper.selectById(id);
        assertEquals("guide.MD", file.getName());
        assertEquals("md", file.getFileType());
        assertTrue(file.getContent().startsWith("# 新文档"));
        assertFalse(file.getLocked());
        verify(skillService).touchSkill(SKILL_ID);
    }

    @Test
    public void testCreateSkillFile_nameInvalid() {
        assertServiceException(() -> skillFileService.createSkillFile(buildCreateReqVO(Ai1SkillFileDO.PARENT_ID_ROOT, "..",
                Ai1SkillFileTypeEnum.FILE.getType())), SKILL_FILE_NAME_INVALID);
        assertServiceException(() -> skillFileService.createSkillFile(buildCreateReqVO(Ai1SkillFileDO.PARENT_ID_ROOT, "a/b",
                Ai1SkillFileTypeEnum.FILE.getType())), SKILL_FILE_NAME_INVALID);
    }

    @Test
    public void testCreateSkillFile_parentIsFile() {
        // mock 数据：父节点是文件
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
    public void testRenameSkillFile_locked() {
        // mock 数据
        Ai1SkillFileDO file = insertSkillFile(Ai1SkillFileDO.PARENT_ID_ROOT, "SKILL.md", Ai1SkillFileTypeEnum.FILE.getType(), true);

        // 调用，并断言异常
        assertServiceException(() -> skillFileService.renameSkillFile(new Ai1SkillFileRenameReqVO().setId(file.getId())
                .setName("README.md")), SKILL_FILE_LOCKED, "重命名");
    }

    @Test
    public void testMoveSkillFile_intoDescendant() {
        // mock 数据：a/ → a/b/
        Ai1SkillFileDO a = insertSkillFile(Ai1SkillFileDO.PARENT_ID_ROOT, "a", Ai1SkillFileTypeEnum.DIRECTORY.getType(), false);
        Ai1SkillFileDO b = insertSkillFile(a.getId(), "b", Ai1SkillFileTypeEnum.DIRECTORY.getType(), false);

        // 调用，并断言异常：不能把 a 移入 a/b
        assertServiceException(() -> skillFileService.moveSkillFile(new Ai1SkillFileMoveReqVO().setId(a.getId())
                .setParentId(b.getId())), SKILL_FILE_MOVE_TO_SELF);
        assertServiceException(() -> skillFileService.moveSkillFile(new Ai1SkillFileMoveReqVO().setId(a.getId())
                .setParentId(a.getId())), SKILL_FILE_MOVE_TO_SELF);
    }

    @Test
    public void testMoveSkillFile_success() {
        // mock 数据：a/、c.txt
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
        // mock 数据：a/b/c.txt，以及同级的 d.txt
        Ai1SkillFileDO a = insertSkillFile(Ai1SkillFileDO.PARENT_ID_ROOT, "a", Ai1SkillFileTypeEnum.DIRECTORY.getType(), false);
        Ai1SkillFileDO b = insertSkillFile(a.getId(), "b", Ai1SkillFileTypeEnum.DIRECTORY.getType(), false);
        insertSkillFile(b.getId(), "c.txt", Ai1SkillFileTypeEnum.FILE.getType(), false);
        Ai1SkillFileDO d = insertSkillFile(Ai1SkillFileDO.PARENT_ID_ROOT, "d.txt", Ai1SkillFileTypeEnum.FILE.getType(), false);

        // 调用：删除 a，再在根级重建同名目录
        skillFileService.deleteSkillFile(a.getId());
        Long newId = skillFileService.createSkillFile(buildCreateReqVO(Ai1SkillFileDO.PARENT_ID_ROOT, "a",
                Ai1SkillFileTypeEnum.DIRECTORY.getType()));

        // 断言：a 及全部后代已删除，d.txt 保留；同名目录可重建
        List<Ai1SkillFileDO> files = skillFileMapper.selectListBySkillId(SKILL_ID);
        assertEquals(2, files.size());
        assertTrue(convertList(files, Ai1SkillFileDO::getId).containsAll(Arrays.asList(d.getId(), newId)));
    }

    // ========== 随机对象 ==========

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
