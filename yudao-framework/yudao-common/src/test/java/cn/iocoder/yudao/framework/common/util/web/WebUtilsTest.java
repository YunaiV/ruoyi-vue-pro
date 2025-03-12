package cn.iocoder.yudao.framework.common.util.web;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @Description: $
 * @Author: c-tao
 * @Date: 2025/3/7$
 */
@Slf4j
public class WebUtilsTest {
    @Test
    public void test1() throws Exception {
        log.info(WebUtils.urlWithParams(
            "https://www.baidu.com",
            Map.of(
                "client", "111"
            ))
        );
    }
}
