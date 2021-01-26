package cn.iocoder.dashboard.framework.redis.core;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RedisKeyDefine} 注册表
 */
public class RedisKeyRegistry {

    private static final List<RedisKeyDefine> defines = new ArrayList<>();

    public static void add(RedisKeyDefine define) {
        defines.add(define);
    }

    public static List<RedisKeyDefine> list() {
        return defines;
    }

    public static int size() {
        return defines.size();
    }

}
