package cn.iocoder.yudao.framework.common.enums;

import java.time.ZoneId;

/**
 * @author 高巍
 * @since 2025-06-12 14:36:06
 */
public interface TimeZoneEnum {
    String _UTC_TIME_ZONE = "UTC";
    String _UTC8_TIME_ZONE = "UTC+8";
    ZoneId UTC_ZONE_ID = ZoneId.of(_UTC_TIME_ZONE);
    ZoneId UTC8_ZONE_ID = ZoneId.of(_UTC8_TIME_ZONE);
}
