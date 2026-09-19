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
