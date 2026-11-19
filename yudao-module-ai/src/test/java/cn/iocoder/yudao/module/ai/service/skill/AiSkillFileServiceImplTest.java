package cn.iocoder.yudao.module.ai.service.skill;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.skill.vo.file.AiSkillFileContentReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.skill.vo.file.AiSkillFileCreateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.skill.vo.file.AiSkillFileMoveReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.skill.vo.file.AiSkillFileRenameReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.skill.AiSkillDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.skill.AiSkillFileDO;
import cn.iocoder.yudao.module.ai.dal.mysql.skill.AiSkillFileMapper;
import cn.iocoder.yudao.module.ai.enums.skill.AiSkillFileTypeEnum;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link AiSkillFileServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(AiSkillFileServiceImpl.class)
public class AiSkillFileServiceImplTest extends BaseDbUnitTest {

    private static final Long SKILL_ID = 1L;
    private static final String SKILL_NAME = "order-analysis";

    @Resource
    private AiSkillFileServiceImpl skillFileService;

    @Resource
    private AiSkillFileMapper skillFileMapper;

    @MockBean
    private AiSkillService skillService;

    @BeforeEach
    public void setUp() {
        // mock 技能
        when(skillService.validateSkillExists(eq(SKILL_ID)))
                .thenReturn(new AiSkillDO().setId(SKILL_ID).setName(SKILL_NAME));
    }

    // ========== 固定文件 ==========

    @Test
    public void testCreateDefaultSkillFileList() {
        // 准备参数
        AiSkillDO skill = new AiSkillDO().setId(SKILL_ID).setName(SKILL_NAME).setDescription("订单统计\r\n口径");

        // 调用
        skillFileService.createDefaultSkillFileList(skill);
        // 断言：固定节点 SKILL.md、scripts/、reference/
        List<AiSkillFileDO> files = skillFileMapper.selectListBySkillId(SKILL_ID);
        assertEquals(3, files.size());
        assertTrue(files.stream().allMatch(file -> Boolean.TRUE.equals(file.getLocked())
                && AiSkillFileDO.PARENT_ID_ROOT.equals(file.getParentId())));
        // 断言：SKILL.md 头信息，描述保持单行
        AiSkillFileDO skillFile = skillFileMapper.selectBySkillIdAndParentIdAndName(
                SKILL_ID, AiSkillFileDO.PARENT_ID_ROOT, AiSkillFileDO.NAME_SKILL);
        assertTrue(skillFile.getContent().startsWith("---\nname: order-analysis\ndescription: 订单统计  口径\n---"));
        assertEquals(StrUtil.utf8Bytes(skillFile.getContent()).length, skillFile.getSize());
        assertEquals("订单统计  口径", AiSkillFileServiceImpl.validateSkillFrontmatter(
                new AiSkillDO().setName(SKILL_NAME), skillFile.getContent()));
    }

    @Test
    public void testCreateDefaultSkillFileList_descriptionInvalid() {
        // 调用，并断言异常：生成的 SKILL.md 必须能原样保存
        assertServiceException(() -> skillFileService.createDefaultSkillFileList(
                new AiSkillDO().setId(SKILL_ID).setName(SKILL_NAME).setDescription(" \n ")),
                SKILL_FILE_SKILL_MD_INVALID, "缺少 description");
        assertServiceException(() -> skillFileService.createDefaultSkillFileList(
                new AiSkillDO().setId(SKILL_ID).setName(SKILL_NAME).setDescription("> 多行")),
                SKILL_FILE_SKILL_MD_INVALID, "description 需要写在一行");
        assertTrue(skillFileMapper.selectListBySkillId(SKILL_ID).isEmpty());
    }

    // ========== 新建 / 重命名 / 移动 / 删除 ==========

    @Test
    public void testCreateSkillFile_success() {
        // 准备参数
        AiSkillFileCreateReqVO reqVO = buildCreateReqVO(AiSkillFileDO.PARENT_ID_ROOT, " guide.MD ",
                AiSkillFileTypeEnum.FILE.getType());

        // 调用
        Long id = skillFileService.createSkillFile(reqVO);
        // 断言
        AiSkillFileDO file = skillFileMapper.selectById(id);
        assertEquals("guide.MD", file.getName());
        assertEquals("md", file.getFileType());
        assertTrue(file.getContent().startsWith("# 新文档"));
        assertEquals(StrUtil.utf8Bytes(file.getContent()).length, file.getSize());
        assertFalse(file.getLocked());
    }

    @Test
    public void testCreateSkillFile_typeNotSupported() {
        // 调用，并断言异常：只允许文本文件
        assertServiceException(() -> skillFileService.createSkillFile(buildCreateReqVO(AiSkillFileDO.PARENT_ID_ROOT,
                "stats.py", AiSkillFileTypeEnum.FILE.getType())), SKILL_FILE_TYPE_NOT_SUPPORTED,
                "stats.py", "md、txt、json、yaml、yml、csv");
        assertServiceException(() -> skillFileService.createSkillFile(buildCreateReqVO(AiSkillFileDO.PARENT_ID_ROOT,
                "README", AiSkillFileTypeEnum.FILE.getType())), SKILL_FILE_TYPE_NOT_SUPPORTED,
                "README", "md、txt、json、yaml、yml、csv");
    }

    @Test
    public void testCreateSkillFile_nameInvalid() {
        // 调用，并断言异常
        assertServiceException(() -> skillFileService.createSkillFile(buildCreateReqVO(AiSkillFileDO.PARENT_ID_ROOT,
                "..", AiSkillFileTypeEnum.DIRECTORY.getType())), SKILL_FILE_NAME_INVALID, 100);
        assertServiceException(() -> skillFileService.createSkillFile(buildCreateReqVO(AiSkillFileDO.PARENT_ID_ROOT,
                "a/b.md", AiSkillFileTypeEnum.FILE.getType())), SKILL_FILE_NAME_INVALID, 100);
    }

    @Test
    public void testCreateSkillFile_duplicate() {
        // mock 数据
        insertSkillFile(AiSkillFileDO.PARENT_ID_ROOT, "a.md", AiSkillFileTypeEnum.FILE.getType(), false);

        // 调用，并断言异常
        assertServiceException(() -> skillFileService.createSkillFile(buildCreateReqVO(AiSkillFileDO.PARENT_ID_ROOT,
                "a.md", AiSkillFileTypeEnum.FILE.getType())), SKILL_FILE_NAME_DUPLICATE, "a.md");
    }

    @Test
    public void testRenameSkillFile_locked() {
        // mock 数据
        AiSkillFileDO file = insertSkillFile(AiSkillFileDO.PARENT_ID_ROOT, AiSkillFileDO.NAME_SKILL,
                AiSkillFileTypeEnum.FILE.getType(), true);
        // 准备参数
        AiSkillFileRenameReqVO reqVO = new AiSkillFileRenameReqVO();
        reqVO.setId(file.getId());
        reqVO.setName("README.md");

        // 调用，并断言异常
        assertServiceException(() -> skillFileService.renameSkillFile(reqVO), SKILL_FILE_LOCKED, "重命名");
    }

    @Test
    public void testRenameSkillFile_success() {
        // mock 数据
        AiSkillFileDO file = insertSkillFile(AiSkillFileDO.PARENT_ID_ROOT, "a.md", AiSkillFileTypeEnum.FILE.getType(), false);
        // 准备参数
        AiSkillFileRenameReqVO reqVO = new AiSkillFileRenameReqVO();
        reqVO.setId(file.getId());
        reqVO.setName("a.json");

        // 调用
        skillFileService.renameSkillFile(reqVO);
        // 断言
        AiSkillFileDO dbFile = skillFileMapper.selectById(file.getId());
        assertEquals("a.json", dbFile.getName());
        assertEquals("json", dbFile.getFileType());
    }

    @Test
    public void testMoveSkillFile_toDescendant() {
        // mock 数据：a/ → a/b/
        AiSkillFileDO a = insertSkillFile(AiSkillFileDO.PARENT_ID_ROOT, "a", AiSkillFileTypeEnum.DIRECTORY.getType(), false);
        AiSkillFileDO b = insertSkillFile(a.getId(), "b", AiSkillFileTypeEnum.DIRECTORY.getType(), false);
        // 准备参数
        AiSkillFileMoveReqVO reqVO = new AiSkillFileMoveReqVO();
        reqVO.setId(a.getId());
        reqVO.setParentId(b.getId());

        // 调用，并断言异常
        assertServiceException(() -> skillFileService.moveSkillFile(reqVO), SKILL_FILE_MOVE_TO_DESCENDANT);
    }

    @Test
    public void testDeleteSkillFile_directory() {
        // mock 数据：a/ → a/b.md
        AiSkillFileDO a = insertSkillFile(AiSkillFileDO.PARENT_ID_ROOT, "a", AiSkillFileTypeEnum.DIRECTORY.getType(), false);
        AiSkillFileDO b = insertSkillFile(a.getId(), "b.md", AiSkillFileTypeEnum.FILE.getType(), false);

        // 调用
        skillFileService.deleteSkillFile(a.getId());
        // 断言：递归删除后代
        assertNull(skillFileMapper.selectById(a.getId()));
        assertNull(skillFileMapper.selectById(b.getId()));
    }

    // ========== 保存内容 ==========

    @Test
    public void testUpdateSkillFileContent_skillFile() {
        // mock 数据
        AiSkillFileDO file = insertSkillFile(AiSkillFileDO.PARENT_ID_ROOT, AiSkillFileDO.NAME_SKILL,
                AiSkillFileTypeEnum.FILE.getType(), true);
        // 准备参数
        String content = "---\nname: order-analysis\ndescription: \"订单统计口径与报表格式\"\n---\n\n# 订单统计\n";

        // 调用
        skillFileService.updateSkillFileContent(buildContentReqVO(file.getId(), content));
        // 断言：内容、大小
        AiSkillFileDO dbFile = skillFileMapper.selectById(file.getId());
        assertEquals(content, dbFile.getContent());
        assertEquals(StrUtil.utf8Bytes(content).length, dbFile.getSize());
        // 断言：描述同步到技能
        verify(skillService).updateSkillDescription(eq(SKILL_ID), eq("订单统计口径与报表格式"));
    }

    @Test
    public void testUpdateSkillFileContent_skillFileInvalid() {
        // mock 数据
        AiSkillFileDO file = insertSkillFile(AiSkillFileDO.PARENT_ID_ROOT, AiSkillFileDO.NAME_SKILL,
                AiSkillFileTypeEnum.FILE.getType(), true);

        // 调用，并断言异常：name 与技能名称不一致
        assertServiceException(() -> skillFileService.updateSkillFileContent(buildContentReqVO(file.getId(),
                "---\nname: other\ndescription: 描述\n---\n")), SKILL_FILE_SKILL_MD_INVALID,
                "name(other) 必须与技能名称(order-analysis)一致");
        // 调用，并断言异常：缺少 description
        assertServiceException(() -> skillFileService.updateSkillFileContent(buildContentReqVO(file.getId(),
                "---\nname: order-analysis\n---\n")), SKILL_FILE_SKILL_MD_INVALID, "缺少 description");
        // 调用，并断言异常：没有头信息
        assertServiceException(() -> skillFileService.updateSkillFileContent(buildContentReqVO(file.getId(),
                "# 订单统计\n")), SKILL_FILE_SKILL_MD_INVALID, "必须以 --- 开头");
        verify(skillService, never()).updateSkillDescription(any(), any());
    }

    @Test
    public void testUpdateSkillFileContent_sizeExceed() {
        // mock 数据
        AiSkillFileDO file = insertSkillFile(AiSkillFileDO.PARENT_ID_ROOT, "a.md", AiSkillFileTypeEnum.FILE.getType(), false);

        // 调用，并断言异常：单个文件超过 256 KB
        assertServiceException(() -> skillFileService.updateSkillFileContent(buildContentReqVO(file.getId(),
                StrUtil.repeat('a', 256 * 1024 + 1))), SKILL_FILE_SIZE_EXCEED, 256, 257);
    }

    @Test
    public void testUpdateSkillFileContent_totalSizeExceed() {
        // mock 数据：已有文件合计 1900 KB
        insertSkillFile(AiSkillFileDO.PARENT_ID_ROOT, "a.md", AiSkillFileTypeEnum.FILE.getType(), false, 1900 * 1024);
        AiSkillFileDO file = insertSkillFile(AiSkillFileDO.PARENT_ID_ROOT, "b.md", AiSkillFileTypeEnum.FILE.getType(), false);

        // 调用，并断言异常：保存 200 KB 后合计超过 2 MB
        assertServiceException(() -> skillFileService.updateSkillFileContent(buildContentReqVO(file.getId(),
                StrUtil.repeat('a', 200 * 1024))), SKILL_FILE_TOTAL_SIZE_EXCEED, 2, 2100);
    }

    // ========== 读取 ==========

    @Test
    public void testReadSkillFile_success() {
        // mock 数据：reference/guide.md
        AiSkillFileDO reference = insertSkillFile(AiSkillFileDO.PARENT_ID_ROOT, "reference",
                AiSkillFileTypeEnum.DIRECTORY.getType(), true);
        AiSkillFileDO guide = insertSkillFile(reference.getId(), "guide.md", AiSkillFileTypeEnum.FILE.getType(), false);
        skillFileMapper.updateById(new AiSkillFileDO().setId(guide.getId()).setContent("# 报表格式"));
        // 准备参数
        AiSkillDO skill = new AiSkillDO().setId(SKILL_ID).setName(SKILL_NAME);

        // 调用，并断言：文件
        assertEquals("# 报表格式", skillFileService.readSkillFile(skill, "reference/guide.md"));
        // 调用，并断言：目录返回子节点
        assertEquals("路径(reference)是目录，包含：guide.md", skillFileService.readSkillFile(skill, "reference"));
        // 调用，并断言：不存在
        assertEquals("技能(order-analysis)中不存在文件(reference/none.md)",
                skillFileService.readSkillFile(skill, "reference/none.md"));
        // 调用，并断言：文件路径列表
        assertEquals(Arrays.asList("reference/guide.md"), skillFileService.getSkillFilePathList(SKILL_ID));
    }

    @Test
    public void testReadSkillFile_pathInvalid() {
        // 准备参数
        AiSkillDO skill = new AiSkillDO().setId(SKILL_ID).setName(SKILL_NAME);

        // 调用，并断言：拒绝越界、绝对路径
        for (String path : asList("../SKILL.md", "reference/../SKILL.md", "/etc/passwd",
                "reference\\a.md", "./SKILL.md", "reference//a.md")) {
            assertTrue(skillFileService.readSkillFile(skill, path).startsWith("路径(" + path + ")不合法"), path);
        }
    }

    @Test
    public void testReadSkillFile_colonName() {
        // mock 数据：名称规则允许冒号，读取规则需要一致
        Long id = skillFileService.createSkillFile(buildCreateReqVO(AiSkillFileDO.PARENT_ID_ROOT,
                "a:guide.md", AiSkillFileTypeEnum.FILE.getType()));
        skillFileMapper.updateById(new AiSkillFileDO().setId(id).setContent("# 冒号"));

        // 调用，并断言
        assertEquals("# 冒号", skillFileService.readSkillFile(new AiSkillDO().setId(SKILL_ID).setName(SKILL_NAME),
                "a:guide.md"));
    }

    // ========== 辅助方法 ==========

    private static AiSkillFileCreateReqVO buildCreateReqVO(Long parentId, String name, Integer type) {
        AiSkillFileCreateReqVO reqVO = new AiSkillFileCreateReqVO();
        reqVO.setSkillId(SKILL_ID);
        reqVO.setParentId(parentId);
        reqVO.setName(name);
        reqVO.setType(type);
        return reqVO;
    }

    private static AiSkillFileContentReqVO buildContentReqVO(Long id, String content) {
        AiSkillFileContentReqVO reqVO = new AiSkillFileContentReqVO();
        reqVO.setId(id);
        reqVO.setContent(content);
        return reqVO;
    }

    private AiSkillFileDO insertSkillFile(Long parentId, String name, Integer type, boolean locked) {
        return insertSkillFile(parentId, name, type, locked, 0);
    }

    private AiSkillFileDO insertSkillFile(Long parentId, String name, Integer type, boolean locked, int size) {
        AiSkillFileDO file = AiSkillFileDO.builder().skillId(SKILL_ID).parentId(parentId).name(name).type(type)
                .fileType(AiSkillFileTypeEnum.isFile(type) ? StrUtil.subAfter(name, '.', true) : null)
                .content(AiSkillFileTypeEnum.isFile(type) ? "" : null).size(size).locked(locked).sort(1).build();
        skillFileMapper.insert(file);
        return file;
    }

}
