package cn.iocoder.yudao.module.system.service.notify;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.template.NotifyTemplatePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.template.NotifyTemplateSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyTemplateDO;
import cn.iocoder.yudao.module.system.dal.mysql.notify.NotifyTemplateMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.NOTIFY_TEMPLATE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link NotifyTemplateServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(NotifyTemplateServiceImpl.class)
public class NotifyTemplateServiceImplTest extends BaseDbUnitTest {

    @Resource
    private NotifyTemplateServiceImpl notifyTemplateService;

    @Resource
    private NotifyTemplateMapper notifyTemplateMapper;

    @Test
    public void testCreateNotifyTemplate_success() {
        // 准备参数
        NotifyTemplateSaveReqVO reqVO = randomPojo(NotifyTemplateSaveReqVO.class,
                o -> o.setStatus(randomCommonStatus()))
                .setId(null); // 防止 id 被赋值

        // 调用
        Long notifyTemplateId = notifyTemplateService.createNotifyTemplate(reqVO);
        // 断言
        assertNotNull(notifyTemplateId);
        // 校验记录的属性是否正确
        NotifyTemplateDO notifyTemplate = notifyTemplateMapper.selectById(notifyTemplateId);
        assertPojoEquals(reqVO, notifyTemplate, "id");
    }

    @Test
    public void testUpdateNotifyTemplate_success() {
        // mock 数据
        NotifyTemplateDO dbNotifyTemplate = randomPojo(NotifyTemplateDO.class);
        notifyTemplateMapper.insert(dbNotifyTemplate);// @Sql: 先插入出一条存在的数据
        // 准备参数
        NotifyTemplateSaveReqVO reqVO = randomPojo(NotifyTemplateSaveReqVO.class, o -> {
            o.setId(dbNotifyTemplate.getId()); // 设置更新的 ID
            o.setStatus(randomCommonStatus());
        });

        // 调用
        notifyTemplateService.updateNotifyTemplate(reqVO);
        // 校验是否更新正确
        NotifyTemplateDO notifyTemplate = notifyTemplateMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, notifyTemplate);
    }

    @Test
    public void testUpdateNotifyTemplate_notExists() {
        // 准备参数
        NotifyTemplateSaveReqVO reqVO = randomPojo(NotifyTemplateSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> notifyTemplateService.updateNotifyTemplate(reqVO), NOTIFY_TEMPLATE_NOT_EXISTS);
    }

    @Test
    public void testDeleteNotifyTemplate_success() {
        // mock 数据
        NotifyTemplateDO dbNotifyTemplate = randomPojo(NotifyTemplateDO.class);
        notifyTemplateMapper.insert(dbNotifyTemplate);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbNotifyTemplate.getId();

        // 调用
        notifyTemplateService.deleteNotifyTemplate(id);
        // 校验数据不存在了
        assertNull(notifyTemplateMapper.selectById(id));
    }

    @Test
    public void testDeleteNotifyTemplate_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> notifyTemplateService.deleteNotifyTemplate(id), NOTIFY_TEMPLATE_NOT_EXISTS);
    }

    @Test
    public void testGetNotifyTemplatePage() {
        // mock 数据
        NotifyTemplateDO dbNotifyTemplate = randomPojo(NotifyTemplateDO.class, o -> { // 等会查询到
            o.setName("芋头");
            o.setCode("test_01");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setCreateTime(buildTime(2022, 2, 3));
        });
        notifyTemplateMapper.insert(dbNotifyTemplate);
        // 测试 name 不匹配
        notifyTemplateMapper.insert(cloneIgnoreId(dbNotifyTemplate, o -> o.setName("投")));
        // 测试 code 不匹配
        notifyTemplateMapper.insert(cloneIgnoreId(dbNotifyTemplate, o -> o.setCode("test_02")));
        // 测试 status 不匹配
        notifyTemplateMapper.insert(cloneIgnoreId(dbNotifyTemplate, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // 测试 createTime 不匹配
        notifyTemplateMapper.insert(cloneIgnoreId(dbNotifyTemplate, o -> o.setCreateTime(buildTime(2022, 1, 5))));
        // 准备参数
        NotifyTemplatePageReqVO reqVO = new NotifyTemplatePageReqVO();
        reqVO.setName("芋");
        reqVO.setCode("est_01");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setCreateTime(buildBetweenTime(2022, 2, 1, 2022, 2, 5));

        // 调用
        PageResult<NotifyTemplateDO> pageResult = notifyTemplateService.getNotifyTemplatePage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbNotifyTemplate, pageResult.getList().get(0));
    }

    @Test
    public void testGetNotifyTemplate() {
        // mock 数据
        NotifyTemplateDO dbNotifyTemplate = randomPojo(NotifyTemplateDO.class);
        notifyTemplateMapper.insert(dbNotifyTemplate);
        // 准备参数
        Long id = dbNotifyTemplate.getId();

        // 调用
        NotifyTemplateDO notifyTemplate = notifyTemplateService.getNotifyTemplate(id);
        // 断言
        assertPojoEquals(dbNotifyTemplate, notifyTemplate);
    }

    @Test
    public void testGetNotifyTemplateByCodeFromCache() {
        // mock 数据
        NotifyTemplateDO dbNotifyTemplate = randomPojo(NotifyTemplateDO.class);
        notifyTemplateMapper.insert(dbNotifyTemplate);
        // 准备参数
        String code = dbNotifyTemplate.getCode();

        // 调用
        NotifyTemplateDO notifyTemplate = notifyTemplateService.getNotifyTemplateByCodeFromCache(code);
        // 断言
        assertPojoEquals(dbNotifyTemplate, notifyTemplate);
    }

    @Test
    public void testFormatNotifyTemplateContent() {
        // 准备参数
        Map<String, Object> params = new HashMap<>();
        params.put("name", "小红");
        params.put("what", "饭");

        // 调用，并断言
        assertEquals("小红，你好，饭吃了吗？",
                notifyTemplateService.formatNotifyTemplateContent("{name}，你好，{what}吃了吗？", params));
    }
}
