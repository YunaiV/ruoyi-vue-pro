package cn.iocoder.yudao.module.oa.service.contact;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.oa.controller.admin.contact.vo.category.OaContactCategorySaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.contact.OaContactCategoryDO;
import cn.iocoder.yudao.module.oa.dal.mysql.contact.OaContactCategoryMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.CONTACT_CATEGORY_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * {@link OaContactCategoryServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(OaContactCategoryServiceImpl.class)
public class OaContactCategoryServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaContactCategoryServiceImpl contactCategoryService;

    @Resource
    private OaContactCategoryMapper contactCategoryMapper;

    @MockBean
    private OaContactService contactService;

    @Test
    public void testCreateContactCategory_ignoreReqId() {
        // mock 数据：其他用户的分类
        OaContactCategoryDO otherCategory = new OaContactCategoryDO().setUserId(1L).setName("客户").setSort(1);
        contactCategoryMapper.insert(otherCategory);
        // 准备参数：请求体携带其他用户分类的编号
        Long userId = 2L;
        OaContactCategorySaveReqVO reqVO = new OaContactCategorySaveReqVO().setId(otherCategory.getId())
                .setName("供应商").setSort(2);

        // 调用
        Long categoryId = contactCategoryService.createContactCategory(reqVO, userId);

        // 断言：忽略请求编号，由数据库生成新编号，归属当前用户
        assertNotEquals(otherCategory.getId(), categoryId);
        OaContactCategoryDO category = contactCategoryMapper.selectById(categoryId);
        assertEquals(userId, category.getUserId());
        assertEquals("供应商", category.getName());
        assertEquals(2, category.getSort());
        // 断言：其他用户的分类不变
        OaContactCategoryDO dbOtherCategory = contactCategoryMapper.selectById(otherCategory.getId());
        assertEquals(1L, dbOtherCategory.getUserId());
        assertEquals("客户", dbOtherCategory.getName());
        assertEquals(1, dbOtherCategory.getSort());
        verifyNoInteractions(contactService);
    }

    @Test
    public void testUpdateContactCategory_noAssignmentUpdate() {
        // mock 数据
        Long userId = randomLongId();
        OaContactCategoryDO category = new OaContactCategoryDO().setUserId(userId).setName("客户").setSort(1);
        contactCategoryMapper.insert(category);
        // 准备参数
        OaContactCategorySaveReqVO reqVO = new OaContactCategorySaveReqVO().setId(category.getId())
                .setName("重点客户").setSort(2);

        // 调用
        contactCategoryService.updateContactCategory(reqVO, userId);

        // 断言
        assertEquals("重点客户", contactCategoryMapper.selectById(category.getId()).getName());
        verifyNoInteractions(contactService);
    }

    @Test
    public void testValidateContactCategory_notOwner() {
        // mock 数据
        OaContactCategoryDO category = new OaContactCategoryDO().setUserId(1L).setName("客户").setSort(1);
        contactCategoryMapper.insert(category);

        // 调用，并断言异常
        assertServiceException(
                () -> contactCategoryService.validateContactCategory(category.getId(), 2L),
                CONTACT_CATEGORY_NOT_EXISTS);
    }

    @Test
    public void testValidateContactCategory_null() {

        // 调用，并断言：分类校验不能放过缺失的编号
        assertServiceException(() -> contactCategoryService.validateContactCategory(null, 1L),
                CONTACT_CATEGORY_NOT_EXISTS);

        // 断言
        assertEquals(0L, contactCategoryMapper.selectCount());
    }

    @Test
    public void testGetContactCategory_ownerOnly() {
        // mock 数据
        OaContactCategoryDO category = new OaContactCategoryDO().setUserId(1L).setName("合作伙伴").setSort(0);
        contactCategoryMapper.insert(category);

        // 调用，并断言
        assertEquals(category.getId(), contactCategoryService.getContactCategory(category.getId(), 1L).getId());
        assertServiceException(() -> contactCategoryService.getContactCategory(category.getId(), 2L),
                CONTACT_CATEGORY_NOT_EXISTS);
    }

    @Test
    public void testDeleteContactCategory_null() {

        // 调用，并断言：缺失分类编号时不能执行分类清空
        assertServiceException(() -> contactCategoryService.deleteContactCategory(null, 1L),
                CONTACT_CATEGORY_NOT_EXISTS);
        verifyNoInteractions(contactService);
    }

    @Test
    public void testDeleteContactCategory_clearsExistingAssignments() {
        // mock 数据
        Long userId = randomLongId();
        OaContactCategoryDO category = new OaContactCategoryDO().setUserId(userId).setName("供应商").setSort(1);
        contactCategoryMapper.insert(category);

        // 调用
        contactCategoryService.deleteContactCategory(category.getId(), userId);

        // 断言
        verify(contactService).clearContactCategoryId(category.getId());
        assertNull(contactCategoryMapper.selectById(category.getId()));
    }

}
