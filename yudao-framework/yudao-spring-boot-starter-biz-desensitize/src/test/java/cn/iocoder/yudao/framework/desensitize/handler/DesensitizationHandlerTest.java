package cn.iocoder.yudao.framework.desensitize.handler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DesensitizationHandlerTest {

    @Test
    public void testSliderDesensitizationHandler() {
        DesensitizationHandler handler = DesensitizationHandlerHolder.getDesensitizationHandler(SliderDesensitizationHandler.class);

        Assertions.assertEquals("A****FG", handler.desensitize("ABCDEFG", 1, 2, "*"));
        Assertions.assertEquals("芋**码", handler.desensitize("芋道源码", 1, 1, "*"));
        Assertions.assertEquals("****", handler.desensitize("芋道源码", 4, 0, "*"));
    }

    @Test
    public void testRegexDesensitizationHandler() {
        DesensitizationHandler handler = DesensitizationHandlerHolder.getDesensitizationHandler(RegexDesensitizationHandler.class);

        Assertions.assertEquals("e****@gmail.com", handler.desensitize("example@gmail.com", "(^.)[^@]*(@.*$)", "$1****$2"));
        Assertions.assertEquals("***，铁***", handler.desensitize("他妈的，铁废物", "他妈的|去你大爷|卧槽|草泥马|废物", "***"));
    }

}
