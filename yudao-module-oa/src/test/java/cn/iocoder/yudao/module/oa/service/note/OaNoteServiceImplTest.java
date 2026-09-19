package cn.iocoder.yudao.module.oa.service.note;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.oa.controller.admin.note.vo.OaNotePageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.note.vo.OaNoteSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.note.OaNoteDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.note.OaNoteReceiverDO;
import cn.iocoder.yudao.module.oa.dal.mysql.note.OaNoteMapper;
import cn.iocoder.yudao.module.oa.dal.mysql.note.OaNoteReceiverMapper;
import cn.iocoder.yudao.module.oa.enums.note.OaNoteTypeEnum;
import cn.iocoder.yudao.module.oa.enums.schedule.OaPriorityEnum;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.NOTE_ACCESS_DENIED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link OaNoteServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(OaNoteServiceImpl.class)
public class OaNoteServiceImplTest extends BaseDbUnitTest {

    @Resource
    private DataSource dataSource;

    @Resource
    private OaNoteServiceImpl noteService;

    @Resource
    private OaNoteMapper noteMapper;
    @Resource
    private OaNoteReceiverMapper noteReceiverMapper;

    @MockBean
    private OaNoteCategoryService noteCategoryService;
    @MockBean
    private AdminUserApi adminUserApi;

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testCreateNote_withoutCategory() {
        // 准备参数：首页快捷笔记不传目录
        Long userId = randomLongId();
        OaNoteSaveReqVO reqVO = randomNoteSaveReqVO().setCategoryId(null);
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(userId), new MockHttpServletRequest());

        // 调用
        Long id = noteService.createNote(reqVO, userId);
        // 断言
        assertNull(noteMapper.selectById(id).getCategoryId());
        assertEquals(reqVO.getContent(), noteService.getNote(id, userId).getContent());
    }

    @Test
    public void testCreateNote_ownerHoldsNote() {
        // 准备参数
        Long userId = randomLongId();
        OaNoteSaveReqVO reqVO = randomNoteSaveReqVO();

        // 调用
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(userId), new MockHttpServletRequest());
        Long noteId = noteService.createNote(reqVO, userId);

        // 断言
        OaNoteDO note = noteMapper.selectById(noteId);
        assertEquals(userId.toString(), note.getCreator());
        assertEquals(OaNoteTypeEnum.SHARED.getType(), note.getType());
        assertEquals(reqVO.getCategoryId(), note.getCategoryId());
        assertEquals(reqVO.getContent(), note.getContent());
        assertEquals(1, noteReceiverMapper.selectListByNoteId(noteId).size());
        assertEquals(userId, CollUtil.getFirst(noteReceiverMapper.selectListByNoteId(noteId)).getUserId());
        assertEquals(noteId, noteService.getNote(noteId, userId).getId());
        assertEquals(1L, noteService.getMyNotePage(new OaNotePageReqVO(), userId).getTotal());
        assertEquals(0L, noteService.getReceivedNotePage(new OaNotePageReqVO(), userId).getTotal());
    }

    @Test
    public void testGetNote_sharedReceiver() {
        // mock 数据
        Long receiverUserId = randomLongId();
        OaNoteDO note = randomNoteDO(randomLongId());
        noteMapper.insert(note);
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(note.getId())
                .setUserId(Long.valueOf(note.getCreator())));
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(note.getId()).setUserId(receiverUserId));

        // 调用
        OaNoteDO result = noteService.getNote(note.getId(), receiverUserId);

        // 断言
        assertEquals(note.getId(), result.getId());
    }

    @Test
    public void testUpdateNote_clearFileUrls() {
        // mock 数据
        Long userId = randomLongId();
        OaNoteDO note = randomNoteDO(userId).setCategoryId(randomLongId()).setContent("旧内容")
                .setFileUrls(Collections.singletonList("https://example.com/a.txt"));
        noteMapper.insert(note);
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(note.getId())
                .setUserId(Long.valueOf(note.getCreator())));
        // 准备参数
        OaNoteSaveReqVO reqVO = randomNoteSaveReqVO().setId(note.getId())
                .setCategoryId(randomLongId()).setContent("更新后的内容").setFileUrls(null);

        // 调用
        noteService.updateNote(reqVO, userId);

        // 断言
        OaNoteDO updatedNote = noteMapper.selectById(note.getId());
        assertEquals(reqVO.getCategoryId(), updatedNote.getCategoryId());
        assertEquals(reqVO.getContent(), updatedNote.getContent());
        assertNull(updatedNote.getFileUrls());
    }

    @Test
    public void testUpdateNoteShare_repeatedReceiver() {
        // mock 数据
        Long creatorUserId = randomLongId();
        Long receiverUserId = randomLongId();
        OaNoteDO note = randomNoteDO(creatorUserId);
        noteMapper.insert(note);
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(note.getId())
                .setUserId(Long.valueOf(note.getCreator())));
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(note.getId()).setUserId(receiverUserId));
        // 准备参数

        // 调用
        noteService.updateNoteShare(note.getId(), Collections.singletonList(receiverUserId), creatorUserId);
        noteService.updateNoteShare(note.getId(), Collections.singletonList(receiverUserId), creatorUserId);

        // 断言
        assertEquals(2, noteReceiverMapper.selectListByNoteIds(Collections.singleton(note.getId())).size());
    }

    @Test
    public void testUpdateNoteShare_preserveAndRemoveReceivers() {
        // mock 数据
        Long userId = randomLongId();
        OaNoteDO note = randomNoteDO(userId);
        noteMapper.insert(note);
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(note.getId())
                .setUserId(Long.valueOf(note.getCreator())));
        OaNoteReceiverDO receiver = new OaNoteReceiverDO().setNoteId(note.getId()).setUserId(2L);
        noteReceiverMapper.insert(receiver);

        // 调用：新增接收人，不重建已有关系
        noteService.updateNoteShare(note.getId(), Arrays.asList(2L, 3L, 3L), userId);

        // 断言
        assertEquals(3, noteReceiverMapper.selectListByNoteId(note.getId()).size());
        assertEquals(receiver.getId(), noteReceiverMapper.selectByNoteIdAndUserId(note.getId(), 2L).getId());

        // 调用：反复取消、添加同一接收人
        for (int i = 0; i < 3; i++) {
            noteService.updateNoteShare(note.getId(), Collections.emptyList(), userId);
            noteService.updateNoteShare(note.getId(), Collections.singletonList(2L), userId);
        }

        // 断言：历史关系仍保留，默认 Map 只返回未删除关系
        assertTrue(new JdbcTemplate(dataSource).queryForObject("SELECT COUNT(*) FROM oa_note_receiver WHERE deleted = TRUE", Long.class) > 0);
        assertEquals(new HashSet<>(Arrays.asList(userId, 2L)), new HashSet<>(noteService.getNoteReceiverUserIdListMap(
                Collections.singleton(note.getId())).get(note.getId())));
        assertTrue(CollUtil.isEmpty(noteService.getNoteReceiverUserIdListMap(Collections.emptyList())));
    }

    @Test
    public void testDeleteReceivedNote_onlyCurrentReceiver() {
        // mock 数据
        Long userId = randomLongId();
        Long otherUserId = randomLongId();
        OaNoteDO note = randomNoteDO(randomLongId());
        noteMapper.insert(note);
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(note.getId())
                .setUserId(Long.valueOf(note.getCreator())));
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(note.getId()).setUserId(userId));
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(note.getId()).setUserId(otherUserId));

        // 调用
        noteService.deleteReceivedNote(note.getId(), userId);

        // 断言：仅移除本人关系，正文和其他接收人的访问保留
        assertNull(noteReceiverMapper.selectByNoteIdAndUserId(note.getId(), userId));
        assertEquals(note.getId(), noteService.getNote(note.getId(), otherUserId).getId());
        assertServiceException(() -> noteService.getNote(note.getId(), userId), NOTE_ACCESS_DENIED);
        assertServiceException(() -> noteService.deleteReceivedNote(note.getId(), userId), NOTE_ACCESS_DENIED);
    }

    @Test
    public void testDeleteNote_sharedOwnerExits() {
        // mock 数据
        Long ownerId = randomLongId();
        Long receiverId = randomLongId();
        OaNoteDO note = randomNoteDO(ownerId).setCategoryId(randomLongId());
        noteMapper.insert(note);
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(note.getId())
                .setUserId(Long.valueOf(note.getCreator())));
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(note.getId()).setUserId(receiverId));

        // 调用
        noteService.deleteNote(note.getId(), ownerId);

        // 断言：作者退出所有入口，其他持有人及正文不受影响
        assertNull(noteReceiverMapper.selectByNoteIdAndUserId(note.getId(), ownerId));
        assertEquals(ownerId.toString(), noteMapper.selectById(note.getId()).getCreator());
        assertServiceException(() -> noteService.updateNote(randomNoteSaveReqVO()
                .setId(note.getId()), ownerId), NOTE_ACCESS_DENIED);
        assertEquals(0L, noteService.getMyNotePage(new OaNotePageReqVO(), ownerId).getTotal());
        assertServiceException(() -> noteService.getNote(note.getId(), ownerId), NOTE_ACCESS_DENIED);
        assertServiceException(() -> noteService.updateNoteFavorite(note.getId(), true, ownerId), NOTE_ACCESS_DENIED);
        assertServiceException(() -> noteService.updateNoteShare(note.getId(), Collections.emptyList(), ownerId),
                NOTE_ACCESS_DENIED);
        assertServiceException(() -> noteService.deleteNote(note.getId(), ownerId), NOTE_ACCESS_DENIED);
        assertEquals(note.getId(), noteService.getNote(note.getId(), receiverId).getId());
        noteService.deleteNotesByCategoryId(note.getCategoryId(), ownerId);
        assertEquals(note.getId(), noteService.getNote(note.getId(), receiverId).getId());
    }

    @Test
    public void testUpdateNoteFavorite_receiversShareState() {
        // mock 数据
        Long ownerId = randomLongId();
        Long receiverId = randomLongId();
        Long otherReceiverId = randomLongId();
        OaNoteDO note = randomNoteDO(ownerId);
        noteMapper.insert(note);
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(note.getId())
                .setUserId(Long.valueOf(note.getCreator())));
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(note.getId()).setUserId(receiverId));
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(note.getId()).setUserId(otherReceiverId));

        // 调用
        noteService.updateNoteFavorite(note.getId(), true, receiverId);

        // 断言：源产品的主表收藏状态对所有持有人一致，外人不能修改
        assertTrue(noteService.getNote(note.getId(), ownerId).getFavorite());
        assertTrue(noteService.getNote(note.getId(), otherReceiverId).getFavorite());
        assertEquals(1L, noteService.getReceivedNotePage(new OaNotePageReqVO().setFavorite(true), receiverId).getTotal());
        assertServiceException(() -> noteService.updateNoteFavorite(note.getId(), false, randomLongId()), NOTE_ACCESS_DENIED);
        noteService.deleteReceivedNote(note.getId(), receiverId);
        assertServiceException(() -> noteService.updateNoteFavorite(note.getId(), false, receiverId), NOTE_ACCESS_DENIED);
    }

    @Test
    public void testDeleteNote_privateDeletesBody() {
        // mock 数据
        Long ownerId = randomLongId();
        OaNoteDO note = randomNoteDO(ownerId);
        noteMapper.insert(note);
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(note.getId())
                .setUserId(Long.valueOf(note.getCreator())));

        // 调用
        noteService.deleteNote(note.getId(), ownerId);

        // 断言
        assertNull(noteMapper.selectById(note.getId()));
    }

    @Test
    public void testDeleteNote_notOwner() {
        // mock 数据
        OaNoteDO note = randomNoteDO(randomLongId());
        noteMapper.insert(note);
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(note.getId())
                .setUserId(Long.valueOf(note.getCreator())));

        // 调用，并断言异常
        assertServiceException(() -> noteService.deleteNote(note.getId(), randomLongId()), NOTE_ACCESS_DENIED);
    }

    @Test
    public void testUpdateNoteFavorite_keepOtherFields() {
        // mock 数据
        Long userId = randomLongId();
        OaNoteDO note = randomNoteDO(userId).setCategoryId(randomLongId()).setContent("原内容")
                .setFileUrls(Collections.singletonList("https://example.com/a.txt"));
        noteMapper.insert(note);
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(note.getId())
                .setUserId(Long.valueOf(note.getCreator())));

        // 调用
        noteService.updateNoteFavorite(note.getId(), true, userId);

        // 断言
        OaNoteDO updatedNote = noteMapper.selectById(note.getId());
        assertTrue(updatedNote.getFavorite());
        assertEquals(note.getCategoryId(), updatedNote.getCategoryId());
        assertEquals(note.getContent(), updatedNote.getContent());
        assertEquals(note.getFileUrls(), updatedNote.getFileUrls());
    }

    @Test
    public void testUpdateNoteShare_duplicateReceivers() {
        // 准备参数
        Long userId = randomLongId();
        Long receiverUserId = randomLongId();
        OaNoteDO note = randomNoteDO(userId);
        noteMapper.insert(note);
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(note.getId())
                .setUserId(Long.valueOf(note.getCreator())));

        // 调用
        noteService.updateNoteShare(note.getId(), Arrays.asList(userId, receiverUserId, userId, receiverUserId), userId);

        // 断言
        assertEquals(2, noteReceiverMapper.selectListByNoteIds(Collections.singleton(note.getId())).size());
    }

    @Test
    public void testGetNote_notReceiver() {
        // mock 数据
        OaNoteDO note = randomNoteDO(randomLongId());
        noteMapper.insert(note);
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(note.getId())
                .setUserId(Long.valueOf(note.getCreator())));

        // 调用，并断言异常
        assertServiceException(() -> noteService.getNote(note.getId(), randomLongId()), NOTE_ACCESS_DENIED);
    }

    @Test
    public void testGetNotePage_mine() {
        // mock 数据
        Long userId = randomLongId();
        OaNoteDO mine = randomNoteDO(userId);
        noteMapper.insert(mine);
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(mine.getId()).setUserId(userId));
        OaNoteDO shared = randomNoteDO(randomLongId());
        noteMapper.insert(shared);
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(shared.getId()).setUserId(userId));
        // 准备参数
        OaNotePageReqVO reqVO = new OaNotePageReqVO();

        // 调用
        PageResult<OaNoteDO> page = noteService.getMyNotePage(reqVO, userId);

        // 断言
        assertEquals(1L, page.getTotal());
        assertEquals(mine.getId(), CollUtil.getFirst(page.getList()).getId());
    }

    @Test
    public void testGetReceivedNotePage_onlyReceiverAndFilters() {
        // mock 数据
        Long userId = randomLongId();
        OaNoteDO received = randomNoteDO(randomLongId()).setTitle("共享会议");
        noteMapper.insert(received);
        noteMapper.insert(randomNoteDO(randomLongId()));
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(received.getId()).setUserId(userId));
        // 准备参数
        OaNotePageReqVO reqVO = new OaNotePageReqVO();

        // 调用，并断言
        assertEquals(1L, noteService.getReceivedNotePage(reqVO, userId).getTotal());
        assertEquals(received.getId(), CollUtil.getFirst(noteService.getReceivedNotePage(reqVO, userId).getList()).getId());
        reqVO.setTitle("不存在");
        assertEquals(0L, noteService.getReceivedNotePage(reqVO, userId).getTotal());
    }

    @Test
    public void testGetNotePage_sharedEmpty() {
        // mock 数据
        Long userId = randomLongId();
        OaNoteDO mine = randomNoteDO(userId);
        noteMapper.insert(mine);
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(mine.getId()).setUserId(userId));
        // 准备参数
        OaNotePageReqVO reqVO = new OaNotePageReqVO();

        // 调用
        PageResult<OaNoteDO> page = noteService.getReceivedNotePage(reqVO, userId);

        // 断言
        assertEquals(0L, page.getTotal());
        assertTrue(CollUtil.isEmpty(page.getList()));
    }

    @Test
    public void testUpdateNoteShare_clearAndKeepContent() {
        // mock 数据
        Long userId = randomLongId();
        OaNoteDO note = randomNoteDO(userId).setCategoryId(randomLongId())
                .setFileUrls(Collections.singletonList("https://example.com/note.txt"));
        noteMapper.insert(note);
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(note.getId())
                .setUserId(Long.valueOf(note.getCreator())));
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(note.getId()).setUserId(randomLongId()));

        // 调用
        noteService.updateNoteShare(note.getId(), Collections.emptyList(), userId);

        // 断言
        assertEquals(1, noteReceiverMapper.selectListByNoteId(note.getId()).size());
        assertEquals(userId, CollUtil.getFirst(noteReceiverMapper.selectListByNoteId(note.getId())).getUserId());
        assertEquals(note.getId(), noteService.getNote(note.getId(), userId).getId());
        OaNoteDO updated = noteMapper.selectById(note.getId());
        assertEquals(note.getContent(), updated.getContent());
        assertEquals(note.getCategoryId(), updated.getCategoryId());
        assertEquals(note.getFileUrls(), updated.getFileUrls());
    }

    @Test
    public void testUpdateNoteShare_notOwner() {
        // mock 数据
        OaNoteDO note = randomNoteDO(randomLongId());
        noteMapper.insert(note);
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(note.getId())
                .setUserId(Long.valueOf(note.getCreator())));

        // 调用，并断言异常
        assertServiceException(() -> noteService.updateNoteShare(note.getId(), Collections.emptyList(), randomLongId()),
                NOTE_ACCESS_DENIED);
    }

    @Test
    public void testUpdateNote_keepReceivers() {
        // mock 数据
        Long userId = randomLongId();
        Long receiverId = randomLongId();
        OaNoteDO note = randomNoteDO(userId);
        noteMapper.insert(note);
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(note.getId())
                .setUserId(Long.valueOf(note.getCreator())));
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(note.getId()).setUserId(receiverId));
        // 准备参数，内容编辑不应覆盖共享关系
        OaNoteSaveReqVO reqVO = randomNoteSaveReqVO().setId(note.getId());

        // 调用
        noteService.updateNote(reqVO, userId);

        // 断言
        assertEquals(2, noteReceiverMapper.selectListByNoteId(note.getId()).size());
        assertEquals(receiverId, noteReceiverMapper.selectByNoteIdAndUserId(note.getId(), receiverId).getUserId());
    }

    @Test
    public void testDeleteNote_sharedWithoutOtherHolders() {
        // mock 数据
        Long userId = randomLongId();
        OaNoteDO note = randomNoteDO(userId).setType(OaNoteTypeEnum.SHARED.getType());
        noteMapper.insert(note);
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(note.getId()).setUserId(userId));

        // 调用
        noteService.deleteNote(note.getId(), userId);

        // 断言：共享类型只退出关系，即使没有其他持有人也保留正文
        assertEquals(note.getId(), noteMapper.selectById(note.getId()).getId());
        assertTrue(CollUtil.isEmpty(noteReceiverMapper.selectListByNoteId(note.getId())));
        assertServiceException(() -> noteService.getNote(note.getId(), userId), NOTE_ACCESS_DENIED);
    }

    @Test
    public void testDeleteReceivedNote_ownerMustUseDeleteEntry() {
        // mock 数据
        Long userId = randomLongId();
        OaNoteDO note = randomNoteDO(userId);
        noteMapper.insert(note);
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(note.getId()).setUserId(userId));

        // 调用，并断言：不能通过接收人入口绕过创建人的删除入口
        assertServiceException(() -> noteService.deleteReceivedNote(note.getId(), userId), NOTE_ACCESS_DENIED);
        assertEquals(note.getId(), noteService.getNote(note.getId(), userId).getId());
    }

    @Test
    public void testDeleteNotesByCategoryId_onlyHeldNotes() {
        // mock 数据
        Long userId = randomLongId();
        Long categoryId = randomLongId();
        OaNoteDO held = randomNoteDO(userId).setCategoryId(categoryId);
        noteMapper.insert(held);
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(held.getId()).setUserId(userId));
        OaNoteDO exited = randomNoteDO(userId).setCategoryId(categoryId);
        noteMapper.insert(exited);
        Long receiverId = randomLongId();
        noteReceiverMapper.insert(new OaNoteReceiverDO().setNoteId(exited.getId()).setUserId(receiverId));

        // 调用
        noteService.deleteNotesByCategoryId(categoryId, userId);

        // 断言：仅清理仍持有的目录笔记，已经退出的正文及其他人的关系保留
        assertNull(noteMapper.selectById(held.getId()));
        assertTrue(CollUtil.isEmpty(noteReceiverMapper.selectListByNoteId(held.getId())));
        assertEquals(exited.getId(), noteService.getNote(exited.getId(), receiverId).getId());
        assertServiceException(() -> noteService.getNote(exited.getId(), userId), NOTE_ACCESS_DENIED);
    }

    // ========== 随机对象 ==========

    /**
     * 构造普通优先级的共享笔记保存参数。
     *
     * @return 保存参数
     */
    private static OaNoteSaveReqVO randomNoteSaveReqVO() {
        return randomPojo(OaNoteSaveReqVO.class, o -> o.setId(null).setCategoryId(randomLongId())
                .setType(OaNoteTypeEnum.SHARED.getType()).setPriority(OaPriorityEnum.NORMAL.getPriority())
                .setTitle("会议纪要").setContent("会议结论").setFileUrls(null));
    }

    /**
     * 构造未收藏的私人笔记。
     *
     * @param creatorUserId 创建人编号
     * @return 未入库的测试对象
     */
    private static OaNoteDO randomNoteDO(Long creatorUserId) {
        OaNoteDO note = randomPojo(OaNoteDO.class, o -> o.setId(null)
                .setCategoryId(null).setType(OaNoteTypeEnum.PRIVATE.getType())
                .setPriority(OaPriorityEnum.NORMAL.getPriority()).setTitle("个人笔记")
                .setContent("笔记内容").setFileUrls(null).setFavorite(false));
        note.setCreator(creatorUserId.toString());
        return note;
    }

}
