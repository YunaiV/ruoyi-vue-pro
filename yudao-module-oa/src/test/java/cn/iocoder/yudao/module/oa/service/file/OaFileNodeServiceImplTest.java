package cn.iocoder.yudao.module.oa.service.file;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import cn.iocoder.yudao.module.oa.controller.admin.file.vo.node.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.file.OaFileFavoriteDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.file.OaFileNodeDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.file.OaFilePermissionDO;
import cn.iocoder.yudao.module.oa.dal.mysql.file.OaFileNodeMapper;
import cn.iocoder.yudao.module.oa.framework.config.OaProperties;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link OaFileNodeServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({OaFileNodeServiceImpl.class, OaProperties.class, ValidationAutoConfiguration.class})
public class OaFileNodeServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaFileNodeService fileNodeService;

    @Resource
    private OaFileNodeMapper fileNodeMapper;
    @Resource
    private OaProperties properties;

    @MockitoBean
    private OaFilePermissionService filePermissionService;
    @MockitoBean
    private OaFileFavoriteService fileFavoriteService;
    @MockitoBean
    private FileApi fileApi;
    @MockitoBean
    private AdminUserApi adminUserApi;

    @BeforeEach
    public void before() {
        // mock 当前已登录账号，用户必须存在，部门允许为空
        when(adminUserApi.getUser(anyLong())).thenAnswer(invocation ->
                new AdminUserRespDTO().setId(invocation.getArgument(0)));
    }

    @Test
    public void testCreateFileNode_duplicateName() {
        // mock 数据：同目录忽略大小写，其他用户和回收站不占用当前名称
        OaFileNodeDO first = randomFileNodeDO(1L, 0L, 0).setType(1).setName("Report.PDF");
        first.setCreator("10");
        fileNodeMapper.insert(first);
        OaFileNodeDO second = randomFileNodeDO(2L, 0L, 0).setType(1).setName("report(1).pdf");
        second.setCreator("10");
        fileNodeMapper.insert(second);
        OaFileNodeDO other = randomFileNodeDO(3L, 0L, 0).setType(1).setName("report(2).pdf");
        other.setCreator("20");
        fileNodeMapper.insert(other);
        // 准备参数
        OaFileNodeSaveReqVO reqVO = new OaFileNodeSaveReqVO().setParentId(0L).setType(1)
                .setName("report.pdf").setUrl("/files/report.pdf").setSize(1L);

        // 调用
        Long id = fileNodeService.createFileNode(reqVO, 10L);
        // 断言
        assertEquals("report(2).pdf", fileNodeMapper.selectById(id).getName());
        assertEquals("/files/report.pdf", fileNodeMapper.selectById(id).getUrl());
    }

    @Test
    public void testCreateFileNode_duplicateFolderName() {
        // mock 数据
        OaFileNodeDO folder = randomFileNodeDO(1L, 0L, 0).setName("资料.v1");
        folder.setCreator("10");
        fileNodeMapper.insert(folder);

        // 调用
        Long id = fileNodeService.createFileNode(new OaFileNodeSaveReqVO().setParentId(0L)
                .setType(0).setName("资料.v1"), 10L);
        // 断言：目录名称中的点不视为扩展名
        assertEquals("资料.v1(1)", fileNodeMapper.selectById(id).getName());
    }

    @Test
    public void testUpdateFileNodeName_duplicateAndSelf() {
        // mock 数据
        OaFileNodeDO node = randomFileNodeDO(1L, 0L, 0).setType(1).setName("原文件.txt").setUrl("/files/old.txt");
        node.setCreator("10");
        fileNodeMapper.insert(node);
        OaFileNodeDO existing = randomFileNodeDO(2L, 0L, 0).setType(1).setName("报告.pdf");
        existing.setCreator("10");
        fileNodeMapper.insert(existing);

        // 调用
        fileNodeService.updateFileNodeName(new OaFileNodeRenameReqVO().setId(1L).setName("报告.pdf"), 10L);
        fileNodeService.updateFileNodeName(new OaFileNodeRenameReqVO().setId(1L).setName("报告(1).pdf"), 10L);
        // 断言：再次提交原名称不追加序号，文件内容地址不改变
        OaFileNodeDO result = fileNodeMapper.selectById(1L);
        assertEquals("报告(1).pdf", result.getName());
        assertEquals("pdf", result.getExtension());
        assertEquals("/files/old.txt", result.getUrl());
    }

    @Test
    public void testUpdateFileNodeParent_duplicateNameAndSameParent() {
        // mock 数据
        OaFileNodeDO target = randomFileNodeDO(1L, 0L, 0).setName("目标目录");
        target.setCreator("10");
        fileNodeMapper.insert(target);
        OaFileNodeDO existing = randomFileNodeDO(2L, 1L, 0).setType(1).setName("报告.pdf");
        existing.setCreator("10");
        fileNodeMapper.insert(existing);
        OaFileNodeDO moving = randomFileNodeDO(3L, 0L, 0).setType(1).setName("报告.pdf").setUrl("/files/report.pdf");
        moving.setCreator("10");
        fileNodeMapper.insert(moving);

        // 调用
        fileNodeService.updateFileNodeParent(new OaFileNodeMoveReqVO().setId(3L).setParentId(1L), 10L);
        fileNodeService.updateFileNodeParent(new OaFileNodeMoveReqVO().setId(3L).setParentId(1L), 10L);
        // 断言
        assertEquals(1L, fileNodeMapper.selectById(3L).getParentId());
        assertEquals("报告(1).pdf", fileNodeMapper.selectById(3L).getName());
        assertEquals("/files/report.pdf", fileNodeMapper.selectById(3L).getUrl());
    }

    @Test
    public void testRestoreFileNode_duplicateName() {
        // mock 数据：本人两个同名候选，其他人的名称不占用个人根目录
        OaFileNodeDO existing = randomFileNodeDO(1L, 0L, 0).setName("报告.pdf").setType(1);
        existing.setCreator("10");
        fileNodeMapper.insert(existing);
        OaFileNodeDO second = randomFileNodeDO(2L, 0L, 0).setName("报告(1).pdf").setType(1);
        second.setCreator("10");
        fileNodeMapper.insert(second);
        OaFileNodeDO other = randomFileNodeDO(3L, 0L, 0).setName("报告(2).pdf").setType(1);
        other.setCreator("20");
        fileNodeMapper.insert(other);
        OaFileNodeDO recycled = randomFileNodeDO(4L, 999L, 1).setName("报告.pdf").setType(1);
        recycled.setCreator("10");
        fileNodeMapper.insert(recycled);

        // 调用
        fileNodeService.restoreFileNode(4L, 10L);
        // 断言
        assertEquals("报告(2).pdf", fileNodeMapper.selectById(4L).getName());
        assertEquals(0L, fileNodeMapper.selectById(4L).getParentId());
        assertEquals(0, fileNodeMapper.selectById(4L).getStatus());
    }

    @Test
    public void testCopyFileNode_directoryKeepsSourceAndSkipsRecycled() {
        // mock 数据
        OaFileNodeDO root = randomFileNodeDO(1L, 0L, 0).setName("资料"); root.setCreator("10");
        OaFileNodeDO folder = randomFileNodeDO(2L, 1L, 0).setName("子目录"); folder.setCreator("10");
        OaFileNodeDO file = randomFileNodeDO(3L, 2L, 0).setType(1).setName("说明.pdf")
                .setUrl("/files/a.pdf").setSize(100L); file.setCreator("10");
        OaFileNodeDO recycled = randomFileNodeDO(4L, 1L, 1); recycled.setCreator("10");
        OaFileNodeDO siblingFile = randomFileNodeDO(5L, 1L, 0).setType(1).setName("附件.txt");
        siblingFile.setCreator("10");
        fileNodeMapper.insert(root); fileNodeMapper.insert(folder); fileNodeMapper.insert(file); fileNodeMapper.insert(recycled);
        fileNodeMapper.insert(siblingFile);

        // 调用
        Long id = fileNodeService.copyFileNode(new OaFileNodeCopyReqVO().setId(1L).setParentId(0L), 10L);
        // 断言
        assertEquals("资料(1)", fileNodeMapper.selectById(id).getName());
        List<OaFileNodeDO> copiedChildren = fileNodeMapper.selectListByParentId(id);
        assertEquals(2, copiedChildren.size());
        OaFileNodeDO copiedFolder = CollUtil.findOne(copiedChildren, node -> node.getType() == 0);
        assertNotNull(CollUtil.findOne(copiedChildren, node -> "附件.txt".equals(node.getName())));
        OaFileNodeDO copiedFile = CollUtil.getFirst(fileNodeMapper.selectListByParentId(copiedFolder.getId()));
        assertEquals("说明.pdf", copiedFile.getName());
        assertEquals(file.getUrl(), copiedFile.getUrl());
        assertEquals(9L, fileNodeMapper.selectCount());
        assertNotNull(fileNodeMapper.selectById(1L));
        assertEquals(2L, fileNodeMapper.selectById(3L).getParentId());
        verifyNoInteractions(fileApi, fileFavoriteService);
    }

    @Test
    public void testCopyFileNode_duplicateNameAndDescendant() {
        // mock 数据
        OaFileNodeDO root = randomFileNodeDO(1L, 0L, 0).setName("资料"); root.setCreator("10");
        OaFileNodeDO existing = randomFileNodeDO(2L, 0L, 0).setName("资料(1)"); existing.setCreator("10");
        OaFileNodeDO child = randomFileNodeDO(3L, 1L, 0); child.setCreator("10");
        fileNodeMapper.insert(root); fileNodeMapper.insert(existing); fileNodeMapper.insert(child);

        // 调用，并断言：不能复制到自身下级
        assertServiceException(() -> fileNodeService.copyFileNode(new OaFileNodeCopyReqVO().setId(1L)
                .setParentId(3L), 10L), FILE_NODE_PATH_INVALID);
        Long id = fileNodeService.copyFileNode(new OaFileNodeCopyReqVO().setId(1L).setParentId(0L), 10L);
        assertEquals("资料(2)", fileNodeMapper.selectById(id).getName());
    }

    @Test
    public void testCopyFileNode_otherUserWithoutPermission() {
        // mock 数据
        OaFileNodeDO root = randomFileNodeDO(1L, 0L, 0); root.setCreator("20");
        fileNodeMapper.insert(root);

        // 调用，并断言异常
        assertServiceException(() -> fileNodeService.copyFileNode(new OaFileNodeCopyReqVO().setId(1L)
                .setParentId(0L), 10L), FILE_NODE_ACCESS_DENIED);
        assertEquals(1L, fileNodeMapper.selectCount());
    }

    @Test
    public void testUpdateFileNodeParent_concurrentMutualMove() throws Exception {
        // mock 数据：同一用户的两个根目录
        OaFileNodeDO first = randomFileNodeDO(1L, 0L, 0).setName("目录 A");
        first.setCreator("10");
        OaFileNodeDO second = randomFileNodeDO(2L, 0L, 0).setName("目录 B");
        second.setCreator("10");
        fileNodeMapper.insert(first);
        fileNodeMapper.insert(second);
        // 准备参数：两个独立事务同时互相移动
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> firstMove = executor.submit(() -> {
                start.await();
                try {
                    fileNodeService.updateFileNodeParent(new OaFileNodeMoveReqVO().setId(1L).setParentId(2L), 10L);
                    return true;
                } catch (ServiceException e) {
                    assertEquals(FILE_NODE_MOVE_INVALID.getCode(), e.getCode());
                    return false;
                }
            });
            Future<Boolean> secondMove = executor.submit(() -> {
                start.await();
                try {
                    fileNodeService.updateFileNodeParent(new OaFileNodeMoveReqVO().setId(2L).setParentId(1L), 10L);
                    return true;
                } catch (ServiceException e) {
                    assertEquals(FILE_NODE_MOVE_INVALID.getCode(), e.getCode());
                    return false;
                }
            });
            // 调用，并断言：仅一个移动成功，最终仍有一个根目录
            start.countDown();
            assertNotEquals(firstMove.get(10, TimeUnit.SECONDS), secondMove.get(10, TimeUnit.SECONDS));
            assertTrue(fileNodeMapper.selectById(1L).getParentId() == 0L
                    || fileNodeMapper.selectById(2L).getParentId() == 0L);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void testUpdateFileNodeName_preserveUrl() {
        // mock 数据
        OaFileNodeDO node = randomFileNodeDO(1L, 0L, 0).setType(1).setName("原名称.txt")
                .setUrl("https://example.com/files/sample.txt");
        node.setCreator("1");
        fileNodeMapper.insert(node);

        // 准备参数：名称允许包含路径分隔符，但不参与文件地址拼接
        OaFileNodeRenameReqVO reqVO = new OaFileNodeRenameReqVO().setId(1L).setName("研发/说明.txt");

        // 调用
        fileNodeService.updateFileNodeName(reqVO, 1L);

        // 断言
        OaFileNodeDO result = fileNodeMapper.selectById(1L);
        assertEquals("研发/说明.txt", result.getName());
        assertEquals(node.getUrl(), result.getUrl());
        verifyNoInteractions(fileApi);
    }

    @Test
    public void testGetFileStorage_configuredSize() {
        // 准备参数
        Long originalSize = properties.getFile().getStorageSize();
        properties.getFile().setStorageSize(10L * 1024 * 1024 * 1024);
        try {
            // 调用
            OaFileStorageRespVO result = fileNodeService.getFileStorage(1L);

            // 断言
            assertEquals(10L * 1024 * 1024 * 1024, result.getTotalSize());
            assertEquals(0L, result.getUsedSize());
        } finally {
            properties.getFile().setStorageSize(originalSize);
        }
    }

    @Test
    public void testGetFileNode() {
        // mock 数据
        OaFileNodeDO node = randomFileNodeDO(1L, 0L, 0).setType(1)
                .setUrl("https://example.com/files/sample.txt");
        node.setCreator("1");
        fileNodeMapper.insert(node);

        // mock 方法
        when(fileApi.presignGetUrl(node.getUrl(), 300)).thenReturn("https://example.com/files/download.txt");
        when(fileFavoriteService.getFileFavoriteListByUserIdAndNodeIds(eq(1L), anyCollection()))
                .thenReturn(Collections.singletonList(new OaFileFavoriteDO().setNodeId(node.getId())));

        // 调用
        OaFileNodeRespVO result = fileNodeService.getFileNode(node.getId(), 1L);

        // 断言
        assertEquals(node.getId(), result.getId());
        assertEquals(node.getName(), result.getName());
        assertEquals(node.getExtension(), result.getExtension());
        assertEquals(4, result.getLevel());
        assertTrue(result.getFavorite());
        assertEquals("https://example.com/files/download.txt", result.getUrl());
        verify(fileApi).presignGetUrl(node.getUrl(), 300);
    }

    @Test
    public void testGetFileNode_readOnly() {
        // mock 数据
        OaFileNodeDO node = randomFileNodeDO(1L, 0L, 0).setType(1).setUrl("https://example.com/private.txt");
        node.setCreator("2");
        fileNodeMapper.insert(node);

        // mock 方法：仅授予查看文件信息权限
        OaFilePermissionDO permission = new OaFilePermissionDO().setNodeId(1L).setSubjectType(1)
                .setSubjectId(1L).setLevel(1);
        when(filePermissionService.getFilePermissionListByNodeIds(anyCollection()))
                .thenReturn(Collections.singletonList(permission));

        // 调用
        OaFileNodeRespVO result = fileNodeService.getFileNode(1L, 1L);

        // 断言
        assertEquals(node.getName(), result.getName());
        assertEquals(1, result.getLevel());
        assertFalse(result.getFavorite());
        assertNull(result.getUrl());
        verifyNoInteractions(fileApi);
    }

    @Test
    public void testGetFileNode_downloadPermission() {
        // mock 数据
        OaFileNodeDO node = randomFileNodeDO(1L, 0L, 0).setType(1).setUrl("https://example.com/private.txt");
        node.setCreator("2");
        fileNodeMapper.insert(node);

        // mock 方法
        OaFilePermissionDO permission = new OaFilePermissionDO().setNodeId(1L).setSubjectType(1)
                .setSubjectId(1L).setLevel(2);
        when(filePermissionService.getFilePermissionListByNodeIds(anyCollection()))
                .thenReturn(Collections.singletonList(permission));
        when(fileApi.presignGetUrl(node.getUrl(), 300)).thenReturn("https://example.com/download.txt");

        // 调用
        OaFileNodeRespVO result = fileNodeService.getFileNode(1L, 1L);

        // 断言
        assertEquals(2, result.getLevel());
        assertEquals("https://example.com/download.txt", result.getUrl());
        verify(fileApi).presignGetUrl(node.getUrl(), 300);
    }

    @Test
    public void testGetFileNode_accessDenied() {
        // mock 数据
        OaFileNodeDO node = randomFileNodeDO(1L, 0L, 0).setType(1);
        node.setCreator("2");
        fileNodeMapper.insert(node);

        // 调用，并断言异常
        assertServiceException(() -> fileNodeService.getFileNode(1L, 1L), FILE_NODE_ACCESS_DENIED);
        verifyNoInteractions(fileApi);
    }

    @Test
    public void testGetFileNode_expiredPermission() {
        // mock 数据
        OaFileNodeDO node = randomFileNodeDO(1L, 0L, 0).setType(1);
        node.setCreator("2");
        fileNodeMapper.insert(node);

        // mock 方法
        OaFilePermissionDO permission = new OaFilePermissionDO().setNodeId(1L).setSubjectType(1)
                .setSubjectId(1L).setLevel(2).setExpireTime(LocalDateTime.now().minusDays(1));
        when(filePermissionService.getFilePermissionListByNodeIds(anyCollection()))
                .thenReturn(Collections.singletonList(permission));

        // 调用，并断言异常
        assertServiceException(() -> fileNodeService.getFileNode(1L, 1L), FILE_NODE_ACCESS_DENIED);
        verifyNoInteractions(fileApi);
    }

    @Test
    public void testGetFileNode_folder() {
        // mock 数据：即使目录残留地址，也不能签发下载地址
        OaFileNodeDO node = randomFileNodeDO(1L, 0L, 0).setUrl("https://example.com/private.txt");
        node.setCreator("1");
        fileNodeMapper.insert(node);

        // 调用
        OaFileNodeRespVO result = fileNodeService.getFileNode(1L, 1L);

        // 断言
        assertEquals(0, result.getType());
        assertEquals(4, result.getLevel());
        assertNull(result.getUrl());
        verifyNoInteractions(fileApi);
    }

    @Test
    public void testGetFileNode_recycledParent() {
        // mock 数据：文件正常，但所属目录已回收
        OaFileNodeDO folder = randomFileNodeDO(1L, 0L, 1);
        folder.setCreator("1");
        fileNodeMapper.insert(folder);
        OaFileNodeDO node = randomFileNodeDO(2L, 1L, 0).setType(1);
        node.setCreator("1");
        fileNodeMapper.insert(node);

        // 调用，并断言异常
        assertServiceException(() -> fileNodeService.getFileNode(2L, 1L), FILE_NODE_NOT_AVAILABLE);
        verifyNoInteractions(fileApi);
    }

    @Test
    public void testGetFileNodePage_noUrl() {
        // mock 数据
        OaFileNodeDO node = randomFileNodeDO(1L, 0L, 0).setType(1).setUrl("https://example.com/private.txt");
        node.setCreator("1");
        fileNodeMapper.insert(node);

        // 准备参数：根目录走数据库分页，全局查询走候选范围分页
        OaFileNodePageReqVO reqVO = new OaFileNodePageReqVO().setScope("my").setParentId(0L);

        // 调用
        PageResult<OaFileNodeRespVO> rootPage = fileNodeService.getFileNodePage(reqVO, 1L);
        PageResult<OaFileNodeRespVO> searchPage = fileNodeService.getFileNodePage(reqVO.setParentId(null), 1L);

        // 断言
        assertEquals(1L, rootPage.getTotal());
        assertEquals(1L, searchPage.getTotal());
        assertNull(CollUtil.getFirst(rootPage.getList()).getUrl());
        assertNull(CollUtil.getFirst(searchPage.getList()).getUrl());
        verifyNoInteractions(fileApi);
    }

    @Test
    public void testGetFileDirectoryList_noUrl() {
        // mock 数据
        OaFileNodeDO node = randomFileNodeDO(1L, 0L, 0).setUrl("https://example.com/private.txt");
        node.setCreator("1");
        fileNodeMapper.insert(node);

        // 调用
        List<OaFileNodeRespVO> result = fileNodeService.getFileDirectoryList(1L);

        // 断言
        assertEquals(1, result.size());
        assertNull(CollUtil.getFirst(result).getUrl());
        verifyNoInteractions(fileApi);
    }

    @Test
    public void testGetFileStorage() {
        // mock 数据：本人正常文件与回收站文件计入容量，目录及他人文件不计入
        OaFileNodeDO normal = randomFileNodeDO(1L, 0L, 0).setType(1).setSize(100L);
        normal.setCreator("1");
        fileNodeMapper.insert(normal);
        OaFileNodeDO recycled = randomFileNodeDO(2L, 0L, 1).setType(1).setSize(200L);
        recycled.setCreator("1");
        fileNodeMapper.insert(recycled);
        OaFileNodeDO folder = randomFileNodeDO(3L, 0L, 0).setType(0).setSize(300L);
        folder.setCreator("1");
        fileNodeMapper.insert(folder);
        OaFileNodeDO shared = randomFileNodeDO(4L, 0L, 0).setType(1).setSize(400L);
        shared.setCreator("2");
        fileNodeMapper.insert(shared);

        // 调用
        OaFileStorageRespVO result = fileNodeService.getFileStorage(1L);

        // 断言
        assertEquals(300L, result.getUsedSize());
        assertEquals(5L * 1024 * 1024 * 1024, result.getTotalSize());
        assertEquals(1L, result.getFileCount());
        assertEquals(0L, result.getSharedCount());
        assertEquals(0L, result.getReceivedCount());
    }

    @Test
    public void testGetFileStorage_sharedCounts() {
        // mock 数据：本人目录多个接收人只计一次，共享给我的子文件不重复计数
        OaFileNodeDO own = randomFileNodeDO(1L, 0L, 0);
        own.setCreator("1");
        fileNodeMapper.insert(own);
        OaFileNodeDO received = randomFileNodeDO(2L, 0L, 0);
        received.setCreator("2");
        fileNodeMapper.insert(received);
        OaFileNodeDO child = randomFileNodeDO(3L, 2L, 0).setType(1);
        child.setCreator("2");
        fileNodeMapper.insert(child);
        OaFileNodeDO expired = randomFileNodeDO(4L, 0L, 0);
        expired.setCreator("1");
        fileNodeMapper.insert(expired);

        // mock 方法
        when(filePermissionService.getFilePermissionListByNodeIds(anyCollection())).thenReturn(Arrays.asList(
                new OaFilePermissionDO().setNodeId(1L).setSubjectType(1).setSubjectId(2L).setLevel(2).setInherit(true),
                new OaFilePermissionDO().setNodeId(1L).setSubjectType(1).setSubjectId(3L).setLevel(2).setInherit(true),
                new OaFilePermissionDO().setNodeId(2L).setSubjectType(1).setSubjectId(1L).setLevel(2).setInherit(true),
                new OaFilePermissionDO().setNodeId(4L).setSubjectType(1).setSubjectId(2L).setLevel(2)
                        .setExpireTime(LocalDateTime.now().minusDays(1))));

        when(filePermissionService.getValidFilePermissionList(1L, null)).thenReturn(Collections.singletonList(
                new OaFilePermissionDO().setNodeId(2L).setSubjectType(1).setSubjectId(1L).setLevel(2).setInherit(true)));

        // 调用
        OaFileStorageRespVO result = fileNodeService.getFileStorage(1L);

        // 断言
        assertEquals(1L, result.getSharedCount());
        assertEquals(1L, result.getReceivedCount());
    }

    @Test
    public void testGetFileNodePage_createTime() {
        // mock 数据
        LocalDateTime time = LocalDateTime.of(2026, 9, 10, 12, 0);
        OaFileNodeDO node = randomFileNodeDO(1L, 0L, 0).setType(1);
        node.setCreator("1");
        node.setCreateTime(time);
        fileNodeMapper.insert(node);

        // 准备参数：起止边界均包含
        OaFileNodePageReqVO reqVO = new OaFileNodePageReqVO().setScope("my")
                .setCreateTime(new LocalDateTime[]{time, time});

        // 调用，并断言
        assertEquals(1L, fileNodeService.getFileNodePage(reqVO, 1L).getTotal());
        reqVO.setCreateTime(new LocalDateTime[]{time.plusSeconds(1), time.plusDays(1)});
        assertEquals(0L, fileNodeService.getFileNodePage(reqVO, 1L).getTotal());
    }

    @Test
    public void testValidateFileNodeAvailable_root() {
        // mock 数据
        fileNodeMapper.insert(randomFileNodeDO(1L, 0L, 0));

        // 调用，并断言
        assertEquals(1L, fileNodeService.validateFileNodeAvailable(1L).getId());
    }

    @Test
    public void testValidateFileNodeAvailable_ancestorsNormal() {
        // mock 数据
        fileNodeMapper.insert(randomFileNodeDO(1L, 2L, 0));
        fileNodeMapper.insert(randomFileNodeDO(2L, 3L, 0));
        fileNodeMapper.insert(randomFileNodeDO(3L, 0L, 0));

        // 调用，并断言
        assertEquals(1L, fileNodeService.validateFileNodeAvailable(1L).getId());
    }

    @Test
    public void testValidateFileNodeAvailable_notExists() {

        // 调用，并断言异常
        assertServiceException(() -> fileNodeService.validateFileNodeAvailable(1L), FILE_NODE_NOT_EXISTS);
    }

    @Test
    public void testValidateFileNodeAvailable_selfRecycled() {
        // mock 数据
        fileNodeMapper.insert(randomFileNodeDO(1L, 0L, 1));

        // 调用，并断言异常
        assertServiceException(() -> fileNodeService.validateFileNodeAvailable(1L), FILE_NODE_NOT_AVAILABLE);
    }

    @Test
    public void testValidateFileNodeAvailable_ancestorRecycled() {
        // mock 数据
        fileNodeMapper.insert(randomFileNodeDO(1L, 2L, 0));
        fileNodeMapper.insert(randomFileNodeDO(2L, 3L, 0));
        fileNodeMapper.insert(randomFileNodeDO(3L, 0L, 1));

        // 调用，并断言异常
        assertServiceException(() -> fileNodeService.validateFileNodeAvailable(1L), FILE_NODE_NOT_AVAILABLE);
    }

    @Test
    public void testValidateFileNodeAvailable_parentDeleted() {
        // mock 数据
        fileNodeMapper.insert(randomFileNodeDO(1L, 2L, 0));
        fileNodeMapper.insert(randomFileNodeDO(2L, 0L, 0));
        fileNodeMapper.deleteById(2L);

        // 调用，并断言异常
        assertServiceException(() -> fileNodeService.validateFileNodeAvailable(1L), FILE_NODE_PATH_INVALID);
    }

    @Test
    public void testValidateFileNodeAvailable_parentMissing() {
        // mock 数据
        fileNodeMapper.insert(randomFileNodeDO(1L, 2L, 0));

        // 调用，并断言异常
        assertServiceException(() -> fileNodeService.validateFileNodeAvailable(1L), FILE_NODE_PATH_INVALID);
    }

    @Test
    public void testValidateFileNodeAvailable_cycle() {
        // mock 数据
        fileNodeMapper.insert(randomFileNodeDO(1L, 2L, 0));
        fileNodeMapper.insert(randomFileNodeDO(2L, 1L, 0));

        // 调用，并断言异常
        assertServiceException(() -> fileNodeService.validateFileNodeAvailable(1L), FILE_NODE_PATH_INVALID);
    }

    @Test
    public void testValidateFileNodeAvailable_parentNotFolder() {
        // mock 数据
        fileNodeMapper.insert(randomFileNodeDO(1L, 2L, 0));
        fileNodeMapper.insert(randomFileNodeDO(2L, 0L, 0).setType(1));

        // 调用，并断言异常
        assertServiceException(() -> fileNodeService.validateFileNodeAvailable(1L), FILE_NODE_PATH_INVALID);
    }

    @Test
    public void testValidateFileNodeAvailable_restoredParentKeepsChildRecycled() {
        // mock 数据
        fileNodeMapper.insert(randomFileNodeDO(1L, 2L, 1));
        fileNodeMapper.insert(randomFileNodeDO(2L, 0L, 1));
        fileNodeMapper.updateById(new OaFileNodeDO().setId(2L).setStatus(0));

        // 调用，并断言异常
        assertServiceException(() -> fileNodeService.validateFileNodeAvailable(1L), FILE_NODE_NOT_AVAILABLE);
    }

    @Test
    public void testGetFileNodePage_hideRecycledDescendants() {
        // mock 数据
        OaFileNodeDO parent = randomFileNodeDO(1L, 0L, 1);
        parent.setCreator("10");
        fileNodeMapper.insert(parent);
        OaFileNodeDO child = randomFileNodeDO(2L, 1L, 0);
        child.setCreator("10");
        fileNodeMapper.insert(child);
        // mock 方法
        when(filePermissionService.getFilePermissionListByNodeIds(anyCollection())).thenReturn(Collections.emptyList());
        when(fileFavoriteService.getFileFavoriteList(10L)).thenReturn(
                Arrays.asList(new OaFileFavoriteDO().setNodeId(2L).setUserId(10L)));

        // 调用，并断言：全局搜索和收藏均不能绕过回收目录
        assertEquals(0L, fileNodeService.getFileNodePage(new OaFileNodePageReqVO().setScope("my"), 10L).getTotal());
        assertEquals(0L, fileNodeService.getFileNodePage(new OaFileNodePageReqVO().setScope("favorite"), 10L).getTotal());
        assertEquals(1L, fileNodeService.getFileNodePage(new OaFileNodePageReqVO().setScope("recycle"), 10L).getTotal());
    }

    @Test
    public void testValidateFileNodePermission_inherit() {
        // mock 数据
        fileNodeMapper.insert(randomFileNodeDO(1L, 0L, 0));
        fileNodeMapper.insert(randomFileNodeDO(2L, 1L, 0));
        // mock 方法
        OaFilePermissionDO permission = new OaFilePermissionDO().setNodeId(1L).setSubjectType(1)
                .setSubjectId(10L).setLevel(2).setInherit(true);
        when(filePermissionService.getFilePermissionListByNodeIds(anyCollection())).thenReturn(Arrays.asList(permission));

        // 调用，并断言
        assertEquals(2L, fileNodeService.validateFileNodePermission(2L, 10L, 2).getId());
        permission.setInherit(false);
        assertServiceException(() -> fileNodeService.validateFileNodePermission(2L, 10L, 1), FILE_NODE_ACCESS_DENIED);
        permission.setInherit(true).setExpireTime(LocalDateTime.now().minusDays(1));
        assertServiceException(() -> fileNodeService.validateFileNodePermission(2L, 10L, 1), FILE_NODE_ACCESS_DENIED);
    }

    @Test
    public void testRecycleFileNode_preserveChildStatus() {
        // mock 数据
        OaFileNodeDO parent = randomFileNodeDO(1L, 0L, 0);
        parent.setCreator("10");
        fileNodeMapper.insert(parent);
        fileNodeMapper.insert(randomFileNodeDO(2L, 1L, 0));

        // 调用
        fileNodeService.recycleFileNode(1L, 10L);
        // 断言
        assertEquals(1, fileNodeMapper.selectById(1L).getStatus());
        assertEquals(0, fileNodeMapper.selectById(2L).getStatus());
    }

    @Test
    public void testDeleteFileNode_preserveChildren() {
        // mock 数据
        OaFileNodeDO parent = randomFileNodeDO(1L, 0L, 1);
        parent.setCreator("10");
        fileNodeMapper.insert(parent);
        fileNodeMapper.insert(randomFileNodeDO(2L, 1L, 0));
        fileNodeMapper.insert(randomFileNodeDO(3L, 2L, 1));

        // 调用
        fileNodeService.deleteFileNode(1L, 10L);
        // 断言
        assertNull(fileNodeMapper.selectById(1L));
        assertNotNull(fileNodeMapper.selectById(2L));
        assertNotNull(fileNodeMapper.selectById(3L));
        verify(filePermissionService).deleteFilePermissionsByNodeIds(Collections.singleton(1L));
        verify(fileFavoriteService).deleteFileFavoritesByNodeIds(Collections.singleton(1L));
        verifyNoInteractions(fileApi);
    }

    @Test
    public void testRecycleFileNode_notOwner() {
        // mock 数据
        OaFileNodeDO node = randomFileNodeDO(1L, 0L, 0);
        node.setCreator("20");
        fileNodeMapper.insert(node);

        // 调用，并断言异常
        assertServiceException(() -> fileNodeService.recycleFileNode(1L, 10L), FILE_NODE_ACCESS_DENIED);
    }

    @Test
    public void testUpdateFileNodeParent_descendant() {
        // mock 数据
        OaFileNodeDO parent = randomFileNodeDO(1L, 0L, 0);
        parent.setCreator("10");
        fileNodeMapper.insert(parent);
        OaFileNodeDO child = randomFileNodeDO(2L, 1L, 0);
        child.setCreator("10");
        fileNodeMapper.insert(child);

        // 调用，并断言异常
        assertServiceException(() -> fileNodeService.updateFileNodeParent(
                new OaFileNodeMoveReqVO().setId(1L).setParentId(2L), 10L), FILE_NODE_MOVE_INVALID);
    }

    @Test
    public void testRestoreFileNode_parentRecycled() {
        // mock 数据
        fileNodeMapper.insert(randomFileNodeDO(1L, 0L, 1));
        OaFileNodeDO child = randomFileNodeDO(2L, 1L, 1);
        child.setCreator("10");
        fileNodeMapper.insert(child);

        // 调用
        fileNodeService.restoreFileNode(2L, 10L);
        // 断言：原父目录仍在回收站，文件恢复到本人的根目录
        assertEquals(1, fileNodeMapper.selectById(1L).getStatus());
        assertEquals(0L, fileNodeMapper.selectById(2L).getParentId());
        assertEquals(0, fileNodeMapper.selectById(2L).getStatus());
    }

    @Test
    public void testCreateFileNode_frontendMetadata() {
        // 准备参数
        OaFileNodeSaveReqVO reqVO = new OaFileNodeSaveReqVO().setParentId(0L).setType(1)
                .setName("说明.pdf").setUrl("https://files.example.com/guide.pdf").setSize(1024L);

        // 调用
        Long id = fileNodeService.createFileNode(reqVO, 10L);
        // 断言：直接保存前端上传结果，不调用 Infra 查询接口
        OaFileNodeDO node = fileNodeMapper.selectById(id);
        assertEquals(reqVO.getUrl(), node.getUrl());
        assertEquals(1024L, node.getSize());
        assertEquals(2, node.getCategory());
        assertEquals(0, node.getStatus());
        verifyNoInteractions(fileApi);
    }

    @Test
    public void testCreateFileNode_storageFull() {
        // mock 数据：回收站文件仍占满本人容量
        OaFileNodeDO file = randomFileNodeDO(1L, 0L, 1).setType(1).setSize(properties.getFile().getStorageSize());
        file.setCreator("10");
        fileNodeMapper.insert(file);
        // 准备参数
        OaFileNodeSaveReqVO reqVO = new OaFileNodeSaveReqVO().setParentId(0L).setType(1)
                .setName("超额.pdf").setUrl("/files/extra.pdf").setSize(1L);

        // 调用，并断言异常
        assertServiceException(() -> fileNodeService.createFileNode(reqVO, 10L), FILE_STORAGE_NOT_ENOUGH);
        assertEquals(1L, fileNodeMapper.selectCount());
    }

    @Test
    public void testCreateFileNode_storageExactLimit() {
        // mock 数据：本人剩余 1 字节；其他用户容量不计入
        OaFileNodeDO file = randomFileNodeDO(1L, 0L, 0).setType(1).setSize(properties.getFile().getStorageSize() - 1);
        file.setCreator("10");
        fileNodeMapper.insert(file);
        OaFileNodeDO other = randomFileNodeDO(2L, 0L, 0).setType(1).setSize(properties.getFile().getStorageSize());
        other.setCreator("20");
        fileNodeMapper.insert(other);
        // 准备参数
        OaFileNodeSaveReqVO reqVO = new OaFileNodeSaveReqVO().setParentId(0L).setType(1)
                .setName("刚好.pdf").setUrl("/files/exact.pdf").setSize(1L);

        // 调用
        Long id = fileNodeService.createFileNode(reqVO, 10L);
        // 断言
        assertEquals(1L, fileNodeMapper.selectById(id).getSize());
    }

    @Test
    public void testCreateFileNode_folderWhenStorageFull() {
        // mock 数据
        OaFileNodeDO file = randomFileNodeDO(1L, 0L, 0).setType(1).setSize(properties.getFile().getStorageSize());
        file.setCreator("10");
        fileNodeMapper.insert(file);
        // 准备参数
        OaFileNodeSaveReqVO reqVO = new OaFileNodeSaveReqVO().setParentId(0L).setType(0).setName("新目录");

        // 调用
        Long id = fileNodeService.createFileNode(reqVO, 10L);
        // 断言：目录不占文件容量
        assertEquals(0L, fileNodeMapper.selectById(id).getSize());
    }

    @Test
    public void testCreateFileNode_relativeUrl() {
        // 准备参数
        OaFileNodeSaveReqVO reqVO = new OaFileNodeSaveReqVO().setParentId(0L).setType(1)
                .setName("说明.pdf").setUrl("/files/guide.pdf").setSize(1024L);

        // 调用
        Long id = fileNodeService.createFileNode(reqVO, 10L);
        // 断言
        assertEquals(reqVO.getUrl(), fileNodeMapper.selectById(id).getUrl());
    }

    @Test
    public void testGetFileNodePage_searchPreservesAncestorPermission() {
        // mock 数据：祖先不匹配搜索条件，但授权需要继承到匹配的文件
        OaFileNodeDO parent = randomFileNodeDO(1L, 0L, 0).setName("共享目录");
        parent.setCreator("20");
        fileNodeMapper.insert(parent);
        OaFileNodeDO child = randomFileNodeDO(2L, 1L, 0).setType(1).setCategory(2).setName("产品方案.pdf");
        child.setCreator("20");
        fileNodeMapper.insert(child);
        OaFileNodeDO unrelated = randomFileNodeDO(3L, 0L, 0).setName("其他目录");
        unrelated.setCreator("30");
        fileNodeMapper.insert(unrelated);
        // 准备参数
        OaFileNodePageReqVO reqVO = new OaFileNodePageReqVO().setScope("shared").setName("方案").setCategory(2);
        // mock 方法
        when(filePermissionService.getFilePermissionListByNodeIds(anyCollection())).thenReturn(Collections.singletonList(
                new OaFilePermissionDO().setNodeId(1L).setSubjectType(1).setSubjectId(10L).setLevel(2).setInherit(true)));
        when(filePermissionService.getValidFilePermissionList(10L, null)).thenReturn(Collections.singletonList(
                new OaFilePermissionDO().setNodeId(1L).setSubjectType(1).setSubjectId(10L).setLevel(2).setInherit(true)));

        // 调用
        PageResult<OaFileNodeRespVO> result =
                fileNodeService.getFileNodePage(reqVO, 10L);
        // 断言
        assertEquals(1L, result.getTotal());
        assertEquals(2L, CollUtil.getFirst(result.getList()).getId());
        assertEquals(2, CollUtil.getFirst(result.getList()).getLevel());
        verify(filePermissionService).getFilePermissionListByNodeIds(
                argThat(ids -> ids.size() == 2 && ids.containsAll(Arrays.asList(1L, 2L))));
    }

    @Test
    public void testGetFileNodePage_sharedRootDoesNotDuplicateChild() {
        // mock 数据
        OaFileNodeDO parent = randomFileNodeDO(1L, 0L, 0);
        parent.setCreator("20");
        fileNodeMapper.insert(parent);
        OaFileNodeDO child = randomFileNodeDO(2L, 1L, 0);
        child.setCreator("20");
        fileNodeMapper.insert(child);
        // mock 方法
        when(filePermissionService.getFilePermissionListByNodeIds(anyCollection())).thenReturn(Collections.singletonList(
                new OaFilePermissionDO().setNodeId(1L).setSubjectType(1).setSubjectId(10L).setLevel(2).setInherit(true)));
        when(filePermissionService.getValidFilePermissionList(10L, null)).thenReturn(Collections.singletonList(
                new OaFilePermissionDO().setNodeId(1L).setSubjectType(1).setSubjectId(10L).setLevel(2).setInherit(true)));

        // 调用
        PageResult<OaFileNodeRespVO> result = fileNodeService.getFileNodePage(
                new OaFileNodePageReqVO().setScope("shared").setParentId(0L), 10L);
        // 断言
        assertEquals(1L, result.getTotal());
        assertEquals(1L, CollUtil.getFirst(result.getList()).getId());
    }

    @Test
    public void testGetFileNodePage_secondPageAfterFiltering() {
        // mock 数据：无权限项不能占用当前页名额
        OaFileNodeDO first = randomFileNodeDO(1L, 0L, 0).setName("方案甲");
        first.setCreator("10");
        first.setCreateTime(LocalDateTime.of(2026, 9, 10, 12, 0));
        fileNodeMapper.insert(first);
        OaFileNodeDO second = randomFileNodeDO(2L, 0L, 0).setName("方案乙");
        second.setCreator("10");
        second.setCreateTime(first.getCreateTime());
        fileNodeMapper.insert(second);
        OaFileNodeDO hidden = randomFileNodeDO(3L, 0L, 0).setName("方案丙");
        hidden.setCreator("20");
        fileNodeMapper.insert(hidden);
        // 准备参数
        OaFileNodePageReqVO reqVO = new OaFileNodePageReqVO().setScope("my").setName("方案");
        reqVO.setPageNo(2);
        reqVO.setPageSize(1);
        // mock 方法：仅为当前页补充收藏状态
        when(fileFavoriteService.getFileFavoriteListByUserIdAndNodeIds(10L, Collections.singleton(1L)))
                .thenReturn(Collections.singletonList(new OaFileFavoriteDO().setNodeId(1L).setUserId(10L)));

        // 调用
        PageResult<OaFileNodeRespVO> result =
                fileNodeService.getFileNodePage(reqVO, 10L);
        // 断言
        assertEquals(2L, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals("10", fileNodeMapper.selectById(CollUtil.getFirst(result.getList()).getId()).getCreator());
        assertTrue(CollUtil.getFirst(result.getList()).getFavorite());
        verify(fileFavoriteService, never()).getFileFavoriteList(anyLong());
        verify(fileFavoriteService).getFileFavoriteListByUserIdAndNodeIds(eq(10L),
                argThat(ids -> ids.size() == 1 && ids.contains(CollUtil.getFirst(result.getList()).getId())));
    }

    @Test
    public void testGetFileNodePage_myRootDatabasePage() {
        // mock 数据：目录优先，同一创建时间按编号倒序；其他人、其他目录和回收项不进入分页
        LocalDateTime createTime = LocalDateTime.of(2026, 9, 10, 12, 0);
        for (long id = 1; id <= 6; id++) {
            OaFileNodeDO node = randomFileNodeDO(id, id == 5 ? 1L : 0L, id == 6 ? 1 : 0)
                    .setType(id == 3 ? 1 : 0);
            node.setCreator(id == 4 ? "20" : "10");
            node.setCreateTime(createTime);
            fileNodeMapper.insert(node);
        }
        // 准备参数
        OaFileNodePageReqVO reqVO = new OaFileNodePageReqVO().setScope("my").setParentId(0L);
        reqVO.setPageNo(2);
        reqVO.setPageSize(1);
        // mock 方法
        when(fileFavoriteService.getFileFavoriteListByUserIdAndNodeIds(10L, Collections.singleton(1L)))
                .thenReturn(Collections.singletonList(new OaFileFavoriteDO().setNodeId(1L).setUserId(10L)));

        // 调用
        PageResult<OaFileNodeRespVO> result = fileNodeService.getFileNodePage(reqVO, 10L);
        // 断言
        assertEquals(3L, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals(1L, CollUtil.getFirst(result.getList()).getId());
        assertEquals(4, CollUtil.getFirst(result.getList()).getLevel());
        assertTrue(CollUtil.getFirst(result.getList()).getFavorite());
        verify(fileFavoriteService, never()).getFileFavoriteList(anyLong());
        verifyNoInteractions(filePermissionService);
    }

    @Test
    public void testGetFileNodePage_myRootSearch() {
        // mock 数据：数据库分页保留名称、分类和时间的组合筛选
        LocalDateTime createTime = LocalDateTime.of(2026, 9, 10, 12, 0);
        OaFileNodeDO matched = randomFileNodeDO(1L, 0L, 0).setType(1).setName("方案.pdf").setCategory(1);
        matched.setCreator("10");
        matched.setCreateTime(createTime);
        fileNodeMapper.insert(matched);
        OaFileNodeDO other = randomFileNodeDO(2L, 0L, 0).setType(1).setName("其他.pdf").setCategory(1);
        other.setCreator("10");
        other.setCreateTime(createTime);
        fileNodeMapper.insert(other);
        // 准备参数
        OaFileNodePageReqVO reqVO = new OaFileNodePageReqVO().setScope("my").setParentId(0L)
                .setName("方案").setCategory(1).setCreateTime(new LocalDateTime[]{createTime, createTime});

        // 调用，并断言
        assertEquals(1L, fileNodeService.getFileNodePage(reqVO, 10L).getTotal());
        reqVO.setCategory(2);
        assertEquals(0L, fileNodeService.getFileNodePage(reqVO, 10L).getTotal());
        reqVO.setCategory(1).setCreateTime(new LocalDateTime[]{createTime.plusSeconds(1), createTime.plusDays(1)});
        assertEquals(0L, fileNodeService.getFileNodePage(reqVO, 10L).getTotal());
    }

    @Test
    public void testGetFileNodePage_myRootOutOfRange() {
        // mock 数据
        OaFileNodeDO node = randomFileNodeDO(1L, 0L, 0);
        node.setCreator("10");
        fileNodeMapper.insert(node);
        // 准备参数
        OaFileNodePageReqVO reqVO = new OaFileNodePageReqVO().setScope("my").setParentId(0L);
        reqVO.setPageNo(2);
        reqVO.setPageSize(1);

        // 调用
        PageResult<OaFileNodeRespVO> result = fileNodeService.getFileNodePage(reqVO, 10L);
        // 断言：越界页保留总数，不重复加载首页，也不查询收藏
        assertEquals(1L, result.getTotal());
        assertTrue(CollUtil.isEmpty(result.getList()));
        verifyNoInteractions(fileFavoriteService, filePermissionService);
    }

    @Test
    public void testGetFileNodePage_ownDirectoryDatabasePage() {
        // mock 数据：本人目录中的他人文件仍继承管理权限，不能按子节点创建人过滤
        OaFileNodeDO folder = randomFileNodeDO(1L, 0L, 0);
        folder.setCreator("10");
        fileNodeMapper.insert(folder);
        for (long id = 2; id <= 4; id++) {
            OaFileNodeDO node = randomFileNodeDO(id, 1L, id == 4 ? 1 : 0).setType(1);
            node.setCreator("20");
            node.setCreateTime(LocalDateTime.of(2026, 9, 10, 12, 0));
            fileNodeMapper.insert(node);
        }
        // 准备参数
        OaFileNodePageReqVO reqVO = new OaFileNodePageReqVO().setScope("my").setParentId(1L);
        reqVO.setPageNo(2);
        reqVO.setPageSize(1);

        // 调用
        PageResult<OaFileNodeRespVO> result = fileNodeService.getFileNodePage(reqVO, 10L);
        // 断言
        assertEquals(2L, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals(2L, CollUtil.getFirst(result.getList()).getId());
        assertEquals(4, CollUtil.getFirst(result.getList()).getLevel());
        verify(filePermissionService).getFilePermissionListByNodeIds(Collections.singleton(1L));
        verify(fileFavoriteService).getFileFavoriteListByUserIdAndNodeIds(10L, Collections.singleton(2L));
        verify(fileFavoriteService, never()).getFileFavoriteList(anyLong());
    }

    @Test
    public void testGetFileNodePage_ownDirectoryRecycledAncestor() {
        // mock 数据：本人目录的祖先已回收时，不得直接数据库分页暴露子文件
        OaFileNodeDO ancestor = randomFileNodeDO(1L, 0L, 1);
        ancestor.setCreator("10");
        fileNodeMapper.insert(ancestor);
        OaFileNodeDO folder = randomFileNodeDO(2L, 1L, 0);
        folder.setCreator("10");
        fileNodeMapper.insert(folder);
        OaFileNodeDO file = randomFileNodeDO(3L, 2L, 0).setType(1);
        file.setCreator("10");
        fileNodeMapper.insert(file);

        // 调用，并断言异常
        assertServiceException(() -> fileNodeService.getFileNodePage(
                new OaFileNodePageReqVO().setScope("my").setParentId(2L), 10L), FILE_NODE_NOT_AVAILABLE);
        verifyNoInteractions(fileFavoriteService);
    }

    @Test
    public void testGetFileNodePage_sharedDirectoryFiltersBeforePaging() {
        // mock 数据：共享目录不继承时，仅对子文件的单独授权生效
        OaFileNodeDO folder = randomFileNodeDO(1L, 0L, 0);
        folder.setCreator("20");
        fileNodeMapper.insert(folder);
        for (long id = 2; id <= 4; id++) {
            OaFileNodeDO file = randomFileNodeDO(id, 1L, 0).setType(1);
            file.setCreator("20");
            file.setCreateTime(LocalDateTime.of(2026, 9, 10, 12, 0));
            fileNodeMapper.insert(file);
        }
        // mock 方法：编号最大的文件没有权限，不应占据第一页
        when(filePermissionService.getFilePermissionListByNodeIds(anyCollection())).thenReturn(Arrays.asList(
                new OaFilePermissionDO().setNodeId(1L).setSubjectType(1).setSubjectId(10L).setLevel(1).setInherit(false),
                new OaFilePermissionDO().setNodeId(2L).setSubjectType(1).setSubjectId(10L).setLevel(2).setInherit(false),
                new OaFilePermissionDO().setNodeId(3L).setSubjectType(1).setSubjectId(10L).setLevel(1).setInherit(false)));
        // 准备参数
        OaFileNodePageReqVO reqVO = new OaFileNodePageReqVO().setScope("shared").setParentId(1L);
        reqVO.setPageNo(2);
        reqVO.setPageSize(1);

        // 调用
        PageResult<OaFileNodeRespVO> result = fileNodeService.getFileNodePage(reqVO, 10L);
        // 断言
        assertEquals(2L, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals(2L, CollUtil.getFirst(result.getList()).getId());
        assertEquals(2, CollUtil.getFirst(result.getList()).getLevel());
        verify(fileFavoriteService).getFileFavoriteListByUserIdAndNodeIds(10L, Collections.singleton(2L));
    }

    @Test
    public void testGetFileNodePage_favoritePage() {
        // mock 数据：收藏入口仍用完整收藏编号筛选，只为当前页拼接响应
        for (long id = 1; id <= 3; id++) {
            OaFileNodeDO file = randomFileNodeDO(id, 0L, 0).setType(1);
            file.setCreator("10");
            file.setCreateTime(LocalDateTime.of(2026, 9, 10, 12, 0));
            fileNodeMapper.insert(file);
        }
        // mock 方法
        when(fileFavoriteService.getFileFavoriteList(10L)).thenReturn(Arrays.asList(
                new OaFileFavoriteDO().setNodeId(1L).setUserId(10L),
                new OaFileFavoriteDO().setNodeId(2L).setUserId(10L)));
        when(fileFavoriteService.getFileFavoriteListByUserIdAndNodeIds(10L, Collections.singleton(1L)))
                .thenReturn(Collections.singletonList(new OaFileFavoriteDO().setNodeId(1L).setUserId(10L)));
        // 准备参数
        OaFileNodePageReqVO reqVO = new OaFileNodePageReqVO().setScope("favorite");
        reqVO.setPageNo(2);
        reqVO.setPageSize(1);

        // 调用
        PageResult<OaFileNodeRespVO> result = fileNodeService.getFileNodePage(reqVO, 10L);
        // 断言
        assertEquals(2L, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals(1L, CollUtil.getFirst(result.getList()).getId());
        assertTrue(CollUtil.getFirst(result.getList()).getFavorite());
        verify(fileFavoriteService).getFileFavoriteList(10L);
        verify(fileFavoriteService).getFileFavoriteListByUserIdAndNodeIds(10L, Collections.singleton(1L));
    }

    @Test
    public void testGetFileStorage_recycledAndDeletedFiles() {
        // mock 数据：正常子文件被回收目录遮蔽，仍占容量；逻辑删除的文件不占容量
        OaFileNodeDO folder = randomFileNodeDO(1L, 0L, 1);
        folder.setCreator("10");
        fileNodeMapper.insert(folder);
        OaFileNodeDO hidden = randomFileNodeDO(2L, 1L, 0).setType(1).setSize(100L);
        hidden.setCreator("10");
        fileNodeMapper.insert(hidden);
        OaFileNodeDO recycled = randomFileNodeDO(3L, 0L, 1).setType(1).setSize(200L);
        recycled.setCreator("10");
        fileNodeMapper.insert(recycled);
        OaFileNodeDO deleted = randomFileNodeDO(4L, 0L, 0).setType(1).setSize(400L);
        deleted.setCreator("10");
        fileNodeMapper.insert(deleted);
        fileNodeMapper.deleteById(deleted.getId());

        // 调用
        OaFileStorageRespVO result = fileNodeService.getFileStorage(10L);
        // 断言
        assertEquals(300L, result.getUsedSize());
        assertEquals(0L, result.getFileCount());
        verify(filePermissionService).getFilePermissionListByNodeIds(
                argThat(ids -> ids.size() == 2 && ids.containsAll(Arrays.asList(1L, 2L))));
    }

    @Test
    public void testGetFileStorage_onlyRelatedNodes() {
        // mock 数据：统计不能加载其他用户未共享的文件
        OaFileNodeDO own = randomFileNodeDO(1L, 0L, 0).setType(1).setSize(100L);
        own.setCreator("10");
        fileNodeMapper.insert(own);
        OaFileNodeDO unrelated = randomFileNodeDO(2L, 0L, 0).setType(1).setSize(200L);
        unrelated.setCreator("20");
        fileNodeMapper.insert(unrelated);

        // 调用
        OaFileStorageRespVO result = fileNodeService.getFileStorage(10L);
        // 断言
        assertEquals(100L, result.getUsedSize());
        assertEquals(1L, result.getFileCount());
        verify(filePermissionService).getFilePermissionListByNodeIds(
                argThat(ids -> ids.size() == 1 && ids.contains(1L)));
    }

    @Test
    public void testGetFileNodePage_directSharingWithoutInheritance() {
        // mock 数据：直接共享目录不继承时，不加载其子文件
        OaFileNodeDO folder = randomFileNodeDO(1L, 0L, 0);
        folder.setCreator("20");
        fileNodeMapper.insert(folder);
        OaFileNodeDO child = randomFileNodeDO(2L, 1L, 0).setType(1);
        child.setCreator("20");
        fileNodeMapper.insert(child);
        OaFilePermissionDO permission = new OaFilePermissionDO().setNodeId(1L).setSubjectType(1)
                .setSubjectId(10L).setLevel(2).setInherit(false);
        // mock 方法
        when(filePermissionService.getValidFilePermissionList(10L, null)).thenReturn(Collections.singletonList(permission));
        when(filePermissionService.getFilePermissionListByNodeIds(anyCollection())).thenReturn(Collections.singletonList(permission));

        // 调用
        PageResult<OaFileNodeRespVO> result = fileNodeService.getFileNodePage(
                new OaFileNodePageReqVO().setScope("shared"), 10L);
        // 断言
        assertEquals(1L, result.getTotal());
        assertEquals(1L, CollUtil.getFirst(result.getList()).getId());
        verify(filePermissionService).getFilePermissionListByNodeIds(
                argThat(ids -> ids.size() == 1 && ids.contains(1L)));
    }

    @Test
    public void testGetFileNodePage_ownerPermissionInherited() {
        // mock 数据：本人目录中由其他人创建的子目录、文件，仍保留管理权限
        OaFileNodeDO folder = randomFileNodeDO(1L, 0L, 0);
        folder.setCreator("10");
        fileNodeMapper.insert(folder);
        OaFileNodeDO subfolder = randomFileNodeDO(2L, 1L, 0);
        subfolder.setCreator("20");
        fileNodeMapper.insert(subfolder);
        OaFileNodeDO child = randomFileNodeDO(3L, 2L, 0).setType(1).setName("方案.pdf");
        child.setCreator("20");
        fileNodeMapper.insert(child);
        OaFileNodeDO unrelated = randomFileNodeDO(4L, 0L, 0).setType(1).setName("其他方案.pdf");
        unrelated.setCreator("30");
        fileNodeMapper.insert(unrelated);

        // 调用
        PageResult<OaFileNodeRespVO> result = fileNodeService.getFileNodePage(
                new OaFileNodePageReqVO().setScope("shared").setName("方案"), 10L);
        // 断言
        assertEquals(1L, result.getTotal());
        assertEquals(3L, CollUtil.getFirst(result.getList()).getId());
        assertEquals(4, CollUtil.getFirst(result.getList()).getLevel());
        verify(filePermissionService).getFilePermissionListByNodeIds(
                argThat(ids -> ids.size() == 3 && !ids.contains(4L)));
    }

    @Test
    public void testGetFileDirectoryList_onlyAvailableFolders() {
        // mock 数据：只返回本人有效目录，文件、其他用户目录及回收子目录不加入结果
        OaFileNodeDO folder = randomFileNodeDO(1L, 0L, 0);
        folder.setCreator("10");
        fileNodeMapper.insert(folder);
        OaFileNodeDO file = randomFileNodeDO(2L, 0L, 0).setType(1);
        file.setCreator("10");
        fileNodeMapper.insert(file);
        OaFileNodeDO recycled = randomFileNodeDO(3L, 0L, 1);
        recycled.setCreator("10");
        fileNodeMapper.insert(recycled);
        OaFileNodeDO hidden = randomFileNodeDO(4L, 3L, 0);
        hidden.setCreator("10");
        fileNodeMapper.insert(hidden);

        // 调用
        List<OaFileNodeRespVO> result = fileNodeService.getFileDirectoryList(10L);
        // 断言
        assertEquals(1, result.size());
        assertEquals(1L, CollUtil.getFirst(result).getId());
    }

    // ========== 随机对象 ==========

    /**
     * 构造大小为零的目录节点。
     *
     * @param id 节点编号
     * @param parentId 父节点编号
     * @param status 业务状态
     * @return 未入库的测试对象
     */
    private static OaFileNodeDO randomFileNodeDO(Long id, Long parentId, Integer status) {
        return randomPojo(OaFileNodeDO.class, node -> {
            node.setId(id);
            node.setParentId(parentId);
            node.setType(0);
            node.setCategory(null);
            node.setStatus(status);
            node.setSize(0L);
        });
    }

}
