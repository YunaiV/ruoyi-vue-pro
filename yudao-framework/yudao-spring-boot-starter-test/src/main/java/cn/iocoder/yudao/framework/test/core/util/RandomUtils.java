package cn.iocoder.yudao.framework.test.core.util;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;
import uk.co.jemos.podam.common.AttributeStrategy;

import javax.validation.constraints.Email;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 随机工具类
 *
 * @author 芋道源码
 */
public class RandomUtils {

    private static final int RANDOM_STRING_LENGTH = 10;

    private static final int TINYINT_MAX = 127;

    private static final int RANDOM_DATE_MAX = 30;

    private static final int RANDOM_COLLECTION_LENGTH = 5;

    private static final PodamFactory PODAM_FACTORY = new PodamFactoryImpl();

    static {
        // 字符串
        PODAM_FACTORY.getStrategy().addOrReplaceTypeManufacturer(String.class,
                (dataProviderStrategy, attributeMetadata, map) -> randomString());
        // Integer
        PODAM_FACTORY.getStrategy().addOrReplaceTypeManufacturer(Integer.class, (dataProviderStrategy, attributeMetadata, map) -> {
            // 如果是 status 的字段，返回 0 或 1
            if ("status".equals(attributeMetadata.getAttributeName())) {
                return RandomUtil.randomEle(CommonStatusEnum.values()).getStatus();
            }
            // 如果是 type、status 结尾的字段，返回 tinyint 范围
            if (StrUtil.endWithAnyIgnoreCase(attributeMetadata.getAttributeName(),
                    "type", "status", "category", "scope", "result")) {
                return RandomUtil.randomInt(0, TINYINT_MAX + 1);
            }
            return RandomUtil.randomInt();
        });
        // LocalDateTime
        PODAM_FACTORY.getStrategy().addOrReplaceTypeManufacturer(LocalDateTime.class,
                (dataProviderStrategy, attributeMetadata, map) -> randomLocalDateTime());
        // Boolean
        PODAM_FACTORY.getStrategy().addOrReplaceTypeManufacturer(Boolean.class, (dataProviderStrategy, attributeMetadata, map) -> {
            // 如果是 deleted 的字段，返回非删除
            if ("deleted".equals(attributeMetadata.getAttributeName())) {
                return false;
            }
            return RandomUtil.randomBoolean();
        });
    }

    public static String randomString() {
        return RandomUtil.randomString(RANDOM_STRING_LENGTH);
    }

    public static Long randomLongId() {
        return RandomUtil.randomLong(0, Long.MAX_VALUE);
    }

    public static Integer randomInteger() {
        return RandomUtil.randomInt(0, Integer.MAX_VALUE);
    }

    public static Date randomDate() {
        return RandomUtil.randomDay(0, RANDOM_DATE_MAX);
    }

    public static LocalDateTime randomLocalDateTime() {
        // 设置 Nano 为零的原因，避免 MySQL、H2 存储不到时间戳
        return LocalDateTimeUtil.of(randomDate()).withNano(0);
    }

    public static Short randomShort() {
        return (short) RandomUtil.randomInt(0, Short.MAX_VALUE);
    }

    public static <T> Set<T> randomSet(Class<T> clazz) {
        return Stream.iterate(0, i -> i).limit(RandomUtil.randomInt(1, RANDOM_COLLECTION_LENGTH))
                .map(i -> randomPojo(clazz)).collect(Collectors.toSet());
    }

    public static Integer randomCommonStatus() {
        return RandomUtil.randomEle(CommonStatusEnum.values()).getStatus();
    }

    public static String randomEmail() {
        return randomString() + "@qq.com";
    }

    public static String randomMobile() {
        return "13800138" + RandomUtil.randomNumbers(3);
    }

    public static String randomURL() {
        return "https://www.iocoder.cn/" + randomString();
    }

    @SafeVarargs
    public static <T> T randomPojo(Class<T> clazz, Consumer<T>... consumers) {
        T pojo = PODAM_FACTORY.manufacturePojo(clazz);
        // 非空时，回调逻辑。通过它，可以实现 Pojo 的进一步处理
        if (ArrayUtil.isNotEmpty(consumers)) {
            Arrays.stream(consumers).forEach(consumer -> consumer.accept(pojo));
        }
        return pojo;
    }

    @SafeVarargs
    public static <T> T randomPojo(Class<T> clazz, Type type, Consumer<T>... consumers) {
        T pojo = PODAM_FACTORY.manufacturePojo(clazz, type);
        // 非空时，回调逻辑。通过它，可以实现 Pojo 的进一步处理
        if (ArrayUtil.isNotEmpty(consumers)) {
            Arrays.stream(consumers).forEach(consumer -> consumer.accept(pojo));
        }
        return pojo;
    }

    @SafeVarargs
    public static <T> List<T> randomPojoList(Class<T> clazz, Consumer<T>... consumers) {
        int size = RandomUtil.randomInt(1, RANDOM_COLLECTION_LENGTH);
        return randomPojoList(clazz, size, consumers);
    }

    @SafeVarargs
    public static <T> List<T> randomPojoList(Class<T> clazz, int size, Consumer<T>... consumers) {
        return Stream.iterate(0, i -> i).limit(size).map(o -> randomPojo(clazz, consumers))
                .collect(Collectors.toList());
    }

}
