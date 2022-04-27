package cn.iocoder.yudao.module.system.service.dept;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.post.PostCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.post.PostExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.post.PostPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.post.PostUpdateReqVO;
import cn.iocoder.yudao.module.system.convert.dept.PostConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.PostDO;
import cn.iocoder.yudao.module.system.dal.mysql.dept.PostMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;

/**
 * 岗位 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PostServiceImpl implements PostService {

    @Resource
    private PostMapper postMapper;

    @Override
    public Long createPost(PostCreateReqVO reqVO) {
        // 校验正确性
        this.checkCreateOrUpdate(null, reqVO.getName(), reqVO.getCode());
        // 插入岗位
        PostDO post = PostConvert.INSTANCE.convert(reqVO);
        postMapper.insert(post);
        return post.getId();
    }

    @Override
    public void updatePost(PostUpdateReqVO reqVO) {
        // 校验正确性
        this.checkCreateOrUpdate(reqVO.getId(), reqVO.getName(), reqVO.getCode());
        // 更新岗位
        PostDO updateObj = PostConvert.INSTANCE.convert(reqVO);
        postMapper.updateById(updateObj);
    }

    @Override
    public void deletePost(Long id) {
        // 校验是否存在
        this.checkPostExists(id);
        // 删除部门
        postMapper.deleteById(id);
    }

    @Override
    public List<PostDO> getPosts(Collection<Long> ids, Collection<Integer> statuses) {
        return postMapper.selectList(ids, statuses);
    }

    @Override
    public PageResult<PostDO> getPostPage(PostPageReqVO reqVO) {
        return postMapper.selectPage(reqVO);
    }

    @Override
    public List<PostDO> getPosts(PostExportReqVO reqVO) {
        return postMapper.selectList(reqVO);
    }

    @Override
    public PostDO getPost(Long id) {
        return postMapper.selectById(id);
    }

    private void checkCreateOrUpdate(Long id, String name, String code) {
        // 校验自己存在
        checkPostExists(id);
        // 校验岗位名的唯一性
        checkPostNameUnique(id, name);
        // 校验岗位编码的唯一性
        checkPostCodeUnique(id, code);
    }

    private void checkPostNameUnique(Long id, String name) {
        PostDO post = postMapper.selectByName(name);
        if (post == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的岗位
        if (id == null) {
            throw ServiceExceptionUtil.exception(POST_NAME_DUPLICATE);
        }
        if (!post.getId().equals(id)) {
            throw ServiceExceptionUtil.exception(POST_NAME_DUPLICATE);
        }
    }

    private void checkPostCodeUnique(Long id, String code) {
        PostDO post = postMapper.selectByCode(code);
        if (post == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的岗位
        if (id == null) {
            throw ServiceExceptionUtil.exception(POST_CODE_DUPLICATE);
        }
        if (!post.getId().equals(id)) {
            throw ServiceExceptionUtil.exception(POST_CODE_DUPLICATE);
        }
    }

    private void checkPostExists(Long id) {
        if (id == null) {
            return;
        }
        PostDO post = postMapper.selectById(id);
        if (post == null) {
            throw ServiceExceptionUtil.exception(POST_NOT_FOUND);
        }
    }

    @Override
    public void validPosts(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 获得岗位信息
        List<PostDO> posts = postMapper.selectBatchIds(ids);
        Map<Long, PostDO> postMap = convertMap(posts, PostDO::getId);
        // 校验
        ids.forEach(id -> {
            PostDO post = postMap.get(id);
            if (post == null) {
                throw exception(POST_NOT_FOUND);
            }
            if (!CommonStatusEnum.ENABLE.getStatus().equals(post.getStatus())) {
                throw exception(POST_NOT_ENABLE, post.getName());
            }
        });
    }
}
