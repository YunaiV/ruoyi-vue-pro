package cn.iocoder.yudao.module.system.service.mail;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplateSaveReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplatePageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailTemplateDO;
import cn.iocoder.yudao.module.system.dal.mysql.mail.MailTemplateMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import jakarta.annotation.Resource;
import java.time.Duration;
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
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.MAIL_TEMPLATE_CODE_EXISTS;
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
    public void testCreateMailTemplate_codeExists() {
        // mock 数据
        MailTemplateDO dbMailTemplate = randomPojo(MailTemplateDO.class);
        mailTemplateMapper.insert(dbMailTemplate);
        // 准备参数
        MailTemplateSaveReqVO reqVO = randomPojo(MailTemplateSaveReqVO.class)
                .setId(null).setCode(dbMailTemplate.getCode());

        // 调用, 并断言异常
        assertServiceException(() -> mailTemplateService.createMailTemplate(reqVO),
                MAIL_TEMPLATE_CODE_EXISTS, dbMailTemplate.getCode());
    }

    @Test
    public void testUpdateMailTemplate_codeExists() {
        // mock 数据
        MailTemplateDO dbMailTemplate = randomPojo(MailTemplateDO.class).setCode("existing");
        mailTemplateMapper.insert(dbMailTemplate);
        MailTemplateDO otherMailTemplate = randomPojo(MailTemplateDO.class).setCode("other");
        mailTemplateMapper.insert(otherMailTemplate);
        // 准备参数
        MailTemplateSaveReqVO reqVO = randomPojo(MailTemplateSaveReqVO.class)
                .setId(dbMailTemplate.getId()).setCode(otherMailTemplate.getCode());

        // 调用, 并断言异常
        assertServiceException(() -> mailTemplateService.updateMailTemplate(reqVO),
                MAIL_TEMPLATE_CODE_EXISTS, otherMailTemplate.getCode());
    }

    @Test
    public void testUpdateMailTemplate_sameCode() {
        // mock 数据
        MailTemplateDO dbMailTemplate = randomPojo(MailTemplateDO.class);
        mailTemplateMapper.insert(dbMailTemplate);
        // 准备参数
        MailTemplateSaveReqVO reqVO = randomPojo(MailTemplateSaveReqVO.class)
                .setId(dbMailTemplate.getId()).setCode(dbMailTemplate.getCode());

        // 调用
        mailTemplateService.updateMailTemplate(reqVO);
        // 断言
        assertPojoEquals(reqVO, mailTemplateMapper.selectById(reqVO.getId()));
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
    public void testGetMailTemplateListByStatus() {
        // mock 数据
        MailTemplateDO dbMailTemplate = randomPojo(MailTemplateDO.class,
                o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        mailTemplateMapper.insert(dbMailTemplate);
        mailTemplateMapper.insert(cloneIgnoreId(dbMailTemplate,
                o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));

        // 调用
        List<MailTemplateDO> list = mailTemplateService.getMailTemplateListByStatus(
                CommonStatusEnum.ENABLE.getStatus());
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(dbMailTemplate, list.get(0));
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
    public void testFormatMailTemplateContent_htmlEntitiesAsText() {
        // 准备参数
        Map<String, Object> params = new HashMap<>();
        params.put("title", "测试标题");

        // 测试 HTML 实体按标准 HTML 解析为文字，不再反转义成标签
        String content = "<h1>{title}</h1>&lt;p&gt;这是一个测试&lt;/p&gt;&amp;nbsp;空格";
        // 调用，并断言
        Document document = Jsoup.parseBodyFragment(mailTemplateService.formatMailTemplateContent(content, params));
        assertEquals("测试标题", document.select("h1").text());
        assertTrue(document.select("p").isEmpty());
        assertEquals("<p>这是一个测试</p>&nbsp;空格", document.body().textNodes().get(0).getWholeText());
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
        // 断言结果是完整的 HTML 文档，head 内容被保留
        assertTrue(StrUtil.startWithIgnoreCase(result, "<!doctype html>"));
        Document document = Jsoup.parse(result);
        assertEquals("en", document.selectFirst("html").attr("lang"));
        assertEquals("Title", document.title());
        assertEquals(2, document.select("head > meta").size());
    }

    @Test
    public void testFormatMailTemplateContent_fullDocumentKeepsHeadAndStyle() {
        // 准备参数
        Map<String, Object> params = new HashMap<>();
        params.put("title", "通知");

        // 测试完整文档中的 head、style 被保留，body 中的代码块被格式化
        String content = "<!DOCTYPE html><html lang=\"zh\"><head><meta charset=\"UTF-8\"><title>{title}</title>"
                + "<style>.code > span { color: #f00; }</style></head>"
                + "<body><p>你好</p><pre class=\"p\"><code class=\"c\">x</code></pre></body></html>";
        String result = mailTemplateService.formatMailTemplateContent(content, params);

        // 断言
        assertTrue(StrUtil.startWithIgnoreCase(result, "<!doctype html>"));
        assertTrue(result.contains("<style>.code > span { color: #f00; }</style>"));
        assertTrue(result.contains("<body><p>你好</p><div><code>x</code></div></body>"));
        Document document = Jsoup.parse(result);
        assertEquals("zh", document.selectFirst("html").attr("lang"));
        assertEquals("通知", document.title());
        assertEquals("UTF-8", document.selectFirst("head > meta").attr("charset"));
    }

    @Test
    public void testFormatMailTemplateContent_emptyContent() {
        // 准备参数
        Map<String, Object> params = new HashMap<>();

        // 测试空内容
        assertEquals("", mailTemplateService.formatMailTemplateContent("", params));
        assertNull(mailTemplateService.formatMailTemplateContent(null, params));
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
        // 断言特殊字符按标准 HTML 显示为文字
        Document document = Jsoup.parseBodyFragment(result);
        assertTrue(document.body().children().isEmpty());
        assertEquals("<div>测试 & 特殊字符</div>", document.body().text());
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
        Document document = Jsoup.parseBodyFragment(formattedContent);
        assertEquals("font-family: Arial, sans-serif; color: #333;", document.body().child(0).attr("style"));
        assertTrue(formattedContent.contains("<h1>Welcome, testuser!</h1>"));
        assertEquals("https://example.com/activate?code=12345", document.selectFirst("a").attr("href"));
        assertEquals("here", document.selectFirst("a").text());
        assertTrue(formattedContent.contains("<div><code>public class WelcomeMessage {"));
        assertTrue(formattedContent.contains("</code></div>"));
        // 断言：所有参数都被正确替换
        assertFalse(formattedContent.contains("{username}"));
        assertFalse(formattedContent.contains("{email}"));
        assertFalse(formattedContent.contains("{role}"));
        assertFalse(formattedContent.contains("{activationLink}"));
    }

    @Test
    public void testFormatMailTemplateContent_dollarInCodeBlock() {
        // 准备参数
        Map<String, Object> params = new HashMap<>();

        // 测试代码块中包含 $ 与 \\（历史上 appendReplacement 会抛 IllegalArgumentException: Illegal group reference）
        String content = "<pre><code>echo $HOME && ls \\root</code></pre>";

        // 调用，并断言内容原样保留
        String result = mailTemplateService.formatMailTemplateContent(content, params);
        assertTrue(result.contains("<div><code>"));
        assertEquals("echo $HOME && ls \\root", Jsoup.parseBodyFragment(result).selectFirst("div > code").wholeText());
    }

    @Test
    public void testFormatMailTemplateContent_codeWhitespace() {
        // 准备参数
        Map<String, Object> params = new HashMap<>();

        // 测试代码块中的换行、缩进被保留
        String content = "<pre><code class=\"language-java\">if (a) {\n    return \"$1\\n\";\n}\n</code></pre>";

        // 调用，并断言
        assertEquals("<div><code>if (a) {\n    return \"$1\\n\";\n}\n</code></div>",
                mailTemplateService.formatMailTemplateContent(content, params));
    }

    @Test
    public void testFormatMailTemplateContent_unclosedPreTags() {
        // 准备参数：大量未闭合的标签（历史上多次正则扫描会对每个 <pre> 反复扫描到末尾，耗时平方级增长）
        Map<String, Object> params = new HashMap<>();
        String content01 = "<pre>".repeat(20000) + "末尾 &amp; 内容";
        String content02 = "<pre><code class=\"a\">".repeat(20000) + "末尾";
        String content03 = "&lt;pre title=&quot;".repeat(20000);

        // 调用，并断言可终止，未闭合的标签按标准 HTML 自动修复，可见文字保留
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            String result01 = mailTemplateService.formatMailTemplateContent(content01, params);
            assertFalse(result01.contains("<pre"));
            assertEquals("末尾 & 内容", Jsoup.parseBodyFragment(result01).text());
            String result02 = mailTemplateService.formatMailTemplateContent(content02, params);
            assertFalse(result02.contains("<pre"));
            assertEquals("末尾", Jsoup.parseBodyFragment(result02).text());
            String result03 = mailTemplateService.formatMailTemplateContent(content03, params);
            assertEquals("<pre title=\"".repeat(20000), Jsoup.parseBodyFragment(result03).text());
        });
    }

    @Test
    public void testFormatMailTemplateContent_angleBracketInAttributes() {
        // 准备参数：pre、code 的单双引号属性中包含 &lt; &gt;
        Map<String, Object> params = new HashMap<>();
        String content = "<pre class=\"a &lt; b\" title='c &gt; d'><code class=\"x &gt; y\" data-a='&lt;z&gt;'>int a = 1;</code></pre>"
                + "<p>after</p>";

        // 调用，并断言属性被去掉，正文完整
        assertEquals("<div><code>int a = 1;</code></div><p>after</p>",
                mailTemplateService.formatMailTemplateContent(content, params));
    }

    @Test
    public void testFormatMailTemplateContent_closePreInCodeAndSpanAttributes() {
        // 准备参数：code 属性、代码内部 span 高亮标签属性中包含 &lt;/pre&gt;
        Map<String, Object> params = new HashMap<>();
        String content = "<pre><code class=\"language-html\" data-end=\"&lt;/pre&gt;\">"
                + "<span class=\"hljs-tag\" title=\"&lt;/pre&gt;\">&lt;/pre&gt;</span></code></pre>"
                + "<pre><code>second</code></pre>";

        // 调用，并断言 code 属性被去掉，span 及其中的文字保留
        String result = mailTemplateService.formatMailTemplateContent(content, params);
        assertFalse(result.contains("<pre"));
        Document document = Jsoup.parseBodyFragment(result);
        assertEquals(2, document.select("div > code").size());
        Element code = document.select("div > code").get(0);
        assertEquals(0, code.attributesSize());
        assertEquals("</pre>", code.selectFirst("span.hljs-tag").attr("title"));
        assertEquals("</pre>", code.selectFirst("span.hljs-tag").text());
        assertEquals("second", document.select("div > code").get(1).text());
    }

    @Test
    public void testFormatMailTemplateContent_sameQuoteEntityInQuotedAttributes() {
        // 准备参数：字面引号包住的属性中包含同类的 &quot; 或 &#39;
        Map<String, Object> params = new HashMap<>();
        String content = "<pre title=\"say &quot;&gt;&quot;\"><code class='it&#39;s &gt; ok'>first</code></pre>"
                + "<pre><code>second</code></pre>";

        // 调用，并断言后续代码块仍正常格式化
        assertEquals("<div><code>first</code></div><div><code>second</code></div>",
                mailTemplateService.formatMailTemplateContent(content, params));
    }

    @Test
    public void testFormatMailTemplateContent_unquotedAttributeStartsWithQuoteEntity() {
        // 准备参数：字面标签中，无引号属性值从 &quot; 或 &#39; 开始
        Map<String, Object> params = new HashMap<>();
        String content = "<pre title=&quot;a><code class=&#39;b>first</code></pre><pre><code>second</code></pre>";

        // 调用，并断言不会误吞后续代码块
        assertEquals("<div><code>first</code></div><div><code>second</code></div>",
                mailTemplateService.formatMailTemplateContent(content, params));
    }

    @Test
    public void testFormatMailTemplateContent_escapedMarkupAsText() {
        // 准备参数：被 HTML 实体转义（一次、两次）的 pre、code 标签，以及与字面标签混合的情况
        Map<String, Object> params = new HashMap<>();
        String escaped = "&lt;pre class=&quot;a &gt; b&quot;&gt;&lt;code&gt;x &amp;lt; y&lt;/code&gt;&lt;/pre&gt;";
        String doubleEscaped = "&amp;lt;pre&amp;gt;second&amp;lt;/pre&amp;gt;";
        String mixed01 = "<pre>&lt;code class=&quot;java&quot;&gt;x&lt;/code&gt;</pre>";
        String mixed02 = "&lt;pre&gt;<code class=\"java\">x</code>&lt;/pre&gt;";

        // 调用，并断言转义的标签按标准 HTML 显示为文字，不会变成标签
        Document document = Jsoup.parseBodyFragment(mailTemplateService.formatMailTemplateContent(escaped, params));
        assertTrue(document.body().children().isEmpty());
        assertEquals("<pre class=\"a > b\"><code>x &lt; y</code></pre>", document.body().text());
        document = Jsoup.parseBodyFragment(mailTemplateService.formatMailTemplateContent(doubleEscaped, params));
        assertTrue(document.body().children().isEmpty());
        assertEquals("&lt;pre&gt;second&lt;/pre&gt;", document.body().text());
        // 断言字面的 pre 仍替换为 div，字面的 code 按实际结构处理
        document = Jsoup.parseBodyFragment(mailTemplateService.formatMailTemplateContent(mixed01, params));
        assertEquals("<code class=\"java\">x</code>", document.selectFirst("body > div").wholeText());
        assertTrue(document.select("code").isEmpty());
        document = Jsoup.parseBodyFragment(mailTemplateService.formatMailTemplateContent(mixed02, params));
        assertEquals("<pre>x</pre>", document.body().text());
        assertEquals("java", document.selectFirst("body > code").attr("class"));
    }

    @Test
    public void testFormatMailTemplateContent_incompleteHtmlInCodeText() {
        // 准备参数：字面 code 正文中包含转义的不完整 HTML 示例，后面紧跟下一个代码块
        Map<String, Object> params = new HashMap<>();
        String content = "<pre><code>&lt;img title=&quot;未闭合 alt=&#39;</code></pre><pre><code>second</code></pre>";

        // 调用，并断言两段代码正文均保留为文字，不会吞掉本块或下一块
        String result = mailTemplateService.formatMailTemplateContent(content, params);
        Document document = Jsoup.parseBodyFragment(result);
        assertTrue(document.select("img").isEmpty());
        assertEquals(2, document.select("div > code").size());
        assertEquals("<img title=\"未闭合 alt='", document.select("div > code").get(0).wholeText());
        assertEquals("second", document.select("div > code").get(1).wholeText());
    }

    @Test
    public void testFormatMailTemplateContent_preWithoutAdjacentCode() {
        // 准备参数：没有 code 的 pre，以及 code 未紧贴 pre 的情况
        Map<String, Object> params = new HashMap<>();
        String content = "<pre class=\"x\">plain &amp; text</pre><pre> <code class=\"y\">z</code></pre>";

        // 调用，并断言 pre 替换为 div，内容保留
        assertEquals("<div>plain &amp; text</div><div> <code class=\"y\">z</code></div>",
                mailTemplateService.formatMailTemplateContent(content, params));
    }

}
