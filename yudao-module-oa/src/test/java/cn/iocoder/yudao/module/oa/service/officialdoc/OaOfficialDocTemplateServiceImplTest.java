package cn.iocoder.yudao.module.oa.service.officialdoc;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.oa.controller.admin.officialdoc.vo.template.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.officialdoc.*;
import cn.iocoder.yudao.module.oa.dal.mysql.officialdoc.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.util.object.BeanUtils.toBean;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link OaOfficialDocTemplateServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(OaOfficialDocTemplateServiceImpl.class)
public class OaOfficialDocTemplateServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaOfficialDocTemplateService officialDocTemplateService;

    @Resource
    private OaOfficialDocTemplateMapper officialDocTemplateMapper;

    @MockBean
    private OaOfficialDocSendService officialDocSendService;

    @Test
    public void testDeleteOfficialDocTemplate_inUse() {
        // mock 数据
        OaOfficialDocTemplateDO record = randomOfficialDocTemplateDO();
        officialDocTemplateMapper.insert(record);
        // mock 方法
        when(officialDocSendService.getOfficialDocSendCountByTemplateId(record.getId())).thenReturn(1L);

        // 调用，并断言异常
        assertServiceException(() -> officialDocTemplateService.deleteOfficialDocTemplate(record.getId()),
                OFFICIAL_DOC_TEMPLATE_IN_USE);
        assertNotNull(officialDocTemplateMapper.selectById(record.getId()));
    }

    @Test
    public void testCreateOfficialDocTemplate() {
        // 准备参数
        OaOfficialDocTemplateSaveReqVO reqVO = toBean(randomOfficialDocTemplateDO(), OaOfficialDocTemplateSaveReqVO.class)
                .setId(null);

        // 调用
        Long id = officialDocTemplateService.createOfficialDocTemplate(reqVO);
        // 断言
        assertEquals(reqVO.getName(), officialDocTemplateMapper.selectById(id).getName());
    }

    @Test
    public void testUpdateOfficialDocTemplate() {
        // mock 数据
        OaOfficialDocTemplateDO template = randomOfficialDocTemplateDO();
        officialDocTemplateMapper.insert(template);
        // 准备参数
        OaOfficialDocTemplateSaveReqVO reqVO = toBean(template, OaOfficialDocTemplateSaveReqVO.class)
                .setName("修改后的模板").setNoPrefix(null).setRemark(null).setSealPicUrl(null);

        // 调用
        officialDocTemplateService.updateOfficialDocTemplate(reqVO);
        // 断言
        assertEquals("修改后的模板", officialDocTemplateMapper.selectById(template.getId()).getName());
        assertEquals(template.getNoPrefix(), officialDocTemplateMapper.selectById(template.getId()).getNoPrefix());
        assertEquals(template.getRemark(), officialDocTemplateMapper.selectById(template.getId()).getRemark());
        assertEquals(template.getSealPicUrl(), officialDocTemplateMapper.selectById(template.getId()).getSealPicUrl());
    }

    @Test
    public void testDeleteOfficialDocTemplate() {
        // mock 数据
        OaOfficialDocTemplateDO template = randomOfficialDocTemplateDO();
        officialDocTemplateMapper.insert(template);

        // 调用
        officialDocTemplateService.deleteOfficialDocTemplate(template.getId());
        // 断言
        assertNull(officialDocTemplateMapper.selectById(template.getId()));
    }

    @Test
    public void testGetOfficialDocTemplate_notExists() {
        // 调用，并断言
        assertNull(officialDocTemplateService.getOfficialDocTemplate(999L));
    }

    @Test
    public void testUpdateOfficialDocTemplate_notExists() {
        // 准备参数
        OaOfficialDocTemplateSaveReqVO reqVO = toBean(randomOfficialDocTemplateDO(), OaOfficialDocTemplateSaveReqVO.class)
                .setId(999L);

        // 调用，并断言异常
        assertServiceException(() -> officialDocTemplateService.updateOfficialDocTemplate(reqVO),
                OFFICIAL_DOC_TEMPLATE_NOT_EXISTS);
    }

    @Test
    public void testDeleteOfficialDocTemplate_notExists() {

        // 调用，并断言异常
        assertServiceException(() -> officialDocTemplateService.deleteOfficialDocTemplate(999L),
                OFFICIAL_DOC_TEMPLATE_NOT_EXISTS);
    }

    @Test
    public void testValidateOfficialDocTemplate() {
        // mock 数据
        OaOfficialDocTemplateDO template = randomOfficialDocTemplateDO().setStatus(0);
        officialDocTemplateMapper.insert(template);

        // 调用
        OaOfficialDocTemplateDO result = officialDocTemplateService.validateOfficialDocTemplate(template.getId());
        // 断言
        assertEquals(template.getId(), result.getId());
    }

    @Test
    public void testValidateOfficialDocTemplate_notExists() {

        // 调用，并断言异常
        assertServiceException(() -> officialDocTemplateService.validateOfficialDocTemplate(999L),
                OFFICIAL_DOC_TEMPLATE_NOT_EXISTS);
    }

    @Test
    public void testValidateOfficialDocTemplate_disabled() {
        // mock 数据
        OaOfficialDocTemplateDO template = randomOfficialDocTemplateDO().setStatus(1);
        officialDocTemplateMapper.insert(template);

        // 调用，并断言异常
        assertServiceException(() -> officialDocTemplateService.validateOfficialDocTemplate(template.getId()),
                OFFICIAL_DOC_TEMPLATE_DISABLED);
    }

    @Test
    public void testGetOfficialDocTemplatePage() {
        // mock 数据
        officialDocTemplateMapper.insert(randomOfficialDocTemplateDO().setName("示例模板").setStatus(0));
        officialDocTemplateMapper.insert(randomOfficialDocTemplateDO().setName("其他模板").setStatus(1));
        // 准备参数
        OaOfficialDocTemplatePageReqVO reqVO = new OaOfficialDocTemplatePageReqVO().setName("示例").setStatus(0);

        // 调用
        PageResult<OaOfficialDocTemplateDO> result = officialDocTemplateService.getOfficialDocTemplatePage(reqVO);
        // 断言
        assertEquals(1L, result.getTotal());
        assertEquals("示例模板", CollUtil.getFirst(result.getList()).getName());
    }


    // ========== 随机对象 ==========

    /**
     * 构造测试数据，固定业务字段的合法取值。
     *
     * @return 未入库的测试对象
     */
    private static OaOfficialDocTemplateDO randomOfficialDocTemplateDO() {
        return randomPojo(OaOfficialDocTemplateDO.class, o -> o.setId(null).setFontSize(32)
                .setSeparatorType(0).setStatus(0).setSort(0));
    }

}
