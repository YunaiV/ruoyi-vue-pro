package cn.iocoder.yudao.module.pms.service.kb.library;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.library.PmsKnowledgeLibraryPageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.library.vo.library.PmsKnowledgeLibrarySaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryDO;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.library.PmsKnowledgeLibraryMapper;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeDocumentStatusEnum;
import cn.iocoder.yudao.module.pms.service.kb.recycle.PmsKnowledgeRecycleService;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link PmsKnowledgeLibraryServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(PmsKnowledgeLibraryServiceImpl.class)
public class PmsKnowledgeLibraryServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsKnowledgeLibraryServiceImpl libraryService;

    @Resource
    private PmsKnowledgeLibraryMapper libraryMapper;

    @MockitoBean
    private PmsKnowledgeLibraryMemberService memberService;
    @MockitoBean
    private PmsKnowledgeGroupService knowledgeGroupService;
    @MockitoBean
    private PmsKnowledgeRecycleService recycleService;
    @MockitoBean
    private PmsKnowledgeLibraryTemplateService libraryTemplateService;
    @MockitoBean
    private PermissionApi permissionApi;

    @Test
    public void testCreateLibrary_success() {
        // 准备参数
        Long userId = randomLongId();
        Long memberUserId = randomLongId();
        PmsKnowledgeLibrarySaveReqVO reqVO = randomSaveReqVO()
                .setAdminUserIds(Collections.singletonList(memberUserId));

        // 调用
        Long libraryId = libraryService.createLibrary(reqVO, userId);

        // 断言
        PmsKnowledgeLibraryDO library = libraryMapper.selectById(libraryId);
        assertEquals(reqVO.getName(), library.getName());
        assertNull(library.getCreator()); // 单元测试无登录上下文，验证 Service 未手工写入审计字段
        verify(memberService).createLibraryMemberList(libraryId, userId,
                Collections.singletonList(memberUserId), Collections.emptyList());
    }

    @Test
    public void testCreateLibrary_withTemplate() {
        // 准备参数
        Long userId = randomLongId();
        PmsKnowledgeLibrarySaveReqVO reqVO = randomSaveReqVO().setTemplateId(5L);

        // 调用
        Long libraryId = libraryService.createLibrary(reqVO, userId);

        // 断言
        verify(memberService).createLibraryMemberList(libraryId, userId,
                Collections.emptyList(), Collections.emptyList());
        verify(libraryTemplateService).createTemplateDocumentList(5L, libraryId, userId);
    }

    @Test
    public void testCreateLibrary_publicKeepsInitialMembers() {
        // 准备参数
        Long userId = randomLongId();
        PmsKnowledgeLibrarySaveReqVO reqVO = randomSaveReqVO().setOpenStatus(true)
                .setAdminUserIds(Collections.singletonList(randomLongId()))
                .setMemberUserIds(Collections.singletonList(randomLongId()));

        // 调用
        libraryService.createLibrary(reqVO, userId);

        // 断言：公开状态只影响可见范围，初始成员仍可用于内容协作
        verify(memberService).createLibraryMemberList(org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.eq(userId),
                org.mockito.ArgumentMatchers.eq(reqVO.getAdminUserIds()),
                org.mockito.ArgumentMatchers.eq(reqVO.getMemberUserIds()));
    }

    @Test
    public void testUpdateLibrary_publicKeepsMembers() {
        // mock 数据
        Long libraryId = randomLongId();
        Long userId = randomLongId();
        when(memberService.validateLibraryAdmin(libraryId, userId)).thenReturn(randomLibraryDO(true).setId(libraryId));
        PmsKnowledgeLibrarySaveReqVO reqVO = randomSaveReqVO().setId(libraryId).setOpenStatus(true);

        // 调用
        libraryService.updateLibrary(reqVO, userId);

        // 断言：公开状态更新不清理已有成员
        verify(memberService, org.mockito.Mockito.never()).validateLibraryCreator(libraryId, userId);
    }

    @Test
    public void testUpdateLibrary_privateAdminCanMakePublic() {
        // mock 数据
        Long libraryId = randomLongId();
        Long userId = randomLongId();
        when(memberService.validateLibraryAdmin(libraryId, userId)).thenReturn(randomLibraryDO(false).setId(libraryId));
        PmsKnowledgeLibrarySaveReqVO reqVO = randomSaveReqVO().setId(libraryId).setOpenStatus(true);

        // 调用
        libraryService.updateLibrary(reqVO, userId);

        // 断言：私有库管理员可以将知识库公开
        verify(memberService, org.mockito.Mockito.never()).validateLibraryCreator(libraryId, userId);
    }

    @Test
    public void testGetLibraryPage_accessScope() {
        // mock 数据
        Long userId = randomLongId();
        PmsKnowledgeLibraryDO publicLibrary = randomLibraryDO(true);
        libraryMapper.insert(publicLibrary);
        PmsKnowledgeLibraryDO memberLibrary = randomLibraryDO(false);
        libraryMapper.insert(memberLibrary);
        libraryMapper.insert(randomLibraryDO(false));
        when(memberService.getJoinedLibraryIdList(userId))
                .thenReturn(Collections.singletonList(memberLibrary.getId()));
        // 准备参数
        PmsKnowledgeLibraryPageReqVO reqVO = new PmsKnowledgeLibraryPageReqVO();
        reqVO.setPageNo(1).setPageSize(10);

        // 调用
        PageResult<PmsKnowledgeLibraryDO> pageResult = libraryService.getLibraryPage(reqVO, userId);

        // 断言
        assertEquals(2, pageResult.getTotal());
        assertTrue(pageResult.getList().stream().anyMatch(item -> item.getId().equals(publicLibrary.getId())));
        assertTrue(pageResult.getList().stream().anyMatch(item -> item.getId().equals(memberLibrary.getId())));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetLibraryPage_allGroupUsesReadableCandidates() {
        // mock 数据
        Long userId = randomLongId();
        Long groupId = randomLongId();
        PmsKnowledgeLibraryDO publicLibrary = randomLibraryDO(true);
        libraryMapper.insert(publicLibrary);
        PmsKnowledgeLibraryDO memberLibrary = randomLibraryDO(false);
        libraryMapper.insert(memberLibrary);
        when(memberService.getJoinedLibraryIdList(userId)).thenReturn(Collections.singletonList(memberLibrary.getId()));
        List<Long> readableLibraryIds = Arrays.asList(publicLibrary.getId(), memberLibrary.getId());
        when(memberService.getReadableLibraryIdList(userId)).thenReturn(readableLibraryIds);
        doAnswer(invocation -> new java.util.ArrayList<>((java.util.Collection<Long>) invocation.getArgument(2)))
                .when(knowledgeGroupService).filterLibraryIdListByGroup(groupId, userId, readableLibraryIds);

        // 调用
        PmsKnowledgeLibraryPageReqVO reqVO = new PmsKnowledgeLibraryPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        reqVO.setGroupId(groupId);
        PageResult<PmsKnowledgeLibraryDO> pageResult = libraryService.getLibraryPage(reqVO, userId);

        // 断言：默认“全部”分组的候选集必须包含公开库
        assertEquals(2, pageResult.getTotal());
        assertTrue(pageResult.getList().stream().anyMatch(item -> item.getId().equals(publicLibrary.getId())));
    }

    // ========== 随机对象 ==========

    private PmsKnowledgeLibraryDO randomLibraryDO(boolean openStatus) {
        return randomPojo(PmsKnowledgeLibraryDO.class, library -> library.setId(null).setName("测试知识库")
                .setDescription("测试").setOpenStatus(openStatus).setCoverUrl(null)
                .setStatus(PmsKnowledgeDocumentStatusEnum.NORMAL.getStatus())
                .setDeleteUserId(null).setDeleteTime(null));
    }

    private static PmsKnowledgeLibrarySaveReqVO randomSaveReqVO() {
        return randomPojo(PmsKnowledgeLibrarySaveReqVO.class, reqVO -> reqVO.setId(null)
                .setName("产品知识库").setDescription("产品文档").setOpenStatus(false)
                .setCoverUrl(null).setAdminUserIds(Collections.emptyList())
                .setMemberUserIds(Collections.emptyList()));
    }

}
