package cn.iocoder.yudao.module.system.service.dept;

import cn.iocoder.yudao.module.system.dal.dataobject.dept.UserPostDO;
import cn.iocoder.yudao.module.system.dal.mysql.dept.UserPostMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 工作流的表单定义 Mapper 接口
 * </p>
 *
 * @author anzhen
 * @since 2022-03-03
 */
@Service
public class UserPostServiceImpl extends ServiceImpl<UserPostMapper, UserPostDO> implements UserPostService {

}
