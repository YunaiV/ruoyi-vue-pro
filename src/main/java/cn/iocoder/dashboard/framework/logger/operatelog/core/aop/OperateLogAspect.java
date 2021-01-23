package cn.iocoder.dashboard.framework.logger.operatelog.core.aop;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.framework.logger.operatelog.core.annotations.OperateLog;
import cn.iocoder.dashboard.framework.logger.operatelog.core.enums.OperateTypeEnum;
import cn.iocoder.dashboard.framework.logger.operatelog.core.service.OperateLogFrameworkService;
import cn.iocoder.dashboard.framework.security.core.util.SecurityUtils;
import cn.iocoder.dashboard.framework.tracer.core.util.TracerUtils;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.operatelog.SysOperateLogCreateReqVO;
import cn.iocoder.dashboard.util.servlet.ServletUtils;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static cn.iocoder.dashboard.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;
import static cn.iocoder.dashboard.common.exception.enums.GlobalErrorCodeConstants.SUCCESS;

/**
 * 拦截使用 @OperateLog 注解，如果满足条件，则生成操作日志。
 * 满足如下任一条件，则会进行记录：
 * 1. 使用 @ApiOperation + 非 @GetMapping
 * 2. 使用 @OperateLog 注解
 *
 * 但是，如果声明 @OperateLog 注解时，将 enable 属性设置为 false 时，强制不记录。
 *
 * @author 芋道源码
 */
@Aspect
@Slf4j
public class OperateLogAspect {

    /**
     * 用于记录操作内容的上下文
     *
     * @see SysOperateLogCreateReqVO#getContent()
     */
    private static final ThreadLocal<String> CONTENT = new ThreadLocal<>();
    /**
     * 用于记录拓展字段的上下文
     *
     * @see SysOperateLogCreateReqVO#getExts()
     */
    private static final ThreadLocal<Map<String, Object>> EXTS = new ThreadLocal<>();

    @Resource
    private OperateLogFrameworkService operateLogFrameworkService;

    @Around("@annotation(apiOperation)")
    public Object around(ProceedingJoinPoint joinPoint, ApiOperation apiOperation) throws Throwable {
        // 可能也添加了 @ApiOperation 注解
        OperateLog operateLog = getMethodAnnotation(joinPoint, OperateLog.class);
        return around0(joinPoint, operateLog, apiOperation);
    }

    @Around("!@annotation(io.swagger.annotations.ApiOperation) && @annotation(operateLog)") // 兼容处理，只添加 @OperateLog 注解的情况
    public Object around(ProceedingJoinPoint joinPoint, OperateLog operateLog) throws Throwable {
        return around0(joinPoint, operateLog, null);
    }

    private Object around0(ProceedingJoinPoint joinPoint, OperateLog operateLog, ApiOperation apiOperation) throws Throwable {
        // 记录开始时间
        Date startTime = new Date();
        try {
            // 执行原有方法
            Object result = joinPoint.proceed();
            // 记录正常执行时的操作日志
            this.log(joinPoint, operateLog, apiOperation, startTime, result, null);
            return result;
        } catch (Throwable exception) {
            this.log(joinPoint, operateLog, apiOperation, startTime, null, exception);
            throw exception;
        } finally {
            clearThreadLocal();
        }
    }

    public static void setContent(String content) {
        CONTENT.set(content);
    }

    public static void addExt(String key, Object value) {
        if (EXTS.get() == null) {
            EXTS.set(new HashMap<>());
        }
        EXTS.get().put(key, value);
    }

    private static void clearThreadLocal() {
        CONTENT.remove();
        EXTS.remove();
    }

    private void log(ProceedingJoinPoint joinPoint, OperateLog operateLog, ApiOperation apiOperation,
                     Date startTime, Object result, Throwable exception) {
        try {
            // 判断不记录的情况
            if (!isLogEnable(joinPoint, operateLog)) {
                return;
            }
            // 真正记录操作日志
            this.log0(joinPoint, operateLog, apiOperation, startTime, result, exception);
        } catch (Throwable ex) {
            log.error("[log][记录操作日志时，发生异常，其中参数是 joinPoint({}) operateLog({}) apiOperation({}) result({}) exception({}) ]",
                    joinPoint, operateLog, apiOperation, result, exception, ex);
        }
    }

    private void log0(ProceedingJoinPoint joinPoint, OperateLog operateLog, ApiOperation apiOperation,
                      Date startTime, Object result, Throwable exception) {
        SysOperateLogCreateReqVO operateLogVO = new SysOperateLogCreateReqVO();
        // 补全通用字段
        operateLogVO.setTraceId(TracerUtils.getTraceId());
        operateLogVO.setStartTime(startTime);
        // 补充用户信息
        fillUserFields(operateLogVO);
        // 补全模块信息
        fillModuleFields(operateLogVO, joinPoint, operateLog, apiOperation);
        // 补全请求信息
        fillRequestFields(operateLogVO);
        // 补全方法信息
        fillMethodFields(operateLogVO, joinPoint, operateLog, startTime, result, exception);

        // 异步记录日志
        operateLogFrameworkService.createOperateLogAsync(operateLogVO);
    }

    private static void fillUserFields(SysOperateLogCreateReqVO operateLogVO) {
        operateLogVO.setUserId(SecurityUtils.getLoginUserId());
    }

    private static void fillModuleFields(SysOperateLogCreateReqVO operateLogVO,
                                         ProceedingJoinPoint joinPoint, OperateLog operateLog, ApiOperation apiOperation) {
        // module 属性
        if (operateLog != null) {
            operateLogVO.setModule(operateLog.module());
        }
        if (StrUtil.isEmpty(operateLogVO.getModule())) {
            Api api = getClassAnnotation(joinPoint, Api.class);
            if (api != null) {
                // 优先读取 @API 的 name 属性
                if (StrUtil.isNotEmpty(api.value())) {
                    operateLogVO.setModule(api.value());
                }
                // 没有的话，读取 @API 的 tags 属性
                if (StrUtil.isEmpty(operateLogVO.getModule()) && ArrayUtil.isNotEmpty(api.tags())) {
                    operateLogVO.setModule(api.tags()[0]);
                }
            }
        }
        // name 属性
        if (operateLog != null) {
            operateLogVO.setName(operateLog.name());
        }
        if (StrUtil.isEmpty(operateLogVO.getName()) && apiOperation != null) {
            operateLogVO.setName(apiOperation.value());
        }
        // type 属性
        if (operateLog != null && ArrayUtil.isNotEmpty(operateLog.type())) {
            operateLogVO.setType(operateLog.type()[0].getType());
        }
        if (operateLogVO.getType() == null) {
            RequestMethod requestMethod = obtainFirstMatchRequestMethod(obtainRequestMethod(joinPoint));
            OperateTypeEnum operateLogType = convertOperateLogType(requestMethod);
            operateLogVO.setType(operateLogType != null ? operateLogType.getType() : null);
        }
        // content 和 exts 属性
        operateLogVO.setContent(CONTENT.get());
        operateLogVO.setExts(EXTS.get());
    }

    private static void fillRequestFields(SysOperateLogCreateReqVO operateLogVO) {
        // 获得 Request 对象
        HttpServletRequest request = ServletUtils.getRequest();
        if (request == null) {
            return;
        }
        // 补全请求信息
        operateLogVO.setRequestMethod(request.getMethod());
        operateLogVO.setRequestUrl(request.getRequestURI());
        operateLogVO.setUserIp(ServletUtil.getClientIP(request));
        operateLogVO.setUserAgent(ServletUtils.getUserAgent(request));
    }

    private static void fillMethodFields(SysOperateLogCreateReqVO operateLogVO,
                                         ProceedingJoinPoint joinPoint, OperateLog operateLog,
                                         Date startTime, Object result, Throwable exception) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        operateLogVO.setJavaMethod(methodSignature.toString());
        if (operateLog == null || operateLog.logArgs()) {
            operateLogVO.setJavaMethodArgs(obtainMethodArgs(joinPoint));
        }
        if (operateLog == null || operateLog.logResultData()) {
            operateLogVO.setResultData(obtainResultData(result));
        }
        operateLogVO.setDuration((int) (System.currentTimeMillis() - startTime.getTime()));
        // （正常）处理 resultCode 和 resultMsg 字段
        if (result != null) {
            if (result instanceof CommonResult) {
                CommonResult<?> commonResult = (CommonResult<?>) result;
                operateLogVO.setResultCode(commonResult.getCode());
                operateLogVO.setResultMsg(commonResult.getMsg());
            } else {
                operateLogVO.setResultCode(SUCCESS.getCode());
            }
        }
        // （异常）处理 resultCode 和 resultMsg 字段
        if (exception != null) {
            operateLogVO.setResultCode(INTERNAL_SERVER_ERROR.getCode());
            operateLogVO.setResultMsg(ExceptionUtil.getRootCauseMessage(exception));
        }
    }

    private static boolean isLogEnable(ProceedingJoinPoint joinPoint, OperateLog operateLog) {
        // 有 @OperateLog 注解的情况下
        if (operateLog != null) {
            return operateLog.enable();
        }
        // 没有 @ApiOperation 注解的情况下，只记录 POST、PUT、DELETE 的情况
        return obtainFirstLogRequestMethod(obtainRequestMethod(joinPoint)) != null;
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

    private static OperateTypeEnum convertOperateLogType(RequestMethod requestMethod) {
        if (requestMethod == null) {
            return null;
        }
        switch (requestMethod) {
            case GET:
                return OperateTypeEnum.GET;
            case POST:
                return OperateTypeEnum.CREATE;
            case PUT:
                return OperateTypeEnum.UPDATE;
            case DELETE:
                return OperateTypeEnum.DELETE;
            default:
                return OperateTypeEnum.OTHER;
        }
    }

    private static RequestMethod[] obtainRequestMethod(ProceedingJoinPoint joinPoint) {
        RequestMapping requestMapping = AnnotationUtils.getAnnotation( // 使用 Spring 的工具类，可以处理 @RequestMapping 别名注解
                ((MethodSignature) joinPoint.getSignature()).getMethod(), RequestMapping.class);
        return requestMapping != null ? requestMapping.method() : new RequestMethod[]{};
    }

    @SuppressWarnings("SameParameterValue")
    private static <T extends Annotation> T getMethodAnnotation(ProceedingJoinPoint joinPoint, Class<T> annotationClass) {
        return ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(annotationClass);
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
        return JSON.toJSONString(args);
    }

    private static String obtainResultData(Object result) {
        // TODO 提升：结果脱敏和忽略
        if (result instanceof CommonResult) {
            result = ((CommonResult<?>) result).getData();
        }
        return JSON.toJSONString(result);
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
                    .anyMatch((Predicate<Object>) o -> isIgnoreArgs(object));
        }
        if (Map.class.isAssignableFrom(clazz)) {
            return isIgnoreArgs(((Map<?, ?>) object).values());
        }
        // obj
        return object instanceof MultipartFile
                || object instanceof HttpServletRequest
                || object instanceof HttpServletResponse;
    }

}
