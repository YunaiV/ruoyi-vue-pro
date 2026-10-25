package cn.iocoder.yudao.module.promotion.service.diy;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.promotion.controller.admin.diy.vo.template.DiyTemplateCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.diy.vo.template.DiyTemplatePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.diy.vo.template.DiyTemplateUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.diy.DiyPageDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.diy.DiyTemplateDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.diy.DiyPageMapper;
import cn.iocoder.yudao.module.promotion.dal.mysql.diy.DiyTemplateMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;
import java.util.function.Consumer;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.DIY_TEMPLATE_NOT_EXISTS;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.DIY_TEMPLATE_USED_CANNOT_DELETE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doCallRealMethod;

/**
 * {@link DiyTemplateServiceImpl} 的单元测试类
 *
 * @author owen
 */
// DONE 芋艿：后续 fix 补充的单测
@Import({DiyTemplateServiceImpl.class, DiyPageServiceImpl.class})
public class DiyTemplateServiceImplTest extends BaseDbUnitTest {

    @Resource
    private DiyTemplateServiceImpl diyTemplateService;

    @Resource
    private DiyTemplateMapper diyTemplateMapper;
    @Resource
    private DiyPageMapper diyPageMapper;

    @MockitoSpyBean
    private DiyPageService diyPageService;

    @Test
    public void testCreateDiyTemplate_success() {
        // 准备参数
        DiyTemplateCreateReqVO reqVO = randomPojo(DiyTemplateCreateReqVO.class);

        // 调用
        Long diyTemplateId = diyTemplateService.createDiyTemplate(reqVO);
        // 断言
        assertNotNull(diyTemplateId);
        // 校验记录的属性是否正确
        DiyTemplateDO diyTemplate = diyTemplateMapper.selectById(diyTemplateId);
        assertPojoEquals(reqVO, diyTemplate);
        assertEquals("{}", diyTemplate.getProperty());
        // 校验默认页面：首页、我的
        List<DiyPageDO> pages = diyPageMapper.selectListByTemplateId(diyTemplateId);
        assertEquals(asSet("首页", "我的"), convertSet(pages, DiyPageDO::getName));
        String remark = String.format("模板【%s】自动创建", reqVO.getName());
        pages.forEach(page -> {
            assertEquals(remark, page.getRemark());
            assertEquals("{}", page.getProperty());
        });
    }

    @Test
    public void testUpdateDiyTemplate_success() {
        // mock 数据
        DiyTemplateDO dbDiyTemplate = randomPojo(DiyTemplateDO.class);
        diyTemplateMapper.insert(dbDiyTemplate);// @Sql: 先插入出一条存在的数据
        // 准备参数
        DiyTemplateUpdateReqVO reqVO = randomPojo(DiyTemplateUpdateReqVO.class, o -> {
            o.setId(dbDiyTemplate.getId()); // 设置更新的 ID
        });

        // 调用
        diyTemplateService.updateDiyTemplate(reqVO);
        // 校验是否更新正确
        DiyTemplateDO diyTemplate = diyTemplateMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, diyTemplate);
    }

    @Test
    public void testUpdateDiyTemplate_notExists() {
        // 准备参数
        DiyTemplateUpdateReqVO reqVO = randomPojo(DiyTemplateUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> diyTemplateService.updateDiyTemplate(reqVO), DIY_TEMPLATE_NOT_EXISTS);
    }

    @Test
    public void testDeleteDiyTemplate_success() {
        // mock 数据（模板）
        DiyTemplateDO dbDiyTemplate = randomPojo(DiyTemplateDO.class, o -> o.setUsed(false));
        diyTemplateMapper.insert(dbDiyTemplate);// @Sql: 先插入出一条存在的数据
        DiyTemplateDO otherDiyTemplate = randomPojo(DiyTemplateDO.class, o -> o.setUsed(false));
        diyTemplateMapper.insert(otherDiyTemplate);
        // mock 数据（页面）：该模板下的全部页面，包括默认页面及后续新增页面
        diyPageMapper.insert(randomDiyPageDO(o -> o.setTemplateId(dbDiyTemplate.getId()).setName("首页")));
        diyPageMapper.insert(randomDiyPageDO(o -> o.setTemplateId(dbDiyTemplate.getId()).setName("我的")));
        diyPageMapper.insert(randomDiyPageDO(o -> o.setTemplateId(dbDiyTemplate.getId()).setName("活动页")));
        // mock 数据（页面）：其它模板下的页面、独立页面
        DiyPageDO otherTemplatePage = randomDiyPageDO(o -> o.setTemplateId(otherDiyTemplate.getId()));
        diyPageMapper.insert(otherTemplatePage);
        DiyPageDO standalonePage = randomDiyPageDO(o -> o.setTemplateId(null));
        diyPageMapper.insert(standalonePage);
        // 准备参数
        Long id = dbDiyTemplate.getId();

        // 调用
        diyTemplateService.deleteDiyTemplate(id);
        // 校验数据不存在了
        assertNull(diyTemplateMapper.selectById(id));
        assertEquals(0, diyPageMapper.selectListByTemplateId(id).size());
        // 校验其它模板、其它模板下的页面、独立页面保留
        assertNotNull(diyTemplateMapper.selectById(otherDiyTemplate.getId()));
        assertNotNull(diyPageMapper.selectById(otherTemplatePage.getId()));
        assertNotNull(diyPageMapper.selectById(standalonePage.getId()));
    }

    @Test
    public void testDeleteDiyTemplate_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> diyTemplateService.deleteDiyTemplate(id), DIY_TEMPLATE_NOT_EXISTS);
    }

    @Test
    public void testDeleteDiyTemplate_used() {
        // mock 数据
        DiyTemplateDO dbDiyTemplate = randomPojo(DiyTemplateDO.class, o -> o.setUsed(true));
        diyTemplateMapper.insert(dbDiyTemplate);// @Sql: 先插入出一条存在的数据
        DiyPageDO dbDiyPage = randomDiyPageDO(o -> o.setTemplateId(dbDiyTemplate.getId()));
        diyPageMapper.insert(dbDiyPage);
        // 准备参数
        Long id = dbDiyTemplate.getId();

        // 调用, 并断言异常
        assertServiceException(() -> diyTemplateService.deleteDiyTemplate(id), DIY_TEMPLATE_USED_CANNOT_DELETE);
        // 校验模板、页面都未删除
        assertNotNull(diyTemplateMapper.selectById(id));
        assertNotNull(diyPageMapper.selectById(dbDiyPage.getId()));
    }

    @Test
    public void testDeleteDiyTemplate_deletePageFailRollback() {
        // mock 数据
        DiyTemplateDO dbDiyTemplate = randomPojo(DiyTemplateDO.class, o -> o.setUsed(false));
        diyTemplateMapper.insert(dbDiyTemplate);// @Sql: 先插入出一条存在的数据
        diyPageMapper.insert(randomDiyPageDO(o -> o.setTemplateId(dbDiyTemplate.getId())));
        diyPageMapper.insert(randomDiyPageDO(o -> o.setTemplateId(dbDiyTemplate.getId())));
        // 准备参数
        Long id = dbDiyTemplate.getId();
        // mock 方法（第 1 个页面真实删除，第 2 个页面删除失败）
        doCallRealMethod().doThrow(new IllegalStateException("删除页面失败"))
                .when(diyPageService).deleteDiyPage(anyLong());

        // 调用, 并断言异常
        assertThrows(IllegalStateException.class, () -> diyTemplateService.deleteDiyTemplate(id));
        // 校验模板、页面都已回滚
        assertNotNull(diyTemplateMapper.selectById(id));
        assertEquals(2, diyPageMapper.selectListByTemplateId(id).size());
    }

    @Test
    // DONE 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetDiyTemplatePage() {
        // mock 数据
        DiyTemplateDO dbDiyTemplate = randomPojo(DiyTemplateDO.class, o -> { // 等会查询到
            o.setName("芋道商城");
            o.setUsed(true);
            o.setUsedTime(buildTime(2023, 2, 10));
            o.setCreateTime(buildTime(2023, 2, 10));
        });
        diyTemplateMapper.insert(dbDiyTemplate);
        // 测试 name 不匹配
        diyTemplateMapper.insert(cloneIgnoreId(dbDiyTemplate, o -> o.setName("土豆商城")));
        // 测试 used 不匹配
        diyTemplateMapper.insert(cloneIgnoreId(dbDiyTemplate, o -> o.setUsed(false)));
        // 测试 usedTime 不匹配
        diyTemplateMapper.insert(cloneIgnoreId(dbDiyTemplate, o -> o.setUsedTime(buildTime(2023, 3, 10))));
        // 测试 createTime 不匹配
        diyTemplateMapper.insert(cloneIgnoreId(dbDiyTemplate, o -> o.setCreateTime(buildTime(2023, 3, 10))));
        // 准备参数
        DiyTemplatePageReqVO reqVO = new DiyTemplatePageReqVO();
        reqVO.setName("芋道");
        reqVO.setUsed(true);
        reqVO.setUsedTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
        reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

        // 调用
        PageResult<DiyTemplateDO> pageResult = diyTemplateService.getDiyTemplatePage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbDiyTemplate, pageResult.getList().get(0));
    }

    @Test
    // DONE 请修改 null 为需要的值，然后删除 @Disabled 注解（DiyTemplateService 无列表查询方法，原 testGetDiyTemplateList 改为测试 getUsedDiyTemplate）
    public void testGetUsedDiyTemplate() {
        // mock 数据
        DiyTemplateDO dbDiyTemplate = randomPojo(DiyTemplateDO.class, o -> o.setUsed(true));
        diyTemplateMapper.insert(dbDiyTemplate);
        // 测试 used 不匹配
        diyTemplateMapper.insert(cloneIgnoreId(dbDiyTemplate, o -> o.setName("土豆商城").setUsed(false)));

        // 调用
        DiyTemplateDO usedDiyTemplate = diyTemplateService.getUsedDiyTemplate();
        // 断言
        assertPojoEquals(dbDiyTemplate, usedDiyTemplate);
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static DiyPageDO randomDiyPageDO(Consumer<DiyPageDO>... consumers) {
        return randomPojo(DiyPageDO.class, consumers);
    }

}
