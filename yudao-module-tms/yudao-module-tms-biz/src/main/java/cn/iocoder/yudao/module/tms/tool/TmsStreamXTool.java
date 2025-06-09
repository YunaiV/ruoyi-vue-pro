package cn.iocoder.yudao.module.tms.tool;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TmsStreamXTool {

    public static <T, K, D, V> void assemble(List<T> targetList,
                                             Function<T, K> keyGetter,
                                             Function<Collection<K>, Map<K, D>> dataLoader,
                                             BiConsumer<T, V> setter,
                                             Function<D, V> toVo) {
        Set<K> keys = targetList.stream().map(keyGetter).collect(Collectors.toSet());
        Map<K, D> dataMap = dataLoader.apply(keys);
        targetList.forEach(item -> {
            D data = dataMap.get(keyGetter.apply(item));
            if (data != null) setter.accept(item, toVo.apply(data));
        });
    }
}
