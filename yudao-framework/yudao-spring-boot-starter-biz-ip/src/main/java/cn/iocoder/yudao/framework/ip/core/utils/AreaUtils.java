package cn.iocoder.yudao.framework.ip.core.utils;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.csv.CsvRow;
import cn.hutool.core.text.csv.CsvUtil;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.ip.core.Area;
import cn.iocoder.yudao.framework.ip.core.enums.AreaTypeEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 区域工具类
 *
 * @author 芋道源码
 */
@Slf4j
public class AreaUtils {

    /**
     * 初始化 SEARCHER
     */
    @SuppressWarnings("InstantiationOfUtilityClass")
    private final static AreaUtils INSTANCE = new AreaUtils();

    /**
     * Area 内存缓存，提升访问速度
     */
    private static Map<Integer, Area> areas;

    private AreaUtils() {
        long now = System.currentTimeMillis();
        areas = new HashMap<>();
        areas.put(Area.ID_GLOBAL, new Area(Area.ID_GLOBAL, "全球", 0,
                null, new ArrayList<>()));
        // 从 csv 中加载数据
        List<CsvRow> rows = CsvUtil.getReader().read(ResourceUtil.getUtf8Reader("area.csv")).getRows();
        rows.remove(0); // 删除 header
        for (CsvRow row : rows) {
            // 创建 Area 对象
            Area area = new Area(Integer.valueOf(row.get(0)), row.get(1), Integer.valueOf(row.get(2)),
                    null, new ArrayList<>());
            // 添加到 areas 中
            areas.put(area.getId(), area);
        }

        // 构建父子关系：因为 Area 中没有 parentId 字段，所以需要重复读取
        for (CsvRow row : rows) {
            Area area = areas.get(Integer.valueOf(row.get(0))); // 自己
            Area parent = areas.get(Integer.valueOf(row.get(3))); // 父
            Assert.isTrue(area != parent, "{}:父子节点相同", area.getName());
            area.setParent(parent);
            parent.getChildren().add(area);
        }
        log.info("启动加载 AreaUtils 成功，耗时 ({}) 毫秒", System.currentTimeMillis() - now);
    }

    /**
     * 获得指定编号对应的区域
     *
     * @param id 区域编号
     * @return 区域
     */
    public static Area getArea(Integer id) {
        return areas.get(id);
    }

    /**
     * 格式化区域
     *
     * @param id 区域编号
     * @return 格式化后的区域
     */
    public static String format(Integer id) {
        return format(id, " ");
    }

    /**
     * 格式化区域
     *
     * 例如说：
     *      1. id = “静安区”时：上海 上海市 静安区
     *      2. id = “上海市”时：上海 上海市
     *      3. id = “上海”时：上海
     *      4. id = “美国”时：美国
     * 当区域在中国时，默认不显示中国
     *
     * @param id 区域编号
     * @param separator 分隔符
     * @return 格式化后的区域
     */
    public static String format(Integer id, String separator) {
        // 获得区域
        Area area = areas.get(id);
        if (area == null) {
            return null;
        }

        // 格式化
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < AreaTypeEnum.values().length; i++) { // 避免死循环
            sb.insert(0, area.getName());
            // “递归”父节点
            area = area.getParent();
            if (area == null
                || ObjectUtils.equalsAny(area.getId(), Area.ID_GLOBAL, Area.ID_CHINA)) { // 跳过父节点为中国的情况
                break;
            }
            sb.insert(0, separator);
        }
        return sb.toString();
    }

}
