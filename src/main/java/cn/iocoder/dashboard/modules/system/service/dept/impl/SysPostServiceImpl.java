package cn.iocoder.dashboard.modules.system.service.dept.impl;

import cn.iocoder.dashboard.modules.system.dal.mysql.dao.dept.SysPostMapper;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dept.SysPostDO;
import cn.iocoder.dashboard.modules.system.service.dept.SysPostService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * 岗位 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class SysPostServiceImpl implements SysPostService {

    @Resource
    private SysPostMapper postMapper;

    @Override
    public List<SysPostDO> listPosts(Collection<Long> ids, Collection<Integer> statuses) {
        return postMapper.selectList(ids, statuses);
    }

}
