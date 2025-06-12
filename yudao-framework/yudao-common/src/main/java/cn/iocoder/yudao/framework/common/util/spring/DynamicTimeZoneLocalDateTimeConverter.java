package cn.iocoder.yudao.framework.common.util.spring;

import cn.hutool.core.date.DatePattern;
import cn.iocoder.yudao.framework.common.enums.TimeZoneEnum;
import cn.iocoder.yudao.framework.common.enums.WebCommonEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.TimeZone;

/**
 * @author 高巍
 * @since 2025-06-11 17:12:50
 * 处理GET请求的路径参数
 */
public class DynamicTimeZoneLocalDateTimeConverter implements Converter<String, LocalDateTime> {

    @Override
    public LocalDateTime convert(String source) {
        if (source.isEmpty()) {
            return null;
        }
        //        字符串转换为LocalDateTime
        ZoneId formZoneId = TimeZone.getDefault().toZoneId();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (Objects.nonNull(requestAttributes)) {
            Object object = requestAttributes.getAttribute(WebCommonEnum.HTTP_HEADER_TIME_ZONE, RequestAttributes.SCOPE_REQUEST);
            if (Objects.nonNull(object) && object instanceof String) {
                formZoneId = ZoneId.of((String) object);
            }
        }
        return LocalDateTime.parse(source, DatePattern.NORM_DATETIME_FORMATTER)
                .atZone(formZoneId)
                .withZoneSameInstant(TimeZoneEnum.UTC_ZONE_ID)
                .toLocalDateTime();
    }
}
