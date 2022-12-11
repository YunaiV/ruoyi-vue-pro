package cn.iocoder.yudao.framework.ip.core.utils;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.ip.core.Area;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;

import java.io.IOException;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;

/**
 * IP 工具类
 * <p>
 * 依赖于ip2region.xdb精简版，来源于<a href="https://gitee.com/zhijiantianya/ip2region"/>
 * region精简为城市编码
 *
 * @author 芋道源码
 */
@Slf4j
public class IPUtils {

    /**
     * 根据ip搜索
     * 启动加载到内存中
     */
    private static Searcher SEARCHER;

    /**
     * 初始化SEARCHER
     */
    private final static IPUtils INSTANCE = new IPUtils();


    /**
     * 私有化构造
     */
    private IPUtils() {
        String dbPath = "ip2region.xdb";
        dbPath = IPUtils.class.getClassLoader().getResource(dbPath).getPath();
        try {
            SEARCHER = Searcher.newWithBuffer(Searcher.loadContentFromFile(dbPath));
        } catch (IOException e) {
            // 加载xdb文件异常,不影响启动
            log.error("启动加载IP SEARCH异常", e);
            SEARCHER = null;
        }
    }


    /**
     * 查询IP对应的地区ID，格式应为127.0.0.1
     * @param ip ip地址
     * @return 地区id
     */
    public static Integer getAreaId(String ip) {
        try {
            return Integer.parseInt(SEARCHER.search(ip));
        } catch (Exception e) {
            throw new ServiceException(INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 查询IP对应的地区ID，格式参考{@link Searcher#checkIP(String)} 的返回
     * @param ip ip地址
     * @return 地区id
     */
    public static Integer getAreaId(long ip) {
        try {
            return Integer.parseInt(SEARCHER.search(ip));
        } catch (Exception e) {
            throw new ServiceException(INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 查询IP对应的地区，格式应为127.0.0.1
     * @param ip ip地址
     * @return 地区
     */
    public static Area getArea(String ip) {
        return AreaUtils.getArea(getAreaId(ip));
    }

    /**
     * 查询IP对应的地区，格式参考{@link Searcher#checkIP(String)} 的返回
     * @param ip ip地址
     * @return 地区
     */
    public static Area getArea(long ip) {
        return AreaUtils.getArea(getAreaId(ip));
    }
}
