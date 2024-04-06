package cn.iocoder.yudao.framework.ai.midjourney.util;

import cn.hutool.core.text.CharSequenceUtil;
import cn.iocoder.yudao.framework.ai.midjourney.MjMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * author: fansili
 * time: 2024/4/6 19:00
 */
public class MjUtil {
    /**
     * content正则匹配prompt和进度.
     */
    public static final String CONTENT_REGEX = ".*?\\*\\*(.*?)\\*\\*.+<@\\d+> \\((.*?)\\)";
    public static final String CONTENT_PROGRESS_REGEX =  "\\(([^)]*)\\)";


    public static MjMessage.Content parseContent(String content) {
        // "**南极应该是什么样子？ --v 6.0 --style raw** - <@972721304891453450> (32%) (fast, stealth)",
        //  "**南极应该是什么样子？ --v 6.0 --style raw** - <@972721304891453450> (fast, stealth)"
        MjMessage.Content mjContent = new MjMessage.Content();
        if (CharSequenceUtil.isBlank(content)) {
            return null;
        }
        if (!content.contains("<@")) {
            return mjContent.setPrompt(content);
        }
        int rawIndex = content.indexOf("<@") - 3;
        String prompt = content.substring(0, rawIndex).trim();
        String contentTail = content.substring(rawIndex).trim();
        // 检查是否存在进度条
        Pattern pattern = Pattern.compile(CONTENT_PROGRESS_REGEX);
        Matcher matcher = pattern.matcher(contentTail);

        if (contentTail.contains("%")) {
            if (matcher.find()) {
                // 获取第一个（也是此处唯一的）捕获组的内容
                String progress = matcher.group(1);
                mjContent.setProgress(progress);
            }
            if (matcher.find()) {
                String status = matcher.group(1);
                mjContent.setStatus(status);
            }
        } else {
            if (matcher.find()) {
                // 获取第一个（也是此处唯一的）捕获组的内容
                String status = matcher.group(1);
                mjContent.setStatus(status);
            }
        }
        mjContent.setPrompt(prompt);
        // tip：contentArray
        return mjContent;
    }

    public static void main(String[] args) {
        String content1 = "**南极应该是什么样子？ --v 6.0 --style raw** - <@972721304891453450> (32%) (fast, stealth)";
        String content2 = "**南极应该是什么样子？ --v 6.0 --style raw** - <@972721304891453450> (fast, stealth)";

        System.err.println(parseContent(content1));;
        System.err.println(parseContent(content2));;
    }

}
