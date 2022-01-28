package cn.iocoder.yudao.adminserver.modules.system.service.user.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.adminserver.modules.system.controller.user.vo.profile.SysUserProfileUpdatePasswordReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.user.vo.profile.SysUserProfileUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.user.vo.user.*;
import cn.iocoder.yudao.adminserver.modules.system.convert.user.SysUserConvert;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.dept.SysDeptDO;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.dept.SysPostDO;
import cn.iocoder.yudao.adminserver.modules.system.dal.mysql.user.SysUserMapper;
import cn.iocoder.yudao.adminserver.modules.system.service.dept.SysDeptService;
import cn.iocoder.yudao.adminserver.modules.system.service.dept.SysPostService;
import cn.iocoder.yudao.adminserver.modules.system.service.permission.SysPermissionService;
import cn.iocoder.yudao.adminserver.modules.system.service.user.SysUserService;
import cn.iocoder.yudao.coreservice.modules.infra.service.file.InfFileCoreService;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.*;

import static cn.iocoder.yudao.adminserver.modules.system.enums.SysErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 用户 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class SysUserServiceImpl implements SysUserService {

    @Value("${sys.user.init-password:yudaoyuanma}")
    private String userInitPassword;

    @Resource(name = "sysUserMapper") // userMapper 存在重名
    private SysUserMapper userMapper;

    @Resource
    private SysDeptService deptService;
    @Resource
    private SysPostService postService;
    @Resource
    private SysPermissionService permissionService;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private InfFileCoreService fileService;

    @Override
    public Long createUser(SysUserCreateReqVO reqVO) {
        // 校验正确性
        this.checkCreateOrUpdate(null, reqVO.getUsername(), reqVO.getMobile(), reqVO.getEmail(),
            reqVO.getDeptId(), reqVO.getPostIds());
        // 插入用户
        SysUserDO user = SysUserConvert.INSTANCE.convert(reqVO);
        user.setStatus(CommonStatusEnum.ENABLE.getStatus()); // 默认开启
        user.setPassword(passwordEncoder.encode(reqVO.getPassword())); // 加密密码
        userMapper.insert(user);
        return user.getId();
    }

    @Override
    public void updateUser(SysUserUpdateReqVO reqVO) {
        // 校验正确性
        this.checkCreateOrUpdate(reqVO.getId(), reqVO.getUsername(), reqVO.getMobile(), reqVO.getEmail(),
            reqVO.getDeptId(), reqVO.getPostIds());
        // 更新用户
        SysUserDO updateObj = SysUserConvert.INSTANCE.convert(reqVO);
        userMapper.updateById(updateObj);
    }

    @Override
    public void updateUserLogin(Long id, String loginIp) {
        userMapper.updateById(new SysUserDO().setId(id).setLoginIp(loginIp).setLoginDate(new Date()));
    }

    @Override
    public void updateUserProfile(Long id, SysUserProfileUpdateReqVO reqVO) {
        // 校验正确性
        this.checkUserExists(id);
        this.checkEmailUnique(id, reqVO.getEmail());
        this.checkMobileUnique(id, reqVO.getMobile());
        // 执行更新
        userMapper.updateById(SysUserConvert.INSTANCE.convert(reqVO).setId(id));
    }

    @Override
    public void updateUserPassword(Long id, SysUserProfileUpdatePasswordReqVO reqVO) {
        // 校验旧密码密码
        this.checkOldPassword(id, reqVO.getOldPassword());
        // 执行更新
        SysUserDO updateObj = new SysUserDO().setId(id);
        updateObj.setPassword(passwordEncoder.encode(reqVO.getNewPassword())); // 加密密码
        userMapper.updateById(updateObj);
    }

    @Override
    public String updateUserAvatar(Long id, InputStream avatarFile) {
        this.checkUserExists(id);
        // 存储文件
        String avatar = fileService.createFile(IdUtil.fastUUID(), IoUtil.readBytes(avatarFile));
        // 更新路径
        SysUserDO sysUserDO = new SysUserDO();
        sysUserDO.setId(id);
        sysUserDO.setAvatar(avatar);
        userMapper.updateById(sysUserDO);
        return avatar;
    }

    @Override
    public void updateUserPassword(Long id, String password) {
        // 校验用户存在
        this.checkUserExists(id);
        // 更新密码
        SysUserDO updateObj = new SysUserDO();
        updateObj.setId(id);
        updateObj.setPassword(passwordEncoder.encode(password)); // 加密密码
        userMapper.updateById(updateObj);
    }

    @Override
    public void updateUserStatus(Long id, Integer status) {
        // 校验用户存在
        this.checkUserExists(id);
        // 更新状态
        SysUserDO updateObj = new SysUserDO();
        updateObj.setId(id);
        updateObj.setStatus(status);
        userMapper.updateById(updateObj);
    }

    @Override
    public void deleteUser(Long id) {
        // 校验用户存在
        this.checkUserExists(id);
        // 删除用户
        userMapper.deleteById(id);
        // 删除用户关联数据
        permissionService.processUserDeleted(id);
    }

    @Override
    public SysUserDO getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public PageResult<SysUserDO> getUserPage(SysUserPageReqVO reqVO) {
        return userMapper.selectPage(reqVO, this.getDeptCondition(reqVO.getDeptId()));
    }

    @Override
    public SysUserDO getUser(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public List<SysUserDO> getUsers(SysUserExportReqVO reqVO) {
        return userMapper.selectList(reqVO, this.getDeptCondition(reqVO.getDeptId()));
    }

    @Override
    public List<SysUserDO> getUsers(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return userMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, SysUserDO> getUserMap(Collection<Long> ids) {
        return SysUserService.super.getUserMap(ids);
    }

    @Override
    public List<SysUserDO> getUsersByNickname(String nickname) {
        return userMapper.selectListByNickname(nickname);
    }

    @Override
    public List<SysUserDO> getUsersByUsername(String username) {
        return userMapper.selectListByUsername(username);
    }

    /**
     * 获得部门条件：查询指定部门的子部门编号们，包括自身
     *
     * @param deptId 部门编号
     * @return 部门编号集合
     */
    private Set<Long> getDeptCondition(Long deptId) {
        if (deptId == null) {
            return Collections.emptySet();
        }
        Set<Long> deptIds = CollectionUtils.convertSet(deptService.getDeptsByParentIdFromCache(
            deptId, true), SysDeptDO::getId);
        deptIds.add(deptId); // 包括自身
        return deptIds;
    }

    private void checkCreateOrUpdate(Long id, String username, String mobile, String email,
                                     Long deptId, Set<Long> postIds) {
        // 校验用户存在
        this.checkUserExists(id);
        // 校验用户名唯一
        this.checkUsernameUnique(id, username);
        // 校验手机号唯一
        this.checkMobileUnique(id, mobile);
        // 校验邮箱唯一
        this.checkEmailUnique(id, email);
        // 校验部门处于开启状态
        this.checkDeptEnable(deptId);
        // 校验岗位处于开启状态
        this.checkPostEnable(postIds);
    }

    @VisibleForTesting
    public void checkUserExists(Long id) {
        if (id == null) {
            return;
        }
        SysUserDO user = userMapper.selectById(id);
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }
    }

    @VisibleForTesting
    public void checkUsernameUnique(Long id, String username) {
        if (StrUtil.isBlank(username)) {
            return;
        }
        SysUserDO user = userMapper.selectByUsername(username);
        if (user == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的用户
        if (id == null) {
            throw exception(USER_USERNAME_EXISTS);
        }
        if (!user.getId().equals(id)) {
            throw exception(USER_USERNAME_EXISTS);
        }
    }

    @VisibleForTesting
    public void checkEmailUnique(Long id, String email) {
        if (StrUtil.isBlank(email)) {
            return;
        }
        SysUserDO user = userMapper.selectByEmail(email);
        if (user == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的用户
        if (id == null) {
            throw exception(USER_EMAIL_EXISTS);
        }
        if (!user.getId().equals(id)) {
            throw exception(USER_EMAIL_EXISTS);
        }
    }

    @VisibleForTesting
    public void checkMobileUnique(Long id, String mobile) {
        if (StrUtil.isBlank(mobile)) {
            return;
        }
        SysUserDO user = userMapper.selectByMobile(mobile);
        if (user == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的用户
        if (id == null) {
            throw exception(USER_MOBILE_EXISTS);
        }
        if (!user.getId().equals(id)) {
            throw exception(USER_MOBILE_EXISTS);
        }
    }

    @VisibleForTesting
    public void checkDeptEnable(Long deptId) {
        if (deptId == null) { // 允许不选择
            return;
        }
        SysDeptDO dept = deptService.getDept(deptId);
        if (dept == null) {
            throw exception(DEPT_NOT_FOUND);
        }
        if (!CommonStatusEnum.ENABLE.getStatus().equals(dept.getStatus())) {
            throw exception(DEPT_NOT_ENABLE);
        }
    }

    @VisibleForTesting
    public void checkPostEnable(Set<Long> postIds) {
        if (CollUtil.isEmpty(postIds)) { // 允许不选择
            return;
        }
        List<SysPostDO> posts = postService.getPosts(postIds, null);
        if (CollUtil.isEmpty(posts)) {
            throw exception(POST_NOT_FOUND);
        }
        Map<Long, SysPostDO> postMap = CollectionUtils.convertMap(posts, SysPostDO::getId);
        postIds.forEach(postId -> {
            SysPostDO post = postMap.get(postId);
            if (post == null) {
                throw exception(POST_NOT_FOUND);
            }
            if (!CommonStatusEnum.ENABLE.getStatus().equals(post.getStatus())) {
                throw exception(POST_NOT_ENABLE, post.getName());
            }
        });
    }

    /**
     * 校验旧密码
     *
     * @param id          用户 id
     * @param oldPassword 旧密码
     */
    @VisibleForTesting
    public void checkOldPassword(Long id, String oldPassword) {
        SysUserDO user = userMapper.selectById(id);
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw exception(USER_PASSWORD_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public SysUserImportRespVO importUsers(List<SysUserImportExcelVO> importUsers, boolean isUpdateSupport) {
        if (CollUtil.isEmpty(importUsers)) {
            throw exception(USER_IMPORT_LIST_IS_EMPTY);
        }
        SysUserImportRespVO respVO = SysUserImportRespVO.builder().createUsernames(new ArrayList<>())
            .updateUsernames(new ArrayList<>()).failureUsernames(new LinkedHashMap<>()).build();
        importUsers.forEach(importUser -> {
            // 校验，判断是否有不符合的原因
            try {
                checkCreateOrUpdate(null, null, importUser.getMobile(), importUser.getEmail(),
                    importUser.getDeptId(), null);
            } catch (ServiceException ex) {
                respVO.getFailureUsernames().put(importUser.getUsername(), ex.getMessage());
                return;
            }
            // 判断如果不存在，在进行插入
            SysUserDO existUser = userMapper.selectByUsername(importUser.getUsername());
            if (existUser == null) {
                userMapper.insert(SysUserConvert.INSTANCE.convert(importUser)
                    .setPassword(passwordEncoder.encode(userInitPassword))); // 设置默认密码
                respVO.getCreateUsernames().add(importUser.getUsername());
                return;
            }
            // 如果存在，判断是否允许更新
            if (!isUpdateSupport) {
                respVO.getFailureUsernames().put(importUser.getUsername(), USER_USERNAME_EXISTS.getMsg());
                return;
            }
            SysUserDO updateUser = SysUserConvert.INSTANCE.convert(importUser);
            updateUser.setId(existUser.getId());
            userMapper.updateById(updateUser);
            respVO.getUpdateUsernames().add(importUser.getUsername());
        });
        return respVO;
    }

    @Override
    public List<SysUserDO> getUsersByStatus(Integer status) {
        return userMapper.selectListByStatus(status);
    }

    @Override
    public List<SysUserDO> getUsersByPostIds(Collection<Long> postIds) {
        if (CollUtil.isEmpty(postIds)) {
            return Collections.emptyList();
        }
        // 过滤不符合条件的
        // TODO 芋艿：暂时只能内存过滤。解决方案：1、新建一个关联表；2、基于 where + 函数；3、json 字段，适合 mysql 8+ 版本
        List<SysUserDO> users = userMapper.selectList();
        users.removeIf(user -> !CollUtil.containsAny(user.getPostIds(), postIds));
        return users;
    }

    @Override
    public List<SysUserDO> getUsersByDeptIds(Collection<Long> deptIds) {
        if (CollUtil.isEmpty(deptIds)) {
            return Collections.emptyList();
        }
        return userMapper.selectListByDeptIds(deptIds);
    }

    @Override
    public void validUsers(Set<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 获得岗位信息
        List<SysUserDO> users = userMapper.selectBatchIds(ids);
        Map<Long, SysUserDO> userMap = CollectionUtils.convertMap(users, SysUserDO::getId);
        // 校验
        ids.forEach(id -> {
            SysUserDO user = userMap.get(id);
            if (user == null) {
                throw exception(USER_NOT_EXISTS);
            }
            if (!CommonStatusEnum.ENABLE.getStatus().equals(user.getStatus())) {
                throw exception(USER_IS_DISABLE, user.getNickname());
            }
        });
    }

}
