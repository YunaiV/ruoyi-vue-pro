package cn.iocoder.yudao.coreservice.modules.system.service.dept.impl;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.dept.SysPostDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.mysql.dept.SysPostCoreMapper;
import cn.iocoder.yudao.coreservice.modules.system.service.dept.SysPostCoreService;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.coreservice.modules.system.enums.SysErrorCodeConstants.POST_NOT_ENABLE;
import static cn.iocoder.yudao.coreservice.modules.system.enums.SysErrorCodeConstants.POST_NOT_FOUND;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 岗位 Core Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class SysPostCoreServiceImpl implements SysPostCoreService {

    @Resource
    private SysPostCoreMapper sysPostCoreMapper;

    @Override
    public void validPosts(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 获得岗位信息
        List<SysPostDO> posts = sysPostCoreMapper.selectBatchIds(ids);
        Map<Long, SysPostDO> postMap = CollectionUtils.convertMap(posts, SysPostDO::getId);
        // 校验
        ids.forEach(id -> {
            SysPostDO post = postMap.get(id);
            if (post == null) {
                throw exception(POST_NOT_FOUND);
            }
            if (!CommonStatusEnum.ENABLE.getStatus().equals(post.getStatus())) {
                throw exception(POST_NOT_ENABLE, post.getName());
            }
        });
    }
}
