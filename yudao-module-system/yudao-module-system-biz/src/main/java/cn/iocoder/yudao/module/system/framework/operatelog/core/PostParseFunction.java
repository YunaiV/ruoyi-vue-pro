package cn.iocoder.yudao.module.system.framework.operatelog.core;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.PostDO;
import cn.iocoder.yudao.module.system.service.dept.PostService;
import com.mzt.logapi.service.IParseFunction;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 岗位名字的 {@link IParseFunction} 实现类
 *
 * @author HUIHUI
 */
@Slf4j
@Component
public class PostParseFunction implements IParseFunction {

    public static final String NAME = "getPostById";

    @Resource
    private PostService postService;

    @Override
    public String functionName() {
        return NAME;
    }

    @Override
    public String apply(Object value) {
        if (StrUtil.isEmptyIfStr(value)) {
            return "";
        }

        // 获取岗位信息
        PostDO post = postService.getPost(Convert.toLong(value));
        if (post == null) {
            log.warn("[apply][获取岗位{{}}为空", value);
            return "";
        }
        return post.getName();
    }

}
