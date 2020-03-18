package com.ruoyi.framework.interceptor.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSONObject;
import com.ruoyi.common.filter.RepeatedlyRequestWrapper;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.http.HttpHelper;
import com.ruoyi.framework.interceptor.RepeatSubmitInterceptor;
import com.ruoyi.framework.redis.RedisCache;

/**
 * 判断请求url和数据是否和上一次相同，
 * 如果和上次相同，则是重复提交表单。 有效时间为10秒内。
 * 
 * @author ruoyi
 */
@Component
public class SameUrlDataInterceptor extends RepeatSubmitInterceptor
{
    public final String REPEAT_PARAMS = "repeatParams";

    public final String REPEAT_TIME = "repeatTime";

    public final String CACHE_REPEAT_KEY = "repeatData";

    @Autowired
    private RedisCache redisCache;

    /**
     * 间隔时间，单位:秒 默认10秒
     * 
     * 两次相同参数的请求，如果间隔时间大于该参数，系统不会认定为重复提交的数据
     */
    private int intervalTime = 10;

    public void setIntervalTime(int intervalTime)
    {
        this.intervalTime = intervalTime;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isRepeatSubmit(HttpServletRequest request)
    {
        RepeatedlyRequestWrapper repeatedlyRequest = (RepeatedlyRequestWrapper) request;
        String nowParams = HttpHelper.getBodyString(repeatedlyRequest);

        // body参数为空，获取Parameter的数据
        if (StringUtils.isEmpty(nowParams))
        {
            nowParams = JSONObject.toJSONString(request.getParameterMap());
        }
        Map<String, Object> nowDataMap = new HashMap<String, Object>();
        nowDataMap.put(REPEAT_PARAMS, nowParams);
        nowDataMap.put(REPEAT_TIME, System.currentTimeMillis());

        // 请求地址（作为存放cache的key值）
        String url = request.getRequestURI();

        Object sessionObj = redisCache.getCacheObject(CACHE_REPEAT_KEY);
        if (sessionObj != null)
        {
            Map<String, Object> sessionMap = (Map<String, Object>) sessionObj;
            if (sessionMap.containsKey(url))
            {
                Map<String, Object> preDataMap = (Map<String, Object>) sessionMap.get(url);
                if (compareParams(nowDataMap, preDataMap) && compareTime(nowDataMap, preDataMap))
                {
                    return true;
                }
            }
        }
        Map<String, Object> cacheMap = new HashMap<String, Object>();
        cacheMap.put(url, nowDataMap);
        redisCache.setCacheObject(CACHE_REPEAT_KEY, cacheMap, intervalTime, TimeUnit.SECONDS);
        return false;
    }

    /**
     * 判断参数是否相同
     */
    private boolean compareParams(Map<String, Object> nowMap, Map<String, Object> preMap)
    {
        String nowParams = (String) nowMap.get(REPEAT_PARAMS);
        String preParams = (String) preMap.get(REPEAT_PARAMS);
        return nowParams.equals(preParams);
    }

    /**
     * 判断两次间隔时间
     */
    private boolean compareTime(Map<String, Object> nowMap, Map<String, Object> preMap)
    {
        long time1 = (Long) nowMap.get(REPEAT_TIME);
        long time2 = (Long) preMap.get(REPEAT_TIME);
        if ((time1 - time2) < (this.intervalTime * 1000))
        {
            return true;
        }
        return false;
    }
}
