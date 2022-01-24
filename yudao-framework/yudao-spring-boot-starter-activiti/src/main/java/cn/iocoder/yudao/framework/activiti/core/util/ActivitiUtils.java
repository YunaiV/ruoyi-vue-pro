package cn.iocoder.yudao.framework.activiti.core.util;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import com.alibaba.ttl.TransmittableThreadLocal;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.util.io.BytesStreamSource;

import javax.xml.bind.Element;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

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

    public static boolean equals(String userIdStr, Long userId) {
        return Objects.equals(userId, NumberUtils.parseLong(userIdStr));
    }

    // ========== BPMN XML 相关 ==========

    /**
     * 构建对应的 BPMN Model
     *
     * @param bpmnBytes 原始的 BPMN XML 字节数组
     * @return BPMN Model
     */
    public static BpmnModel buildBpmnModel(byte[] bpmnBytes) {
        // 转换成 BpmnModel 对象
        BpmnXMLConverter converter = new BpmnXMLConverter();
        return converter.convertToBpmnModel(new BytesStreamSource(bpmnBytes), true, true);
    }

    /**
     * 获得 BPMN 流程中，指定的元素们
     *
     * @param model
     * @param clazz 指定元素。例如说，{@link org.activiti.bpmn.model.UserTask}、{@link org.activiti.bpmn.model.Gateway} 等等
     * @return 元素们
     */
    public static <T extends FlowElement> List<T> getBpmnModelElements(BpmnModel model, Class<T> clazz) {
        List<T> result = new ArrayList<>();
        model.getProcesses().forEach(process -> {
            process.getFlowElements().forEach(flowElement -> {
                if (flowElement.getClass().isAssignableFrom(clazz)) {
                    result.add((T) flowElement);
                }
            });
        });
        return result;
    }

    public static String getBpmnXml(BpmnModel model) {
        if (model == null) {
            return null;
        }
        return StrUtil.utf8Str(getBpmnBytes(model));
    }

    public static byte[] getBpmnBytes(BpmnModel model) {
        if (model == null) {
            return new byte[0];
        }
        BpmnXMLConverter converter = new BpmnXMLConverter();
        return converter.convertToXML(model);
    }

    public static boolean equals(BpmnModel oldModel, BpmnModel newModel) {
        // 由于 BpmnModel 未提供 equals 方法，所以只能转成字节数组，进行比较
        return Arrays.equals(getBpmnBytes(oldModel), getBpmnBytes(newModel));
    }

}
