package cn.iocoder.dashboard.util.collection;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Map 工具类
 *
 * @author 芋道源码
 */
public class MapUtils {

    public static <K, V> List<V> getList(Multimap<K, V> multimap, Collection<K> keys) {
        List<V> result = new ArrayList<>();
        keys.forEach(k -> {
            Collection<V> values = multimap.get(k);
            if (CollectionUtil.isEmpty(values)) {
                return;
            }
            result.addAll(values);
        });
        return result;
    }

}
