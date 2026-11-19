package cn.iocoder.yudao.module.report.framework.jmreport.core.service;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.modules.jmreport.ai.service.IJimuAiAssistantService;
import org.jeecg.modules.jmreport.common.expetion.JimuReportException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * {@link IJimuAiAssistantService} 的「未配置」实现，用于积木报表内置 AI 助手不可用的场景
 *
 * 行为与积木自带实现在未配置 AI 时一致：{@link #isConfigured()} 返回 false，调用 AI 的方法抛出 {@link JimuReportException}。
 * 普通报表、大屏不依赖它，不受影响。
 *
 * @author 芋道源码
 */
public class JmReportAiAssistantDisabledServiceImpl implements IJimuAiAssistantService {

    static final String DISABLED_MESSAGE = "积木报表内置 AI 助手不可用：它依赖的 Spring AI 1.x 与当前项目的 Spring AI 版本不兼容";

    @Override
    public boolean isConfigured() {
        return false;
    }

    @Override
    public JSONObject callAiService(String type, String content, String ddl, String extra) {
        throw new JimuReportException(DISABLED_MESSAGE);
    }

    @Override
    public String callAi(String systemText, String userText) {
        throw new JimuReportException(DISABLED_MESSAGE);
    }

    @Override
    public SseEmitter routeStream(String type, Map<String, Object> params) {
        throw new JimuReportException(DISABLED_MESSAGE);
    }

}
