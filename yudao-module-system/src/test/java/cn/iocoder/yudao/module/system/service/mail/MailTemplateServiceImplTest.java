package cn.iocoder.yudao.module.system.service.mail;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplateSaveReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplatePageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailTemplateDO;
import cn.iocoder.yudao.module.system.dal.mysql.mail.MailTemplateMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.MAIL_TEMPLATE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link MailTemplateServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(MailTemplateServiceImpl.class)
public class MailTemplateServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MailTemplateServiceImpl mailTemplateService;

    @Resource
    private MailTemplateMapper mailTemplateMapper;

    @Test
    public void testCreateMailTemplate_success() {
        // 准备参数
        MailTemplateSaveReqVO reqVO = randomPojo(MailTemplateSaveReqVO.class)
                .setId(null); // 防止 id 被赋值

        // 调用
        Long mailTemplateId = mailTemplateService.createMailTemplate(reqVO);
        // 断言
        assertNotNull(mailTemplateId);
        // 校验记录的属性是否正确
        MailTemplateDO mailTemplate = mailTemplateMapper.selectById(mailTemplateId);
        assertPojoEquals(reqVO, mailTemplate, "id");
    }

    @Test
    public void testUpdateMailTemplate_success() {
        // mock 数据
        MailTemplateDO dbMailTemplate = randomPojo(MailTemplateDO.class);
        mailTemplateMapper.insert(dbMailTemplate);// @Sql: 先插入出一条存在的数据
        // 准备参数
        MailTemplateSaveReqVO reqVO = randomPojo(MailTemplateSaveReqVO.class, o -> {
            o.setId(dbMailTemplate.getId()); // 设置更新的 ID
        });

        // 调用
        mailTemplateService.updateMailTemplate(reqVO);
        // 校验是否更新正确
        MailTemplateDO mailTemplate = mailTemplateMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, mailTemplate);
    }

    @Test
    public void testUpdateMailTemplate_notExists() {
        // 准备参数
        MailTemplateSaveReqVO reqVO = randomPojo(MailTemplateSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> mailTemplateService.updateMailTemplate(reqVO), MAIL_TEMPLATE_NOT_EXISTS);
    }

    @Test
    public void testDeleteMailTemplate_success() {
        // mock 数据
        MailTemplateDO dbMailTemplate = randomPojo(MailTemplateDO.class);
        mailTemplateMapper.insert(dbMailTemplate);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbMailTemplate.getId();

        // 调用
        mailTemplateService.deleteMailTemplate(id);
        // 校验数据不存在了
        assertNull(mailTemplateMapper.selectById(id));
    }

    @Test
    public void testDeleteMailTemplate_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> mailTemplateService.deleteMailTemplate(id), MAIL_TEMPLATE_NOT_EXISTS);
    }

    @Test
    public void testGetMailTemplatePage() {
        // mock 数据
        MailTemplateDO dbMailTemplate = randomPojo(MailTemplateDO.class, o -> { // 等会查询到
            o.setName("源码");
            o.setCode("test_01");
            o.setAccountId(1L);
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setCreateTime(buildTime(2023, 2, 3));
        });
        mailTemplateMapper.insert(dbMailTemplate);
        // 测试 name 不匹配
        mailTemplateMapper.insert(cloneIgnoreId(dbMailTemplate, o -> o.setName("芋道")));
        // 测试 code 不匹配
        mailTemplateMapper.insert(cloneIgnoreId(dbMailTemplate, o -> o.setCode("test_02")));
        // 测试 accountId 不匹配
        mailTemplateMapper.insert(cloneIgnoreId(dbMailTemplate, o -> o.setAccountId(2L)));
        // 测试 status 不匹配
        mailTemplateMapper.insert(cloneIgnoreId(dbMailTemplate, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // 测试 createTime 不匹配
        mailTemplateMapper.insert(cloneIgnoreId(dbMailTemplate, o -> o.setCreateTime(buildTime(2023, 1, 5))));
        // 准备参数
        MailTemplatePageReqVO reqVO = new MailTemplatePageReqVO();
        reqVO.setName("源");
        reqVO.setCode("est_01");
        reqVO.setAccountId(1L);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 5));

        // 调用
        PageResult<MailTemplateDO> pageResult = mailTemplateService.getMailTemplatePage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbMailTemplate, pageResult.getList().get(0));
    }

    @Test
    public void testGetMailTemplateList() {
        // mock 数据
        MailTemplateDO dbMailTemplate01 = randomPojo(MailTemplateDO.class);
        mailTemplateMapper.insert(dbMailTemplate01);
        MailTemplateDO dbMailTemplate02 = randomPojo(MailTemplateDO.class);
        mailTemplateMapper.insert(dbMailTemplate02);

        // 调用
        List<MailTemplateDO> list = mailTemplateService.getMailTemplateList();
        // 断言
        assertEquals(2, list.size());
        assertEquals(dbMailTemplate01, list.get(0));
        assertEquals(dbMailTemplate02, list.get(1));
    }

    @Test
    public void testGetMailTemplate() {
        // mock 数据
        MailTemplateDO dbMailTemplate = randomPojo(MailTemplateDO.class);
        mailTemplateMapper.insert(dbMailTemplate);
        // 准备参数
        Long id = dbMailTemplate.getId();

        // 调用
        MailTemplateDO mailTemplate = mailTemplateService.getMailTemplate(id);
        // 断言
        assertPojoEquals(dbMailTemplate, mailTemplate);
    }

    @Test
    public void testGetMailTemplateByCodeFromCache() {
        // mock 数据
        MailTemplateDO dbMailTemplate = randomPojo(MailTemplateDO.class);
        mailTemplateMapper.insert(dbMailTemplate);
        // 准备参数
        String code = dbMailTemplate.getCode();

        // 调用
        MailTemplateDO mailTemplate = mailTemplateService.getMailTemplateByCodeFromCache(code);
        // 断言
        assertPojoEquals(dbMailTemplate, mailTemplate);
    }

    @Test
    public void testFormatMailTemplateContent() {
        // 准备参数
        Map<String, Object> params = new HashMap<>();
        params.put("name", "小红");
        params.put("what", "饭");

        // 调用，并断言
        assertEquals("小红，你好，饭吃了吗？",
                mailTemplateService.formatMailTemplateContent("{name}，你好，{what}吃了吗？", params));
    }

    @Test
    public void testFormatMailTemplateContent_htmlUnescape() {
        // 准备参数
        Map<String, Object> params = new HashMap<>();
        params.put("title", "测试标题");

        // 测试HTML反转义
        String content = "<h1>{title}</h1>&lt;p&gt;这是一个测试&lt;/p&gt;&amp;nbsp;空格";
        String expected = "<h1>测试标题</h1><p>这是一个测试</p> 空格";
        // 调用，并断言
        assertEquals(expected,
                mailTemplateService.formatMailTemplateContent(content, params));
    }

    @Test
    public void testFormatMailTemplateContent_codeBlockFormatting() {
        // 准备参数
        Map<String, Object> params = new HashMap<>();
        params.put("name", "测试");

        // 测试代码块格式化
        String content = "<pre><code>public class Test {\n    public static void main(String[] args) {\n        System.out.println(\"Hello {name}\"));\n    }\n}</code></pre>";

        // 调用，并断言结果
        String result = mailTemplateService.formatMailTemplateContent(content, params);
        // 断言 pre 标签被替换为 div 标签
        assertTrue(result.contains("<div><code>public class Test {"));
        assertTrue(result.contains("System.out.println(\"Hello 测试\""));
        assertTrue(result.contains("</code></div>"));
    }

    @Test
    public void testFormatMailTemplateContent_preToDiv() {
        // 准备参数
        Map<String, Object> params = new HashMap<>();
        params.put("content", "测试内容");

        // 测试 pre 标签替换为 div 标签
        String content = "<pre><code>{content}</code></pre>";
        String result = mailTemplateService.formatMailTemplateContent(content, params);
        // 断言结果中包含 div 标签，而不包含 pre 标签
        assertTrue(result.contains("<div><code>测试内容</code></div>"));
    }

    @Test
    public void testFormatMailTemplateContent_completeHtml() {
        // 准备参数
        Map<String, Object> params = new HashMap<>();
        params.put("username", "testuser");
        params.put("company", "测试公司");

        // 测试完整的 HTML 邮件模板
        String content = "<!DOCTYPE html>\n <html lang=\"en\">\n <head>\n  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n  <meta charset=\"UTF-8\">\n  <title>Title</title>\n </head>\n <body>\n <div>\n  <includetail>\n      <div>\n          <div class=\"open_email\" style=\"margin: 8px; \">\n              <div>\n                  <br>\n                  <span class=\"genEmailContent\">\n                      <div id=\"cTMail-Wrap\" style=\"word-break: break-all;box-sizing:border-box;text-align:left;min-width:320px; max-width:660px; border:1px solid #f6f6f6; background-color:#f7f8fa; margin:auto; padding:20px 0 30px; font-family:'helvetica neue',PingFangSC-Light,arial,'hiragino sans gb','microsoft yahei ui','microsoft yahei',simsun,sans-serif\">\n                          <div class=\"main-content\">\n                              <table style=\"width:100%;font-weight:300;margin-bottom:10px;border-collapse:collapse\">\n                                  <tbody>\n                                  <tr style=\"font-weight:300\">\n                                      <td style=\"width:3%;max-width:30px;\"></td>\n                                      <td style=\"max-width:600px;\">\n                                          <div id=\"cTMail-logo\" style=\"width:92px; height:36px;\">\n                                              <a href=\"\">\n                                                  <img border=\"0\" src=\"左上角图片logo\" style=\"width:120px; height:36px;display:block\">\n                                              </a>\n                                          </div>\n                                          <div style=\"color: #C2C5C9;width: 260px;float: right;font-size: 12px;\">此邮件由系统发出，请勿直接回复或转发他人</div>\n                                          <p style=\"height:2px;background-color: #00a4ff;border: 0;font-size:0;padding:0;width:100%;margin-top:20px;\"></p>\n                                          <div id=\"cTMail-inner\" style=\"background-color:#fff; padding:23px 0 20px;box-shadow: 0px 1px 1px 0px rgba(122, 55, 55, 0.2);text-align:left;\">\n                                              <table style=\"width:100%;font-weight:300;margin-bottom:10px;border-collapse:collapse;\">\n                                                  <tbody>\n                                                  <tr style=\"font-weight:300\">\n                                                      <td style=\"width:3.2%;max-width:30px;\"></td>\n                                                      <td style=\"max-width:480px;\">\n                                                          <h1 id=\"cTMail-title\" style=\"font-size: 20px; line-height: 36px; margin: 0px 0px 22px;\">\n                                                              尊敬的 {username}，\n                                                          </h1>\n                                                          <dl style=\"font-size: 14px; color: #595E65; line-height: 18px;\">\n                                                              <dd style=\"margin: 0px 0px 6px; padding: 0px; font-size: 14px; line-height: 22px;\">\n                                                                  <p id=\"cTMail-sender\" style=\"font-size: 14px; line-height: 26px; word-wrap: break-word; word-break: break-all; margin-top: 32px;\">\n                                                                     内容<br>\n 内容<br>\n 内容123<br><br>\n\n 如果您在使用过程中遇到任何问题或者有任何建议，都可以随时联系我们的客户团队，我们将竭诚为您服务。\n\n\n\n\n <br>\n                                                                      <br>\n                                                                      {company}<br>\n                                                                      地址：xxxxx<br>\n                                                                      邮箱：lambc77@163.com\n                                                                  </p>\n                                                              </dd>\n                                                          </dl>\n                                                          <hr style=\"border: 0.1px solid #e5e5e5;\"/>\n                                                           <dl style=\"font-size: 14px; color: #595E65; line-height: 18px;\">\n                                                               <div style=\"color: #93979B;\">\n                                                                   <strong>声明：本邮件含有保密信息，仅限于收件人所用。禁止任何人未经发件人许可，以任何形式（包括但不限于部分的泄露、复制或散发）不当的使用本邮件中的信息。如果您错收了本邮件，请您立即电话或邮件通知发件人并删除本邮件，谢谢！\n </strong><br>\n                                                               </div>\n                                                           </dl>\n                                                      </td>\n                                                      <td style=\"width:3.2%;max-width:30px;\"></td>\n                                                  </tr>\n                                                  </tbody>\n                                              </table>\n                                          </div>\n                                      </td>\n                                      <td style=\"width:3%;max-width:30px;\"></td>\n                                  </tr>\n                                  </tbody>\n                              </table>\n                          </div>\n                      </div>\n                  </span>\n              </div>\n          </div>\n      </div>\n  </includetail>\n </div>\n </body>\n <script>\n\n </script>\n </html>";

        // 调用，并断言成功处理
        String result = mailTemplateService.formatMailTemplateContent(content, params);
        // 断言结果中包含替换后的变量
        assertTrue(result.contains("尊敬的 testuser"));
        assertTrue(result.contains("测试公司"));
        // 断言结果是有效的 HTML
        assertTrue(result.startsWith("<!DOCTYPE html>"));
    }

    @Test
    public void testFormatMailTemplateContent_emptyContent() {
        // 准备参数
        Map<String, Object> params = new HashMap<>();

        // 测试空内容
        String result = mailTemplateService.formatMailTemplateContent("", params);
        assertEquals("", result);
    }

    @Test
    public void testFormatMailTemplateContent_noParams() {
        // 准备参数
        Map<String, Object> params = new HashMap<>();

        // 测试没有参数需要替换的情况
        String content = "<pre><code>System.out.println(\"Hello World\");</code></pre>";
        String result = mailTemplateService.formatMailTemplateContent(content, params);
        assertTrue(result.contains("<div><code>System.out.println(\"Hello World\");</code></div>"));
    }

    @Test
    public void testFormatMailTemplateContent_multiplePreTags() {
        // 准备参数
        Map<String, Object> params = new HashMap<>();
        params.put("param1", "value1");
        params.put("param2", "value2");

        // 测试多个 pre 标签的情况
        String content = "<pre><code>First code block: {param1}</code></pre>\n" +
                "<p>Some text between code blocks</p>\n" +
                "<pre><code>Second code block: {param2}</code></pre>";
        String result = mailTemplateService.formatMailTemplateContent(content, params);
        // 断言两个pre标签都被替换为div标签
        assertTrue(result.contains("<div><code>First code block: value1</code></div>"));
        assertTrue(result.contains("<div><code>Second code block: value2</code></div>"));
    }

    @Test
    public void testFormatMailTemplateContent_specialCharacters() {
        // 准备参数
        Map<String, Object> params = new HashMap<>();

        // 简化测试，只测试基本的 HTML 特殊字符
        String content = "&lt;div&gt;测试 &amp; 特殊字符&lt;/div&gt;";
        String result = mailTemplateService.formatMailTemplateContent(content, params);
        // 断言特殊字符被正确反转义
        assertTrue(result.contains("<div>测试 & 特殊字符</div>"));
    }

    @Test
    public void testCountByAccountId() {
        // mock 数据
        MailTemplateDO dbMailTemplate = randomPojo(MailTemplateDO.class);
        mailTemplateMapper.insert(dbMailTemplate);
        // 测试 accountId 不匹配
        mailTemplateMapper.insert(cloneIgnoreId(dbMailTemplate, o -> o.setAccountId(2L)));
        // 准备参数
        Long accountId = dbMailTemplate.getAccountId();

        // 调用
        long count = mailTemplateService.getMailTemplateCountByAccountId(accountId);
        // 断言
        assertEquals(1, count);
    }

    @Test
    public void testDifferenceWithHtmlContent() {
        // 准备包含 HTML 格式的模板内容
        String content = "<div style='font-family: Arial, sans-serif; color: #333;'>" +
                "<h1>Welcome, {username}!</h1>" +
                "<p>Your account has been created successfully.</p>" +
                "<div style='background-color: #f0f0f0; padding: 10px; border-radius: 5px;'>" +
                "<strong>Account Details:</strong><br>" +
                "Username: {username}<br>" +
                "Email: {email}<br>" +
                "Role: {role}<br>" +
                "</div>" +
                "<p>Please click <a href='{activationLink}'>here</a> to activate your account.</p>" +
                "<pre><code>public class WelcomeMessage {\n    public static void main(String[] args) {\n        System.out.println(\"Hello {username}!\");\n    }\n}</code></pre>" +
                "</div>";

        Map<String, Object> params = new HashMap<>();
        params.put("username", "testuser");
        params.put("email", "test@163.com");
        params.put("role", "admin");
        params.put("activationLink", "https://example.com/activate?code=12345");

        // 1. 使用 parseTemplateContentParams：只提取参数名称，忽略了 HTML 格式
        List<String> parsedParams = mailTemplateService.parseTemplateContentParams(content);
        System.out.println("parseTemplateContentParams结果：" + parsedParams);

        // 断言：只提取了纯参数名称，没有 HTML 格式
        assertEquals(6, parsedParams.size());
        // 检查所有参数类型
        assertEquals(3, parsedParams.stream().filter("username"::equals).count());
        assertEquals(1, parsedParams.stream().filter("email"::equals).count());
        assertEquals(1, parsedParams.stream().filter("role"::equals).count());
        assertEquals(1, parsedParams.stream().filter("activationLink"::equals).count());
        // 断言：没有包含任何 HTML 标签
        for (String param : parsedParams) {
            assertFalse(param.contains("<"));
            assertFalse(param.contains(">"));
        }

        // 2. 使用 formatMailTemplateContent：处理 HTML 格式，生成最终内容
        String formattedContent = mailTemplateService.formatMailTemplateContent(content, params);
        System.out.println("formatMailTemplateContent结果：" + formattedContent);

        // 断言：HTML 格式被保留并处理
        assertTrue(formattedContent.contains("<div style='font-family: Arial, sans-serif; color: #333;'>"));
        assertTrue(formattedContent.contains("<h1>Welcome, testuser!</h1>"));
        assertTrue(formattedContent.contains("<a href='https://example.com/activate?code=12345'>here</a>"));
        assertTrue(formattedContent.contains("<div><code>public class WelcomeMessage {"));
        assertTrue(formattedContent.contains("</code></div>"));
        // 断言：所有参数都被正确替换
        assertFalse(formattedContent.contains("{username}"));
        assertFalse(formattedContent.contains("{email}"));
        assertFalse(formattedContent.contains("{role}"));
        assertFalse(formattedContent.contains("{activationLink}"));
    }

}
