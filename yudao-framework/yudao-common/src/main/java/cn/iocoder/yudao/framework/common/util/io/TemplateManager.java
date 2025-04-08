package cn.iocoder.yudao.framework.common.util.io;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

import static cn.iocoder.yudao.framework.common.enums.ErrorCodeConstants.GENERATE_CONTRACT_FAIL;
import static cn.iocoder.yudao.framework.common.enums.ErrorCodeConstants.GENERATE_CONTRACT_FAIL_PARSE;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Slf4j
public class TemplateManager {

    public static byte[] getTemplateBytes(Resource resource) {
        if (resource == null) {
            throw exception(GENERATE_CONTRACT_FAIL, "null", "资源对象不能为空");
        }

        String path = resource.getFilename();
        log.debug("获取模板: {}", path);

        try {
            if (!resource.exists()) {
                throw exception(GENERATE_CONTRACT_FAIL, path, "模板文件不存在");
            }
            try (InputStream stream = resource.getInputStream()) {
                return stream.readAllBytes();
            }
        } catch (IOException e) {
            throw exception(GENERATE_CONTRACT_FAIL_PARSE, path, e.getMessage());
        }
    }
}
