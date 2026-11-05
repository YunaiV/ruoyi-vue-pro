package cn.iocoder.yudao.module.oa.service.note;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.oa.controller.admin.note.vo.category.OaNoteCategorySaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.note.OaNoteCategoryDO;
import cn.iocoder.yudao.module.oa.dal.mysql.note.OaNoteCategoryMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.NOTE_CATEGORY_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.NOTE_CATEGORY_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * {@link OaNoteCategoryServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(OaNoteCategoryServiceImpl.class)
public class OaNoteCategoryServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaNoteCategoryServiceImpl noteCategoryService;

    @Resource
    private OaNoteCategoryMapper noteCategoryMapper;

    @MockitoBean
    private OaNoteService noteService;

    @Test
    public void testCreateNoteCategory_duplicateName() {
        // mock 数据
        OaNoteCategoryDO category = randomNoteCategoryDO();
        noteCategoryMapper.insert(category);
        // 准备参数
        OaNoteCategorySaveReqVO reqVO = new OaNoteCategorySaveReqVO().setName(category.getName()).setSort(0);

        // 调用，并断言异常
        assertServiceException(() -> noteCategoryService.createNoteCategory(reqVO, category.getUserId()),
                NOTE_CATEGORY_NAME_DUPLICATE);
    }

    @Test
    public void testCreateNoteCategory_ignoreReqId() {
        // mock 数据：其他用户的目录
        OaNoteCategoryDO otherCategory = randomNoteCategoryDO().setUserId(1L);
        noteCategoryMapper.insert(otherCategory);
        // 准备参数：请求体携带其他用户目录的编号
        Long userId = 2L;
        OaNoteCategorySaveReqVO reqVO = new OaNoteCategorySaveReqVO().setId(otherCategory.getId())
                .setName("新建目录").setSort(3);

        // 调用
        Long categoryId = noteCategoryService.createNoteCategory(reqVO, userId);

        // 断言：忽略请求编号，由数据库生成新编号，归属当前用户
        assertNotEquals(otherCategory.getId(), categoryId);
        OaNoteCategoryDO category = noteCategoryMapper.selectById(categoryId);
        assertEquals(userId, category.getUserId());
        assertEquals(reqVO.getName(), category.getName());
        assertEquals(reqVO.getSort(), category.getSort());
        // 断言：其他用户的目录不变
        OaNoteCategoryDO dbOtherCategory = noteCategoryMapper.selectById(otherCategory.getId());
        assertEquals(1L, dbOtherCategory.getUserId());
        assertEquals(otherCategory.getName(), dbOtherCategory.getName());
        assertEquals(0, dbOtherCategory.getSort());
    }

    @Test
    public void testUpdateNoteCategory_keepOwner() {
        // mock 数据
        OaNoteCategoryDO category = randomNoteCategoryDO();
        noteCategoryMapper.insert(category);
        // 准备参数
        OaNoteCategorySaveReqVO reqVO = new OaNoteCategorySaveReqVO().setId(category.getId())
                .setName("更新目录").setSort(2);

        // 调用
        noteCategoryService.updateNoteCategory(reqVO, category.getUserId());

        // 断言
        OaNoteCategoryDO updatedCategory = noteCategoryMapper.selectById(category.getId());
        assertEquals(category.getUserId(), updatedCategory.getUserId());
        assertEquals(reqVO.getName(), updatedCategory.getName());
        assertEquals(reqVO.getSort(), updatedCategory.getSort());
    }

    @Test
    public void testDeleteNoteCategory_success() {
        // mock 数据
        OaNoteCategoryDO category = randomNoteCategoryDO();
        noteCategoryMapper.insert(category);

        // 调用
        noteCategoryService.deleteNoteCategory(category.getId(), category.getUserId());

        // 断言
        assertNull(noteCategoryMapper.selectById(category.getId()));
        verify(noteService).deleteNotesByCategoryId(category.getId(), category.getUserId());
    }

    @Test
    public void testDeleteNoteCategory_notOwner() {
        // mock 数据
        OaNoteCategoryDO category = randomNoteCategoryDO();
        noteCategoryMapper.insert(category);

        // 调用，并断言异常
        assertServiceException(() -> noteCategoryService.deleteNoteCategory(category.getId(), randomLongId()),
                NOTE_CATEGORY_NOT_EXISTS);
        verifyNoInteractions(noteService);
    }

    // ========== 随机对象 ==========

    /**
     * 构造排序值为零的笔记分类。
     *
     * @return 未入库的测试对象
     */
    private static OaNoteCategoryDO randomNoteCategoryDO() {
        return randomPojo(OaNoteCategoryDO.class, o -> o.setId(null).setSort(0));
    }

}
