package cn.iocoder.yudao.framework.ip.core.utils;


import cn.iocoder.yudao.framework.ip.core.Area;
import cn.iocoder.yudao.framework.ip.core.enums.AreaTypeEnum;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AreaUtils} 的单元测试
 *
 * @author 芋道源码
 */
public class AreaUtilsTest {

    @Test
    public void testGetArea() {
        // 调用：北京
        Area area = AreaUtils.getArea(110100);
        // 断言
        assertEquals(area.getId(), 110100);
        assertEquals(area.getName(), "北京市");
        assertEquals(area.getType(), AreaTypeEnum.CITY.getType());
        assertEquals(area.getParent().getId(), 110000);
        assertEquals(area.getChildren().size(), 16);
    }

    @Test
    public void testFormat() {
        assertEquals(AreaUtils.format(110105), "北京市 北京市 朝阳区");
        assertEquals(AreaUtils.format(1), "中国");
        assertEquals(AreaUtils.format(2), "蒙古");
        // 中国台湾省：省/市/区三级
        assertEquals(AreaUtils.format(710101), "台湾省 台北市 中正区");
        // 自定义分隔符
        assertEquals(AreaUtils.format(110105, "/"), "北京市/北京市/朝阳区");
        // 不存在的编号
        assertNull(AreaUtils.format(-1));
    }

    @Test
    public void testParseArea() {
        // 调用：通过路径解析得到地区
        Area area = AreaUtils.parseArea("北京市/北京市/朝阳区");
        // 断言
        assertNotNull(area);
        assertEquals(area.getId(), 110105);
        // 路径不存在时返回 null
        assertNull(AreaUtils.parseArea("不存在/路径"));
    }

    @Test
    public void testGetParentIdByType() {
        // 调用：朝阳区向上找省
        Integer provinceId = AreaUtils.getParentIdByType(110105, AreaTypeEnum.PROVINCE);
        // 断言
        assertEquals(provinceId, 110000);
        // 自身就是目标类型
        assertEquals(AreaUtils.getParentIdByType(110000, AreaTypeEnum.PROVINCE), 110000);
        // 不存在的编号返回 null
        assertNull(AreaUtils.getParentIdByType(-1, AreaTypeEnum.PROVINCE));
    }

    @Test
    public void testGetByType() {
        // 调用：获取所有省份
        List<Area> provinces = AreaUtils.getByType(AreaTypeEnum.PROVINCE, area -> area);
        // 断言：包含北京、台湾、香港、澳门
        assertTrue(provinces.stream().anyMatch(area -> "北京市".equals(area.getName())));
        assertTrue(provinces.stream().anyMatch(area -> "台湾省".equals(area.getName())));
        assertTrue(provinces.stream().anyMatch(area -> "香港特别行政区".equals(area.getName())));
        assertTrue(provinces.stream().anyMatch(area -> "澳门特别行政区".equals(area.getName())));
    }

}
