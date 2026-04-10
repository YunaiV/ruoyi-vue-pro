package cn.iocoder.yudao.module.system.service.mail;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplatePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplateSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailTemplateDO;
import cn.iocoder.yudao.module.system.dal.mysql.mail.MailTemplateMapper;
import cn.iocoder.yudao.module.system.dal.redis.RedisKeyConstants;
import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.MAIL_TEMPLATE_CODE_EXISTS;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.MAIL_TEMPLATE_NOT_EXISTS;

/**
 * 邮箱模版 Service 实现类
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
@Service
@Validated
@Slf4j
public class MailTemplateServiceImpl implements MailTemplateService {

    /**
     * 正则表达式，匹配 {} 中的变量
     */
    private static final Pattern PATTERN_PARAMS = Pattern.compile("\\{(.*?)}");

    @Resource
    private MailTemplateMapper mailTemplateMapper;

    @Override
    public Long createMailTemplate(MailTemplateSaveReqVO createReqVO) {
        // 校验 code 是否唯一
        validateCodeUnique(null, createReqVO.getCode());

        // 插入
        MailTemplateDO template = BeanUtils.toBean(createReqVO, MailTemplateDO.class)
                .setParams(parseTemplateTitleAndContentParams(createReqVO.getTitle(), createReqVO.getContent()));
        mailTemplateMapper.insert(template);
        return template.getId();
    }

    @Override
    @CacheEvict(cacheNames = RedisKeyConstants.MAIL_TEMPLATE,
            allEntries = true) // allEntries 清空所有缓存，因为可能修改到 code 字段，不好清理
    public void updateMailTemplate(@Valid MailTemplateSaveReqVO updateReqVO) {
        // 校验是否存在
        validateMailTemplateExists(updateReqVO.getId());
        // 校验 code 是否唯一
        validateCodeUnique(updateReqVO.getId(),updateReqVO.getCode());

        // 更新
        MailTemplateDO updateObj = BeanUtils.toBean(updateReqVO, MailTemplateDO.class)
                .setParams(parseTemplateTitleAndContentParams(updateReqVO.getTitle(), updateReqVO.getContent()));
        mailTemplateMapper.updateById(updateObj);
    }

    @VisibleForTesting
    void validateCodeUnique(Long id, String code) {
        MailTemplateDO template = mailTemplateMapper.selectByCode(code);
        if (template == null) {
            return;
        }
        // 存在 template 记录的情况下
        if (id == null // 新增时，说明重复
                || ObjUtil.notEqual(id, template.getId())) { // 更新时，如果 id 不一致，说明重复
            throw exception(MAIL_TEMPLATE_CODE_EXISTS);
        }
    }

    @Override
    @CacheEvict(cacheNames = RedisKeyConstants.MAIL_TEMPLATE,
            allEntries = true) // allEntries 清空所有缓存，因为 id 不是直接的缓存 code，不好清理
    public void deleteMailTemplate(Long id) {
        // 校验是否存在
        validateMailTemplateExists(id);

        // 删除
        mailTemplateMapper.deleteById(id);
    }

    @Override
    @CacheEvict(cacheNames = RedisKeyConstants.MAIL_TEMPLATE,
            allEntries = true) // allEntries 清空所有缓存，因为 id 不是直接的缓存 code，不好清理
    public void deleteMailTemplateList(List<Long> ids) {
        mailTemplateMapper.deleteByIds(ids);
    }

    private void validateMailTemplateExists(Long id) {
        if (mailTemplateMapper.selectById(id) == null) {
            throw exception(MAIL_TEMPLATE_NOT_EXISTS);
        }
    }

    @Override
    public MailTemplateDO getMailTemplate(Long id) {return mailTemplateMapper.selectById(id);}

    @Override
    @Cacheable(value = RedisKeyConstants.MAIL_TEMPLATE, key = "#code", unless = "#result == null")
    public MailTemplateDO getMailTemplateByCodeFromCache(String code) {
        return mailTemplateMapper.selectByCode(code);
    }

    @Override
    public PageResult<MailTemplateDO> getMailTemplatePage(MailTemplatePageReqVO pageReqVO) {
        return mailTemplateMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MailTemplateDO> getMailTemplateList() {return mailTemplateMapper.selectList();}

    @Override
    public String formatMailTemplateContent(String content, Map<String, Object> params) {
        // 1. 先替换模板变量
        String formattedContent = StrUtil.format(content, params);

        // 关联 Pull Request：https://gitee.com/zhijiantianya/ruoyi-vue-pro/pulls/1461 讨论
        // 2.1 反转义HTML特殊字符
        formattedContent = unescapeHtml(formattedContent);
        // 2.2 处理代码块（确保<pre><code>标签格式正确）
        formattedContent = formatHtmlCodeBlocks(formattedContent);
        // 2.3 将最外层的 pre 标签替换为 div 标签
        formattedContent = replaceOuterPreWithDiv(formattedContent);
        return formattedContent;
    }

    private String replaceOuterPreWithDiv(String content) {
        if (StrUtil.isEmpty(content)) {
            return content;
        }
        // 使用正则表达式匹配所有的 <pre> 标签，包括嵌套的 <code> 标签
        String regex = "(?s)<pre[^>]*>(.*?)</pre>";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            // 提取 <pre> 标签内的内容
            String innerContent = matcher.group(1);
            // 返回 div 标签包裹的内容
            matcher.appendReplacement(sb, "<div>" + innerContent + "</div>");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 反转义 HTML 特殊字符
     *
     * @param input 输入字符串
     * @return 反转义后的字符串
     */
    private String unescapeHtml(String input) {
        if (StrUtil.isEmpty(input)) {
            return input;
        }
        return input
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&nbsp;", " ");
    }

    /**
     * 格式化 HTML 中的代码块
     *
     * @param content 邮件内容
     * @return 格式化后的邮件内容
     */
    private String formatHtmlCodeBlocks(String content) {
        // 匹配 <pre><code> 标签的代码块
        Pattern codeBlockPattern = Pattern.compile("<pre\\s*.*?><code\\s*.*?>(.*?)</code></pre>", Pattern.DOTALL);
        Matcher matcher = codeBlockPattern.matcher(content);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            // 获取代码块内容
            String codeBlock = matcher.group(1);
            // 为代码块添加样式
            String replacement = "<pre style=\"background-color: #f5f5f5; padding: 10px; border-radius: 5px; overflow-x: auto;\"><code>" + codeBlock + "</code></pre>";
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    @Override
    public long getMailTemplateCountByAccountId(Long accountId) {
        return mailTemplateMapper.selectCountByAccountId(accountId);
    }

    /**
     * 解析标题和内容中的参数
     */
    @VisibleForTesting
    public List<String> parseTemplateTitleAndContentParams(String title, String content) {
        List<String> titleParams = ReUtil.findAllGroup1(PATTERN_PARAMS, title);
        List<String> contentParams = ReUtil.findAllGroup1(PATTERN_PARAMS, content);
        // 合并参数并去重
        List<String> allParams = new ArrayList<>(titleParams);
        for (String param : contentParams) {
            if (!allParams.contains(param)) {
                allParams.add(param);
            }
        }
        return allParams;
    }

    /**
     * 获得邮件模板中的参数，形如 {key}
     *
     * @param content 内容
     * @return 参数列表
     */
    List<String> parseTemplateContentParams(String content) {
        return ReUtil.findAllGroup1(PATTERN_PARAMS, content);
    }

}