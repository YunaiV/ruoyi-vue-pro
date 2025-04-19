package cn.iocoder.yudao.framework.common.util.collection;


import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 1、封装中间操作，一步完成。
 * 2、可重复使用，不受 Stream 流关闭影响
 */
public class StreamX<T> {


    public static <T> StreamX<T> from(Iterable<T> iterable) {
        return new StreamX<>(false, iterable);
    }

    public static <T> StreamX<T> from(Stream<T> stream) {
        return new StreamX<>(false, stream);
    }


    @SafeVarargs
    public static <T> StreamX<T> from(T... arr) {
        return new StreamX<>(false, arr);
    }

    public static <T> StreamX<T> parallel(Iterable<T> iterable) {
        return new StreamX<>(true, iterable);
    }

    public static <T> StreamX<T> parallel(Stream<T> stream) {
        return new StreamX<>(true, stream);
    }


    @SafeVarargs
    public static <T> StreamX<T> parallel(T... arr) {
        return new StreamX<>(true, arr);
    }


    private boolean parallel = false;
    private Iterable<T> iterable = null;

    private Stream<T> stream = null;




    private Stream<T> stream() {

        if (this.stream == null) {
            this.stream = StreamSupport.stream(iterable.spliterator(), this.parallel).onClose(() -> this.stream = null);
        } else {
            // test stream is available
            try {
                this.stream = this.stream.filter(t -> true);
            } catch (Exception e) {
                this.renew();
                return this.stream();
            }
        }

        return this.stream;
    }


    /**
     * 返回可循环迭代的数据列表
     */
    public Iterable<T> iterable() {
        return iterable;
    }

    /**
     * 从 Iterable 构造
     */
    private StreamX(boolean parallel, Iterable<T> iterable) {
        this.iterable = iterable;
        this.parallel = parallel;

    }

    /**
     * 从 stream 构造
     */
    private StreamX(boolean parallel, Stream<T> stream) {
        // 目前看上去还不是最佳方案
        Iterator<T> iterator = stream.iterator();
        List<T> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        this.iterable = list;
        this.parallel = parallel;
    }

    /**
     * 从数组构造
     */
    private StreamX(boolean parallel, T... arr) {
        this.iterable = List.of(arr);
        this.parallel = parallel;
    }

    /**
     * 从 stream 构造
     */
    private StreamX(Stream<T> stream) {
        this.stream = stream;
    }


    /**
     * 过滤
     *
     * @param pure false 不生成新的 StreamX 对象; true 生成新的 StreamX 对象
     */
    public StreamX<T> filter(Predicate<? super T> predicate, boolean pure) {
        this.stream = this.stream().filter(predicate);
        if (pure) {
            return this;
        } else {
            return new StreamX<>(this.parallel, this.stream);
        }
    }

    /**
     * 过滤, 返回当前 StreamX 对象
     */
    public StreamX<T> filter(Predicate<? super T> predicate) {
        return filter(predicate, true);
    }


    /**
     * 映射,带数据转换
     *
     * @param pure false 带数据; true 不带数据
     */
    public <R> StreamX<R> map(Function<? super T, ? extends R> mapper, boolean pure) {
        Stream<R> mapStream = this.stream().map(mapper);
        if (pure) {
            return new StreamX<>(mapStream);
        } else {
            return new StreamX<>(this.parallel, mapStream);
        }
    }

    /**
     * 映射,不带数据的纯 Stream 转换
     */
    public <R> StreamX<R> map(Function<? super T, ? extends R> mapper) {
        return map(mapper, true);
    }


    /**
     * 降维映射,带数据转换
     */
    public <R> StreamX<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper, boolean pure) {
        Stream<R> flatmapStream = this.stream().flatMap(mapper);
        if (pure) {
            return new StreamX<>(flatmapStream);
        } else {
            return new StreamX<>(this.parallel, flatmapStream);
        }
    }

    /**
     * 降维映射,不带数据的纯 Stream 转换
     */
    public <R> StreamX<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return flatMap(mapper, true);
    }


    /**
     * 去重
     */
    public StreamX<T> distinct() {
        this.stream = this.stream().distinct();
        return this;
    }


    /**
     * 排序
     */
    public StreamX<T> sorted(Comparator<? super T> comparator) {
        this.stream = this.stream().sorted(comparator);
        return this;
    }

    /**
     * 排序
     */
    public StreamX<T> sorted() {
        this.stream = this.stream().sorted();
        return this;
    }

    /**
     * 排序,空值最后
     *
     * @param property 属性
     * @param asc      是否降序，否则升序
     */
    public <R> StreamX<T> sorted(Function<? super T, ? extends R> property, boolean asc) {
        return sorted(property, asc, true);
    }

    /**
     * 排序
     *
     * @param property  属性
     * @param asc       是否降序，否则升序
     * @param nullsLast 是否为空值最后
     */
    public <R> StreamX<T> sorted(Function<? super T, ? extends R> property, boolean asc, boolean nullsLast) {
        this.stream = this.stream().sorted((o1, o2) -> {
            R v1 = property.apply(o1);
            R v2 = property.apply(o2);
            int cp = 0;
            if (v1 == null && v2 == null) cp = 0;
            else if (v1 != null && v2 == null) {
                cp = nullsLast ? -1 : 1;
            } else if (v1 == null && v2 != null) {
                cp = nullsLast ? 1 : -1;
            } else if (v1 != null && v2 != null) {
                if (v1 instanceof Comparable) {
                    cp = ((Comparable) v1).compareTo((Comparable) v2);
                } else {
                    String s1 = v1.toString();
                    String s2 = v2.toString();
                    cp = s1.compareTo(s2);
                }
                if (!asc) {
                    cp = -1 * cp;
                }
            }
            return cp;
        });
        return this;
    }


    /**
     * 限定最大元素索引
     */
    public StreamX<T> limit(long max) {
        this.stream = this.stream().limit(max);
        return this;
    }

    /**
     * 限定最小元素个数
     */
    public StreamX<T> skip(long min) {
        this.stream = this.stream().skip(min);
        return this;
    }


    /**
     * 获得第一个非空元素
     */
    public T first() {
        return stream().filter(Objects::nonNull).findFirst().orElse(null);
    }

    /**
     * 获得第一个非空元素，如果集合为空则返回默认值
     */
    public T first(T defaultValue) {
        return stream().filter(Objects::nonNull).findFirst().orElse(defaultValue);
    }

    /**
     * 获得过滤后的第一个非空元素
     */
    public T first(Predicate<? super T> filter) {
        return first(filter, null);
    }

    /**
     * 获得过滤后的第一个非空元素，如果集合为空则返回默认值
     */
    public T first(Predicate<? super T> filter, T defaultValue) {
        return stream().filter(Objects::nonNull).filter(filter).findFirst().orElse(defaultValue);
    }


    /**
     * 获得第一个非空元素的指定属性值
     */
    public <R> R firstProperty(Function<T, R> property) {
        T first = first();
        return first == null ? null : property.apply(first);
    }

    /**
     * 获得第一个非空指定属性值，如果找不到着则返回 null
     */
    public <R> R firstNonNullProperty(Function<T, R> property) {
        return stream().filter(Objects::nonNull).map(property).filter(Objects::nonNull).findFirst().orElse(null);
    }


    /**
     * 获得第一个非空元素的指定属性值，并指定默认值
     */
    public <R> R firstProperty(Function<T, R> property, R defaultValue) {
        R r = firstProperty(property);
        return r == null ? defaultValue : r;
    }

    /**
     * 获得过滤后第一个非空元素的指定属性值，如果找不到着则返回 null
     */
    public <R> R firstFilteredProperty(Predicate<? super T> filter, Function<T, R> property) {
        T first = first(filter);
        return first == null ? null : property.apply(first);

    }

    /**
     * 获得过滤后第一个非空元素的指定属性值
     */
    public <R> R firstFilteredProperty(Predicate<? super T> filter, Function<T, R> property, R defaultValue) {
        R r = firstFilteredProperty(filter, property);
        return r == null ? defaultValue : r;
    }


    /**
     * 获得任意非空元素
     */
    public T any() {
        return stream().filter(Objects::nonNull).findAny().orElse(null);
    }

    /**
     * 获得任意非空元素，如果集合为空返回默认值
     */
    public T any(T defaultValue) {
        return stream().filter(Objects::nonNull).findAny().orElse(defaultValue);
    }

    /**
     * 获得过滤后的任意非空元素
     */
    public T any(Predicate<? super T> filter) {
        return any(filter, null);
    }


    /**
     * 获得过滤后的任意非空元素，如果集合为空返回默认值
     */
    public T any(Predicate<? super T> filter, T defaultValue) {
        return stream().filter(Objects::nonNull).filter(filter).findAny().orElse(defaultValue);
    }

    /**
     * 获得任意一个非空元素的指定属性值
     */
    public <R> R anyProperty(Function<T, R> property) {
        T any = any();
        return any == null ? null : property.apply(any);
    }

    /**
     * 获得任意一个非空指定属性值，如果找不到着则返回 null
     */
    public <R> R anyNonNullProperty(Function<T, R> property) {
        return stream().filter(Objects::nonNull).map(property).filter(Objects::nonNull).findAny().orElse(null);
    }


    /**
     * 获得任意一个非空元素的指定属性值
     */
    public <R> R anyProperty(Function<T, R> property, R defaultValue) {
        R r = anyProperty(property);
        return r == null ? defaultValue : r;
    }

    /**
     * 获得过滤后任意一个非空元素的指定属性值
     */
    public <R> R anyFilteredProperty(Predicate<? super T> filter, Function<T, R> property) {
        T first = any(filter);
        return first == null ? null : property.apply(first);
    }

    /**
     * 获得过滤后任意一个非空元素的指定属性值，并指定默认值
     */
    public <R> R anyFilteredProperty(Predicate<? super T> filter, Function<T, R> property, R defaultValue) {
        R r = anyFilteredProperty(filter, property);
        return r == null ? defaultValue : r;
    }


    /**
     * 获得最后一个非空元素
     */
    public T last() {
        return stream().filter(Objects::nonNull).reduce((first, second) -> second).orElse(null);
    }

    /**
     * 获得最后一个非空元素，如果集合为空返回默认值
     */
    public T last(T defaultValue) {
        return stream().filter(Objects::nonNull).reduce((first, second) -> second).orElse(defaultValue);
    }

    /**
     * 获得过滤后的最后一个非空元素
     */
    public T last(Predicate<? super T> filter) {
        return last(filter, null);
    }

    /**
     * 获得过滤后的最后一个非空元素，如果集合为空返回默认值
     */
    public T last(Predicate<? super T> filter, T defaultValue) {
        return stream().filter(Objects::nonNull).filter(filter).reduce((first, second) -> second).orElse(defaultValue);
    }


    /**
     * 获得最后一个非空元素的指定属性值
     */
    public <R> R lastProperty(Function<T, R> property) {
        T first = last();
        return first == null ? null : property.apply(first);
    }

    /**
     * 获得最后一个非空指定属性值，如果找不到着则返回 null
     */
    public <R> R lastNonNullProperty(Function<T, R> property) {
        return stream().filter(Objects::nonNull).reduce((first, second) -> second).map(property).filter(Objects::nonNull).stream().findFirst().orElse(null);
    }


    /**
     * 获得最后一个非空元素的指定属性值，并指定默认值
     */
    public <R> R lastProperty(Function<T, R> property, R defaultValue) {
        R r = lastProperty(property);
        return r == null ? defaultValue : r;
    }

    /**
     * 获得过滤后最后一个非空元素的指定属性值
     */
    public <R> R lastFilteredProperty(Predicate<? super T> filter, Function<T, R> property) {
        T first = last(filter);
        return first == null ? null : property.apply(first);

    }

    /**
     * 获得过滤后最后一个非空元素的指定属性值，并指定默认值
     */
    public <R> R lastFilteredProperty(Predicate<? super T> filter, Function<T, R> property, R defaultValue) {
        R r = lastFilteredProperty(filter, property);
        return r == null ? defaultValue : r;
    }


    /**
     * 返回集合中 null 元素的个数
     */
    public long nulls() {
        return stream().filter(Objects::isNull).count();
    }

    /**
     * 返回集合中指定属性值为 null 元素的个数
     */
    public <R> long nulls(Function<T, R> property) {
        long a = nulls();
        long b = stream().filter(t -> {
            return t != null && property.apply(t) == null;
        }).count();
        return a + b;
    }


    /**
     * 遍历
     */
    public StreamX<T> forEach(Consumer<? super T> action) {
        List<T> list = this.toList();
        list.stream().forEach(action);
        return StreamX.from(list);
    }

    /**
     * 返回当前集合转换后的 List
     */
    public <R> List<R> toList(Function<? super T, ? extends R> property) {
        return this.stream().filter(Objects::nonNull).map(property).collect(Collectors.toList());
    }

    /**
     * 返回当前集合转换后的 List
     */
    public <R> List<R> toList(Function<? super T, ? extends R> property, boolean removeNulls, boolean distinct) {
        Stream<R> stream = this.stream().filter(Objects::nonNull).map(property);
        if (removeNulls) {
            stream = stream.filter(Objects::nonNull);
        }
        if (distinct) {
            stream = stream.distinct();
        }
        return stream.collect(Collectors.toList());
    }

    /**
     * 返回当前集合转换后的 List
     */
    public <R> List<R> toList(Function<? super T, ? extends R> property, boolean removeNulls) {
        return toList(property, removeNulls, false);
    }

    /**
     * 返回当前集合类型的 List
     */
    public List<T> toList() {
        return this.stream().collect(Collectors.toList());
    }


    /**
     * 当前集合按特定的Key 生成 Map
     */
    public <K> Map<K, T> toMap(Function<? super T, ? extends K> key) {
        return this.stream().filter(Objects::nonNull).filter(t -> {
            return key.apply(t) != null;
        }).collect(Collectors.toMap(key, t -> t));
    }

    /**
     * 当前集合按特定的Key 生成 Map
     */
    public <K> Map<K, T> toMap(Function<? super T, ? extends K> key, boolean keepFirstOnDPkey) {
        return this.stream().filter(Objects::nonNull).filter(t -> {
            return key.apply(t) != null;
        }).collect(Collectors.toMap(key, t -> t, (v1, v2) -> {
            return keepFirstOnDPkey ? v1 : v2;
        }));
    }

    /**
     * 当前集合按特定的Key 和 Value 生成 Map
     */
    public <K, V> Map<K, V> toMap(Function<? super T, ? extends K> key, Function<? super T, ? extends V> val) {
        return this.stream().filter(Objects::nonNull).filter(t -> {
            return key.apply(t) != null;
        }).collect(Collectors.toMap(key, val));
    }

    /**
     * 当前集合按特定的Key 和 Value 生成 Map
     *
     * @param keepFirstOnDPkey true:如果key值重复，则按顺序保留第一个，其它值忽略；false:如果key值重复，则按顺序保留最后一个，其它值忽略
     */
    public <K, V> Map<K, V> toMap(Function<? super T, ? extends K> key, Function<? super T, ? extends V> val, boolean keepFirstOnDPkey) {
        return this.stream().filter(Objects::nonNull).filter(t -> {
            return key.apply(t) != null;
        }).collect(Collectors.toMap(key, val, (v1, v2) -> {
            return keepFirstOnDPkey ? v1 : v2;
        }));
    }


    /**
     * 当前集合按特定的Key 生成 Map，如果Key值为null则排除
     */
    public <K> Map<K, List<T>> groupBy(Function<? super T, ? extends K> key) {
        return this.stream().filter(Objects::nonNull).filter(t -> key.apply(t) != null).collect(Collectors.groupingBy(t -> {
            K keyVal = key.apply(t);
            return keyVal;
        }));
    }

    /**
     * 当前集合按特定的Key 生成 Map，如果Key值为null，则使用 nullKeyValue 为 key 值
     */
    public <K> Map<K, List<T>> groupBy(Function<? super T, ? extends K> key, K nullKeyValue) {
        return this.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(t -> {
            K keyVal = key.apply(t);
            if (keyVal == null) {
                return nullKeyValue;
            }
            return keyVal;
        }));
    }

    /**
     * 当前集合按特定的Key 和 Value 生成 Map
     */
    public <K, V> Map<K, List<V>> groupBy(Function<? super T, ? extends K> key, Function<? super T, ? extends V> val) {
        Map<K, List<T>> source = groupBy(key);
        return collectProperty(source, val);
    }

    /**
     * 当前集合按特定的Key 和 Value 生成 Map
     */
    public <K, V> Map<K, List<V>> groupBy(Function<? super T, ? extends K> key, Function<? super T, ? extends V> val, K nullKeyVal) {
        Map<K, List<T>> source = groupBy(key, nullKeyVal);
        return collectProperty(source, val);
    }

    private <V, K> Map<K, List<V>> collectProperty(Map<K, List<T>> source, Function<? super T, ? extends V> val) {
        Map<K, List<V>> map = new HashMap<>();
        source.forEach((k, sl) -> {
            List<V> list = new ArrayList<>();
            sl.forEach(t -> list.add(val.apply(t)));
            map.put(k, list);
        });
        return map;
    }


    /**
     * 按 key 值去重
     *
     * @param keepFirstOnDPkey 当 key 重复时，是否保留靠前的 key 和 元素
     */
    public <K> List<T> distinct(Function<? super T, ? extends K> keyMapper, final boolean keepFirstOnDPkey) {
        Map<K, T> map = this.stream().filter(Objects::nonNull).collect(Collectors.toMap(keyMapper, t -> t
            , (v1, v2) -> {
                return keepFirstOnDPkey ? v1 : v2;
            }));
        return new ArrayList<>(map.values());
    }

    /**
     * 按 key 值去重
     */
    public <K> List<T> distinct(Function<? super T, ? extends K> keyMapper) {
        return distinct(keyMapper, true);
    }


    /**
     * 返回当前集合类型的 Set
     */
    public Set<T> toSet() {
        return this.stream().collect(Collectors.toSet());
    }


    /**
     * 返回当前集合转换后的 Set
     */
    public <R> Set<R> toSet(Function<? super T, ? extends R> property) {
        return this.stream().filter(Objects::nonNull).map(property).collect(Collectors.toSet());
    }

    /**
     * 返回当前集合转换后的 Set
     */
    public <R> Set<R> toSet(Function<? super T, ? extends R> property, boolean removeNulls) {
        Stream<R> stream = this.stream().filter(Objects::nonNull).map(property);
        if (removeNulls) {
            stream = stream.filter(Objects::nonNull);
        }
        return stream.collect(Collectors.toSet());
    }


    /**
     * 扁平化集合类型的属性为一个List集合
     */
    public <R> List<R> flattenList(Function<? super T, ? extends Stream<? extends R>> mapper, boolean removeNulls, boolean distinct) {
        Stream<R> stream = this.stream().flatMap(mapper);
        if (removeNulls) {
            stream = stream.filter(Objects::nonNull);
        }
        if (distinct) {
            stream = stream.distinct();
        }
        return stream.collect(Collectors.toList());
    }


    /**
     * 扁平化集合类型的属性为一个List集合
     */
    public <R> List<R> flattenList(Function<? super T, ? extends Stream<? extends R>> mapper, boolean removeNulls) {
        return flattenList(mapper, removeNulls, false);
    }


    /**
     * 扁平化集合类型的属性为一个Set集合
     */
    public <R> Set<R> flattenSet(Function<? super T, ? extends Stream<? extends R>> mapper, boolean removeNulls) {
        Stream<R> stream = this.stream().flatMap(mapper);
        if (removeNulls) {
            stream = stream.filter(Objects::nonNull);
        }
        return stream.collect(Collectors.toSet());
    }

    /**
     * 扁平化集合类型的属性为一个Set集合
     */
    public <R> Set<R> flattenSet(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return flattenSet(mapper, false);
    }


    /**
     * 建立新的 stream 流
     */
    public StreamX<T> renew() {
        this.stream = null;
        return this;
    }

    /**
     * 链式取得结果
     */
    public StreamX<T> result(Consumer<StreamX<T>> consumer) {
        consumer.accept(this);
        return this;
    }


    /**
     * 将当前Map中的元素，按照指定的key，设置到对应元素的属性中
     * @param propertyValueMap 以 keyGetter 方法对应的属性值为 key 的属性值对象 Map
     * @param keyPropertyGetter 获取当前元素属性值的方法，以此属性值为 key，从 propertyValueMap 中获取对应的属性值
     * @param propertySetter 设置当前元素属性值的方法，将 beanMap 中获取到的属性值设置到当前元素属性中
     * @return 当前对象
     **/
    public <K, M> StreamX<T> assemble(Map<K,M> propertyValueMap, Function<T,K> keyPropertyGetter, BiConsumer<T,M> propertySetter) {
        this.forEach(t->{
            if(t==null) {
                return;
            }
            K key = keyPropertyGetter.apply(t);
            M value = propertyValueMap.get(key);
            propertySetter.accept(t,value);
        });
        return this;
    }

    /**
     * 将当前 valueList 中的元素，按照指定的 key，设置到对应元素的属性中
     * @param valueList 以 keyPropertyGetter4List 方法对应的属性值为 key 的属性值对象 Map
     * @param keyPropertyGetter4List 用此方法获取当前元素属性值，以此属性值为 key，产生中间 Map
     * @param keyPropertyGetter 获取当前元素属性值的方法，以此属性值为 key，从中间 Map 中获取对应的属性值
     * @param propertySetter 设置当前元素属性值的方法，将中间 Map 中获取到的属性值设置到当前元素属性中
     * @return 当前对象
     **/
    public <K, M> StreamX<T> assemble(Collection<M> valueList, Function<M,K> keyPropertyGetter4List,Function<T,K> keyPropertyGetter, BiConsumer<T,M> propertySetter) {
        Map<K,M> propertyValueMap=StreamX.from(valueList).toMap(keyPropertyGetter4List,t->t);
        return this.assemble(propertyValueMap,keyPropertyGetter,propertySetter);
    }

    /**
     * Same as Stream::iterate but will include the first element where hasNext evaluate to false
     *
     * @param <T>          The type of the element.
     * @param seed    the initial element
     * @param hasNext   a predicate to apply to elements to determine when the last element is reached
     * @param next  a function to be applied to the previous element to produce a new element
     * @return a new sequential Stream
     */
    public static <T> Stream<T> iterate(T seed, Predicate<? super T> hasNext, UnaryOperator<T> next) {
        return StreamSupport.stream(new PaginationSpliterator<>(seed, hasNext, next), false);
    }


    @Getter
    public static class CompareResult<B> {

        private List<B> intersectionList= new ArrayList<>();
        private List<B> baseMoreThanTargetList= new ArrayList<>();
        private List<B> targetMoreThanBaseList= new ArrayList<>();

        void addIntersection(B bean) {
            this.intersectionList.add(bean);
        }

        void addBaseMoreThanTarget(B bean) {
            this.baseMoreThanTargetList.add(bean);
        }

        void addTargetMoreThanBase(B bean) {
            this.targetMoreThanBaseList.add(bean);
        }



    }

    public static <B,I> CompareResult<B> compare(Collection<B> baseCollection, Collection<B> targetCollection, Function<B,I> key) {

        Map<I,B> baseMap=StreamX.from(baseCollection).toMap(key,t->t);
        Map<I,B> targetMap=StreamX.from(targetCollection).toMap(key,t->t);

        CompareResult result = new CompareResult();

        StreamX.from(targetCollection).filter(Objects::nonNull).forEach(item -> {
            if(!baseMap.keySet().contains(key.apply(item))) {
                result.addTargetMoreThanBase(item);
            }
            if(baseMap.keySet().contains(key.apply(item))) {
                result.addIntersection(item);
            }
        });
        baseCollection.forEach(item -> {
            if(!targetMap.keySet().contains(key.apply(item))) {
                result.addBaseMoreThanTarget(item);
            }
        });
        return result;
    }

    public static <B,V> boolean isRepeated(Collection<B> collection, Function<B,V> value) {
        Set<V> set=StreamX.from(collection).toSet(value);
        return set.size()!=collection.size();
    }

}




class PaginationSpliterator<T> implements Spliterator<T> {
    private T currentPage;
    private final Predicate<? super T> haveNextPage;
    private final UnaryOperator<T> getNextPage;
    private boolean finished = false;

    public PaginationSpliterator(T firstPage, Predicate<? super T> haveNextPage, UnaryOperator<T> getNextPage) {
        this.currentPage = firstPage;
        this.haveNextPage = haveNextPage;
        this.getNextPage = getNextPage;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if (finished) {
            return false;
        }
        action.accept(currentPage);
        if (!haveNextPage.test(currentPage)) {
            finished = true;
        } else {
            currentPage = getNextPage.apply(currentPage);
        }
        return true;
    }

    @Override
    public Spliterator<T> trySplit() {
        return null; // Not supporting parallel streams
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE; // Unknown size
    }

    @Override
    public int characteristics() {
        return ORDERED | NONNULL | IMMUTABLE;
    }

}


