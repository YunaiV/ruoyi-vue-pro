package cn.iocoder.yudao.framework.ip.core.utils;

import cn.iocoder.yudao.framework.ip.core.Area;
import org.junit.jupiter.api.Test;
import org.lionsoul.ip2region.xdb.Searcher;


import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link IPUtils} 的单元测试
 */
class IPUtilsTest {

    @Test
    void getAreaId() {
        // 120.202.4.0|120.202.4.255|420600
        Integer areaId = IPUtils.getAreaId("120.202.4.50");
        assertEquals(420600, areaId);
    }

    @Test
    void testGetAreaId() {
        // 120.203.123.0|120.203.133.255|360900
        long ip = 0L;
        try {
            ip = Searcher.checkIP("120.203.123.250");
        } catch (Exception e) {
            // ignore
        }
        Integer areaId = IPUtils.getAreaId(ip);
        assertEquals(360900, areaId);
    }

    @Test
    void getArea() {
        // 120.202.4.0|120.202.4.255|420600
        Area area = IPUtils.getArea("120.202.4.50");
        assertEquals("襄阳市", area.getName());
    }

    @Test
    void testGetArea() {
        // 120.203.123.0|120.203.133.255|360900
        long ip = 0L;
        try {
            ip = Searcher.checkIP("120.203.123.252");
        } catch (Exception e) {
            // ignore
        }
        Area area = IPUtils.getArea(ip);
        assertEquals("宜春市", area.getName());

    }
}