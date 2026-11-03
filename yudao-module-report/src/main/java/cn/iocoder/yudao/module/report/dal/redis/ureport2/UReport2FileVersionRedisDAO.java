package cn.iocoder.yudao.module.report.dal.redis.ureport2;

import cn.hutool.core.util.IdUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

import static cn.iocoder.yudao.module.report.dal.redis.RedisKeyConstants.UREPORT2_FILE_VERSION;

/**
 * UReport2 报表模板版本号的 Redis DAO
 *
 * 只保存随机版本号，不保存解析后的模板定义：模板定义的对象图较复杂（表达式、数据源、图表等），不适合序列化到 Redis
 *
 * @author 芋道源码
 */
@Repository
public class UReport2FileVersionRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 获得模板的版本号
     *
     * @param tenantId 租户编号
     * @param file 模板名称，包含存储前缀
     * @return 版本号；从未保存、删除过时为 null
     */
    public String get(Long tenantId, String file) {
        return stringRedisTemplate.opsForValue().get(formatKey(tenantId, file));
    }

    /**
     * 刷新模板的版本号
     *
     * 在模板保存、删除并提交到数据库之后调用
     *
     * @param tenantId 租户编号
     * @param file 模板名称，包含存储前缀
     * @return 新的版本号
     */
    public String refresh(Long tenantId, String file) {
        String version = IdUtil.fastSimpleUUID();
        stringRedisTemplate.opsForValue().set(formatKey(tenantId, file), version);
        return version;
    }

    private static String formatKey(Long tenantId, String file) {
        return String.format(UREPORT2_FILE_VERSION, tenantId, file);
    }

}
