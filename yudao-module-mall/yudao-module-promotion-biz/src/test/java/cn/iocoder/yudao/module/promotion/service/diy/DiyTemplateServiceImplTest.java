package cn.iocoder.yudao.module.promotion.service.diy;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.promotion.controller.admin.diy.vo.template.DiyTemplateCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.diy.vo.template.DiyTemplatePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.diy.vo.template.DiyTemplateUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.diy.DiyTemplateDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.diy.DiyTemplateMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.DIY_TEMPLATE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link DiyTemplateServiceImpl} 的单元测试类
 *
 * @author owen
 */
@Disabled // TODO 芋艿：后续 fix 补充的单测
@Import(DiyTemplateServiceImpl.class)
public class DiyTemplateServiceImplTest extends BaseDbUnitTest {

    @Resource
    private DiyTemplateServiceImpl diyTemplateService;

    @Resource
    private DiyTemplateMapper diyTemplateMapper;

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
        // mock 数据
        DiyTemplateDO dbDiyTemplate = randomPojo(DiyTemplateDO.class);
        diyTemplateMapper.insert(dbDiyTemplate);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbDiyTemplate.getId();

        // 调用
        diyTemplateService.deleteDiyTemplate(id);
        // 校验数据不存在了
        assertNull(diyTemplateMapper.selectById(id));
    }

    @Test
    public void testDeleteDiyTemplate_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> diyTemplateService.deleteDiyTemplate(id), DIY_TEMPLATE_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetDiyTemplatePage() {
        // mock 数据
        DiyTemplateDO dbDiyTemplate = randomPojo(DiyTemplateDO.class, o -> { // 等会查询到
            o.setName(null);
            o.setUsed(null);
            o.setUsedTime(null);
            o.setRemark(null);
            o.setPreviewPicUrls(null);
            o.setProperty(null);
            o.setCreateTime(null);
        });
        diyTemplateMapper.insert(dbDiyTemplate);
        // 测试 name 不匹配
        diyTemplateMapper.insert(cloneIgnoreId(dbDiyTemplate, o -> o.setName(null)));
        // 测试 used 不匹配
        diyTemplateMapper.insert(cloneIgnoreId(dbDiyTemplate, o -> o.setUsed(null)));
        // 测试 usedTime 不匹配
        diyTemplateMapper.insert(cloneIgnoreId(dbDiyTemplate, o -> o.setUsedTime(null)));
        // 测试 remark 不匹配
        diyTemplateMapper.insert(cloneIgnoreId(dbDiyTemplate, o -> o.setRemark(null)));
        // 测试 previewPicUrls 不匹配
        diyTemplateMapper.insert(cloneIgnoreId(dbDiyTemplate, o -> o.setPreviewPicUrls(null)));
        // 测试 property 不匹配
        diyTemplateMapper.insert(cloneIgnoreId(dbDiyTemplate, o -> o.setProperty(null)));
        // 测试 createTime 不匹配
        diyTemplateMapper.insert(cloneIgnoreId(dbDiyTemplate, o -> o.setCreateTime(null)));
        // 准备参数
        DiyTemplatePageReqVO reqVO = new DiyTemplatePageReqVO();
        reqVO.setName(null);
        reqVO.setUsed(null);
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
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetDiyTemplateList() {
        // mock 数据
        DiyTemplateDO dbDiyTemplate = randomPojo(DiyTemplateDO.class, o -> { // 等会查询到
            o.setName(null);
            o.setUsed(null);
            o.setUsedTime(null);
            o.setRemark(null);
            o.setPreviewPicUrls(null);
            o.setProperty(null);
            o.setCreateTime(null);
        });
        diyTemplateMapper.insert(dbDiyTemplate);
        // 测试 name 不匹配
        diyTemplateMapper.insert(cloneIgnoreId(dbDiyTemplate, o -> o.setName(null)));
        // 测试 used 不匹配
        diyTemplateMapper.insert(cloneIgnoreId(dbDiyTemplate, o -> o.setUsed(null)));
        // 测试 usedTime 不匹配
        diyTemplateMapper.insert(cloneIgnoreId(dbDiyTemplate, o -> o.setUsedTime(null)));
        // 测试 remark 不匹配
        diyTemplateMapper.insert(cloneIgnoreId(dbDiyTemplate, o -> o.setRemark(null)));
        // 测试 previewPicUrls 不匹配
        diyTemplateMapper.insert(cloneIgnoreId(dbDiyTemplate, o -> o.setPreviewPicUrls(null)));
        // 测试 property 不匹配
        diyTemplateMapper.insert(cloneIgnoreId(dbDiyTemplate, o -> o.setProperty(null)));
        // 测试 createTime 不匹配
        diyTemplateMapper.insert(cloneIgnoreId(dbDiyTemplate, o -> o.setCreateTime(null)));
    }

}
