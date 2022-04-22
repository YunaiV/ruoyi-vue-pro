package cn.iocoder.yudao.module.system.api.dept;

import cn.iocoder.yudao.module.system.service.dept.PostService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;

/**
 * 岗位 API 实现类
 *
 * @author 芋道源码
 */
@Service
public class PostApiImpl implements PostApi {

    @Resource
    private PostService postService;

    @Override
    public void validPosts(Collection<Long> ids) {
        postService.validPosts(ids);
    }
}
