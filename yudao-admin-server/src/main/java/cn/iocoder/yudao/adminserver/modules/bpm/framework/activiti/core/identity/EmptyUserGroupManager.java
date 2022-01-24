package cn.iocoder.yudao.adminserver.modules.bpm.framework.activiti.core.identity;

import org.activiti.api.runtime.shared.identity.UserGroupManager;

import java.util.Collections;
import java.util.List;

/**
 * 空的 UserGroupManager 实现类，用于禁用 Activiti 自带的用户组实现。
 * 原因是，我们使用了自己实现的任务分配规则，所以不需要 Activiti。
 * 如果不去禁用，会存在一些场景下，会去查询用户所在的用户组，导致报错。
 *
 * @author 芋道源码
 */
public class EmptyUserGroupManager implements UserGroupManager {

    @Override
    public List<String> getUserGroups(String s) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getUserRoles(String s) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getGroups() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getUsers() {
        return Collections.emptyList();
    }

}
