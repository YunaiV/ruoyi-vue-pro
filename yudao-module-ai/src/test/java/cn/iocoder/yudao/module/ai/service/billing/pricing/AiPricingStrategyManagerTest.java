package cn.iocoder.yudao.module.ai.service.billing.pricing;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link AiPricingStrategyManager} 的单元测试
 */
class AiPricingStrategyManagerTest {

    private final DefaultPricingStrategy defaultStrategy = new DefaultPricingStrategy();

    @Test
    void testGetStrategy_null_returnsDefault() {
        AiPricingStrategyManager manager = new AiPricingStrategyManager(List.of(defaultStrategy));
        assertSame(defaultStrategy, manager.getStrategy(null));
    }

    @Test
    void testGetStrategy_empty_returnsDefault() {
        AiPricingStrategyManager manager = new AiPricingStrategyManager(List.of(defaultStrategy));
        assertSame(defaultStrategy, manager.getStrategy(""));
    }

    @Test
    void testGetStrategy_unknown_returnsDefault() {
        AiPricingStrategyManager manager = new AiPricingStrategyManager(List.of(defaultStrategy));
        assertSame(defaultStrategy, manager.getStrategy("UNKNOWN_TYPE"));
    }

    @Test
    void testGetStrategy_default_returnsDefault() {
        AiPricingStrategyManager manager = new AiPricingStrategyManager(List.of(defaultStrategy));
        assertSame(defaultStrategy, manager.getStrategy("DEFAULT"));
    }

    @Test
    void testGetStrategy_customStrategy() {
        AiPricingStrategy customStrategy = new AiPricingStrategy() {
            @Override
            public String getType() {
                return "CUSTOM";
            }

            @Override
            public long calculateCost(AiPricingContext ctx) {
                return 42L;
            }
        };
        AiPricingStrategyManager manager = new AiPricingStrategyManager(
                List.of(defaultStrategy, customStrategy));

        assertSame(customStrategy, manager.getStrategy("CUSTOM"));
        assertSame(defaultStrategy, manager.getStrategy("DEFAULT"));
        assertSame(defaultStrategy, manager.getStrategy(null));
    }

    @Test
    void testConstructor_noDefaultStrategy_throwsException() {
        AiPricingStrategy nonDefault = new AiPricingStrategy() {
            @Override
            public String getType() {
                return "OTHER";
            }

            @Override
            public long calculateCost(AiPricingContext ctx) {
                return 0L;
            }
        };
        assertThrows(IllegalStateException.class,
                () -> new AiPricingStrategyManager(List.of(nonDefault)));
    }

}
