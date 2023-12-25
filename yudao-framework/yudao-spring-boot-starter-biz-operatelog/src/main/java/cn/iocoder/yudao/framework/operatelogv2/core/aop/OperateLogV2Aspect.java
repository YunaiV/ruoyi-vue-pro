package cn.iocoder.yudao.framework.operatelogv2.core.aop;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.monitor.TracerUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.system.api.logger.OperateLogApi;
import cn.iocoder.yudao.module.system.api.logger.dto.OperateLogV2CreateReqDTO;
import com.google.common.collect.Maps;
import com.mzt.logapi.beans.LogRecord;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.SUCCESS;
import static cn.iocoder.yudao.framework.operatelogv2.core.enums.OperateLogV2Constants.*;

/**
 * 拦截使用 @Operation 注解, 获取操作类型、开始时间、持续时间、方法相关信息、执行结果等信息
 * 对 mzt-biz-log 日志信息进行增强
 *
 * @author HUIHUI
 */
@Aspect
@Slf4j
public class OperateLogV2Aspect {

    /**
     * 用于记录操作内容的上下文
     *
     * @see OperateLogV2CreateReqDTO#getContent()
     */
    private static final ThreadLocal<LogRecord> CONTENT = new ThreadLocal<>();
    /**
     * 用于记录拓展字段的上下文
     *
     * @see OperateLogV2CreateReqDTO#getExtra()
     */
    private static final ThreadLocal<Map<String, Object>> EXTRA = new ThreadLocal<>();

    @Resource
    private OperateLogApi operateLogApi;

    @Around("@annotation(operation)")
    public Object around(ProceedingJoinPoint joinPoint, Operation operation) throws Throwable {
        RequestMethod requestMethod = obtainFirstMatchRequestMethod(obtainRequestMethod(joinPoint));
        if (requestMethod == RequestMethod.GET) { // 跳过 get 方法
            return joinPoint.proceed();
        }

        // 目前，只有管理员，才记录操作日志！所以非管理员，直接调用，不进行记录
        Integer userType = WebFrameworkUtils.getLoginUserType();
        if (ObjUtil.notEqual(userType, UserTypeEnum.ADMIN.getValue())) {
            return joinPoint.proceed();
        }

        // 记录开始时间
        LocalDateTime startTime = LocalDateTime.now();
        try {
            // 执行原有方法
            Object result = joinPoint.proceed();
            // 记录正常执行时的操作日志
            this.log(joinPoint, operation, startTime, result, null);
            return result;
        } catch (Throwable exception) {
            this.log(joinPoint, operation, startTime, null, exception);
            throw exception;
        } finally {
            clearThreadLocal();
        }
    }

    public static void setContent(LogRecord content) {
        CONTENT.set(content);
    }

    public static void addExtra(String key, Object value) {
        if (EXTRA.get() == null) {
            EXTRA.set(new HashMap<>());
        }
        EXTRA.get().put(key, value);
    }

    public static void addExtra(Map<String, Object> extra) {
        if (EXTRA.get() == null) {
            EXTRA.set(new HashMap<>());
        }
        EXTRA.get().putAll(extra);
    }

    private static void clearThreadLocal() {
        CONTENT.remove();
        EXTRA.remove();
    }

    private void log(ProceedingJoinPoint joinPoint, Operation operation,
                     LocalDateTime startTime, Object result, Throwable exception) {
        try {
            // 判断不记录的情况(默认没有值就是记录)
            if (EXTRA.get() != null && EXTRA.get().get(ENABLE) != null) {
                return;
            }
            if (CONTENT.get() == null) { // 没有值说明没有日志需要记录
                return;
            }

            // 真正记录操作日志
            this.log0(joinPoint, operation, startTime, result, exception);
        } catch (Throwable ex) {
            log.error("[log][记录操作日志时，发生异常，其中参数是 joinPoint({}) apiOperation({}) result({}) exception({}) ]",
                    joinPoint, operation, result, exception, ex);
        }
    }

    private void log0(ProceedingJoinPoint joinPoint, Operation operation,
                      LocalDateTime startTime, Object result, Throwable exception) {
        OperateLogV2CreateReqDTO reqDTO = new OperateLogV2CreateReqDTO();
        // 补全通用字段
        reqDTO.setTraceId(TracerUtils.getTraceId());
        reqDTO.setStartTime(startTime);
        // 补充用户信息
        fillUserFields(reqDTO);
        // 补全模块信息
        fillModuleFields(reqDTO, operation);
        // 补全请求信息
        fillRequestFields(reqDTO);
        // 补全方法信息
        fillMethodFields(reqDTO, joinPoint, startTime, result, exception);

        // 异步记录日志
        operateLogApi.createOperateLogV2(reqDTO);
    }

    private static void fillUserFields(OperateLogV2CreateReqDTO reqDTO) {
        reqDTO.setUserId(WebFrameworkUtils.getLoginUserId());
        reqDTO.setUserType(WebFrameworkUtils.getLoginUserType());
    }

    private static void fillModuleFields(OperateLogV2CreateReqDTO reqDTO, Operation operation) {
        LogRecord logRecord = CONTENT.get();
        reqDTO.setBizId(Long.parseLong(logRecord.getBizNo())); // 操作模块业务编号
        reqDTO.setContent(logRecord.getAction());// 例如说，修改编号为 1 的用户信息，将性别从男改成女，将姓名从芋道改成源码。

        // type 属性
        reqDTO.setType(logRecord.getType()); // 大模块类型如 crm 客户
        // subType 属性
        if (logRecord.getSubType() != null) {
            reqDTO.setSubType(logRecord.getSubType());// 操作名称如 转移客户
        }
        if (StrUtil.isEmpty(reqDTO.getSubType()) && operation != null) {
            reqDTO.setSubType(operation.summary());
        }

        // 拓展字段，有些复杂的业务，需要记录一些字段 ( JSON 格式 )，例如说，记录订单编号，{ orderId: "1"}
        Map<String, Object> objectMap = EXTRA.get();
        if (objectMap != null) {
            Object object = objectMap.get(EXTRA_KEY);
            if (object instanceof Map<?, ?> extraMap) {
                if (extraMap.keySet().stream().allMatch(String.class::isInstance)) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> extra = (Map<String, Object>) extraMap;
                    reqDTO.setExtra(extra);
                    return;
                }
            }
            // 激进一点不是 map 直接当 value 处理
            Map<String, Object> extra = Maps.newHashMapWithExpectedSize(1);
            extra.put(EXTRA_KEY, object);
            reqDTO.setExtra(extra);
        }

    }

    private static void fillRequestFields(OperateLogV2CreateReqDTO reqDTO) {
        // 获得 Request 对象
        HttpServletRequest request = ServletUtils.getRequest();
        if (request == null) {
            return;
        }
        // 补全请求信息
        reqDTO.setRequestMethod(request.getMethod());
        reqDTO.setRequestUrl(request.getRequestURI());
        reqDTO.setUserIp(ServletUtils.getClientIP(request));
        reqDTO.setUserAgent(ServletUtils.getUserAgent(request));
    }

    private static void fillMethodFields(OperateLogV2CreateReqDTO reqDTO, ProceedingJoinPoint joinPoint, LocalDateTime startTime,
                                         Object result, Throwable exception) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        reqDTO.setJavaMethod(methodSignature.toString());
        if (EXTRA.get().get(IS_LOG_ARGS) == null) {
            reqDTO.setJavaMethodArgs(obtainMethodArgs(joinPoint));
        }
        if (EXTRA.get().get(IS_LOG_RESULT_DATA) == null) {
            reqDTO.setResultData(obtainResultData(result));
        }
        reqDTO.setDuration((int) (LocalDateTimeUtil.between(startTime, LocalDateTime.now()).toMillis()));
        // （正常）处理 resultCode 和 resultMsg 字段
        if (result instanceof CommonResult) {
            CommonResult<?> commonResult = (CommonResult<?>) result;
            reqDTO.setResultCode(commonResult.getCode());
            reqDTO.setResultMsg(commonResult.getMsg());
        } else {
            reqDTO.setResultCode(SUCCESS.getCode());
        }
        // （异常）处理 resultCode 和 resultMsg 字段
        if (exception != null) {
            reqDTO.setResultCode(INTERNAL_SERVER_ERROR.getCode());
            reqDTO.setResultMsg(ExceptionUtil.getRootCauseMessage(exception));
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static <T extends Annotation> T getClassAnnotation(ProceedingJoinPoint joinPoint, Class<T> annotationClass) {
        return ((MethodSignature) joinPoint.getSignature()).getMethod().getDeclaringClass().getAnnotation(annotationClass);
    }

    private static String obtainMethodArgs(ProceedingJoinPoint joinPoint) {
        // TODO 提升：参数脱敏和忽略
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String[] argNames = methodSignature.getParameterNames();
        Object[] argValues = joinPoint.getArgs();
        // 拼接参数
        Map<String, Object> args = Maps.newHashMapWithExpectedSize(argValues.length);
        for (int i = 0; i < argNames.length; i++) {
            String argName = argNames[i];
            Object argValue = argValues[i];
            // 被忽略时，标记为 ignore 字符串，避免和 null 混在一起
            args.put(argName, !isIgnoreArgs(argValue) ? argValue : "[ignore]");
        }
        return JsonUtils.toJsonString(args);
    }

    private static String obtainResultData(Object result) {
        // TODO 提升：结果脱敏和忽略
        if (result instanceof CommonResult) {
            result = ((CommonResult<?>) result).getData();
        }
        return JsonUtils.toJsonString(result);
    }

    private static boolean isIgnoreArgs(Object object) {
        Class<?> clazz = object.getClass();
        // 处理数组的情况
        if (clazz.isArray()) {
            return IntStream.range(0, Array.getLength(object))
                    .anyMatch(index -> isIgnoreArgs(Array.get(object, index)));
        }
        // 递归，处理数组、Collection、Map 的情况
        if (Collection.class.isAssignableFrom(clazz)) {
            return ((Collection<?>) object).stream()
                    .anyMatch((Predicate<Object>) OperateLogV2Aspect::isIgnoreArgs);
        }
        if (Map.class.isAssignableFrom(clazz)) {
            return isIgnoreArgs(((Map<?, ?>) object).values());
        }
        // obj
        return object instanceof MultipartFile
                || object instanceof HttpServletRequest
                || object instanceof HttpServletResponse
                || object instanceof BindingResult;
    }

    private static RequestMethod obtainFirstMatchRequestMethod(RequestMethod[] requestMethods) {
        if (ArrayUtil.isEmpty(requestMethods)) {
            return null;
        }
        // 优先，匹配最优的 POST、PUT、DELETE
        RequestMethod result = obtainFirstLogRequestMethod(requestMethods);
        if (result != null) {
            return result;
        }
        // 然后，匹配次优的 GET
        result = Arrays.stream(requestMethods).filter(requestMethod -> requestMethod == RequestMethod.GET)
                .findFirst().orElse(null);
        if (result != null) {
            return result;
        }
        // 兜底，获得第一个
        return requestMethods[0];
    }

    private static RequestMethod[] obtainRequestMethod(ProceedingJoinPoint joinPoint) {
        RequestMapping requestMapping = AnnotationUtils.getAnnotation( // 使用 Spring 的工具类，可以处理 @RequestMapping 别名注解
                ((MethodSignature) joinPoint.getSignature()).getMethod(), RequestMapping.class);
        return requestMapping != null ? requestMapping.method() : new RequestMethod[]{};
    }

    private static RequestMethod obtainFirstLogRequestMethod(RequestMethod[] requestMethods) {
        if (ArrayUtil.isEmpty(requestMethods)) {
            return null;
        }
        return Arrays.stream(requestMethods).filter(requestMethod ->
                        requestMethod == RequestMethod.POST
                                || requestMethod == RequestMethod.PUT
                                || requestMethod == RequestMethod.DELETE)
                .findFirst().orElse(null);
    }

}
