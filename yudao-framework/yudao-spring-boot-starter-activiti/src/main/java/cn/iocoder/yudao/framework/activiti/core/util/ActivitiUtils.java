package cn.iocoder.yudao.framework.activiti.core.util;

import cn.hutool.core.util.ReflectUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceEntityImpl;
import org.activiti.engine.impl.persistence.entity.HistoricScopeInstanceEntityImpl;
import org.activiti.engine.impl.util.io.StringStreamSource;

/**
 * Activiti 工具类
 *
 * @author 芋道源码
 */
public class ActivitiUtils {

    static {
        setAuthenticationThreadLocal();
    }

    // ========== Authentication 相关 ==========

    /**
     * 反射修改 Authentication 的 authenticatedUserIdThreadLocal 静态变量，使用 TTL 线程变量
     * 目的：保证 @Async 等异步执行时，变量丢失的问题
     */
    private static void setAuthenticationThreadLocal() {
        ReflectUtil.setFieldValue(Authentication.class, "authenticatedUserIdThreadLocal",
                new TransmittableThreadLocal<String>());
    }

    public static void setAuthenticatedUserId(Long userId) {
        Authentication.setAuthenticatedUserId(String.valueOf(userId));
    }

    public static void clearAuthenticatedUserId() {
        Authentication.setAuthenticatedUserId(null);
    }

    // ========== BPMN XML 相关 ==========

    /**
     * 替换 BPMN XML 主流程的 id 和 name 属性
     *
     * @param bpmnXml 原始的 BPMN XML 字符串
     * @param id 编号，对应到 XML 实际是 key 属性
     * @param name 名字
     * @return 新的 BPMN XML 的字节数组
     */
    public static byte[] replaceBpmnMainProcessIdAndName(String bpmnXml, String id, String name) {
        // 转换成 BpmnModel 对象
        BpmnXMLConverter converter = new BpmnXMLConverter();
        BpmnModel bpmnModel = converter.convertToBpmnModel(new StringStreamSource(bpmnXml), true, true);
        // 设置 id 和 name 属性
        bpmnModel.getMainProcess().setId(id);
        bpmnModel.getMainProcess().setName(name);
        // 转换回字节数组
        return converter.convertToXML(bpmnModel);
    }

}
