package cn.iocoder.dashboard.modules.system.service.user;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.dashboard.BaseSpringBootUnitTest;
import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserImportExcelVO;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserImportRespVO;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserUpdateReqVO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.dept.SysDeptDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dept.SysDeptMapper;
import cn.iocoder.dashboard.modules.system.service.dept.SysDeptService;
import cn.iocoder.dashboard.util.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.dashboard.util.RandomUtils.randomPojo;
import static cn.iocoder.dashboard.util.RandomUtils.randomString;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @Author zxl
 * @Date 2021/3/6 14:28
 * @Desc
 */
@Import(PasswordEncoder.class)
public class SysUserServiceImplTest  extends BaseSpringBootUnitTest {

    @Autowired
    private SysUserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SysDeptService deptService;
    @Resource
    private SysDeptMapper deptMapper;


    @Test
    public void test_creatUser(){
        // 准备参数
        SysUserCreateReqVO reqVO = randomPojo(SysUserCreateReqVO.class,o->{
            o.setDeptId(null);
            o.setPostIds(Collections.emptySet());
            o.setSex(1);
            o.setPassword(randomString());
        });
        // 调用方法
        Long  userId = userService.createUser(reqVO);
        // 校验数据是否存在
        assertNotNull(userService.getUser(userId));
    }

    @Test
    public void test_updateUser(){
        // 准备参数
        SysUserCreateReqVO reqVO = randomPojo(SysUserCreateReqVO.class,o->{
            o.setDeptId(null);
            o.setPostIds(Collections.emptySet());
            o.setSex(1);
            o.setPassword(randomString());
        });
        // 先插入一条
        Long userId = userService.createUser(reqVO);
        // 准备更新参数:更新手机号
        SysUserUpdateReqVO updateVo = new SysUserUpdateReqVO();
        updateVo.setId(userId);
        updateVo.setMobile(RandomUtil.randomNumbers(11));
        // 调用方法、
        userService.updateUser(updateVo);
        // 校验结果
        assertEquals(userService.getUser(userId).getMobile(),updateVo.getMobile());

    }

    @Test
    public void test_deleteUser(){
        // 准备参数
        SysUserCreateReqVO reqVO = randomPojo(SysUserCreateReqVO.class,o->{
            o.setDeptId(null);
            o.setPostIds(Collections.emptySet());
            o.setSex(1);
            o.setPassword(randomString());
        });
        // 先插入一条
        Long userId = userService.createUser(reqVO);
        // 调用数据
        userService.deleteUser(userId);
        // 校验结果
        assertNull(userService.getUser(userId));
    }

    @Test
    public void test_updateUserPassword(){
        // 准备参数
        SysUserCreateReqVO reqVO = randomPojo(SysUserCreateReqVO.class,o->{
            o.setDeptId(null);
            o.setPostIds(Collections.emptySet());
            o.setSex(1);
            o.setPassword("123");
        });
        // 先插入一条
        Long userId = userService.createUser(reqVO);
        String newPassword = RandomUtils.randomString();
        // 调用
        userService.updateUserPassword(userId,newPassword);
        // 校验结果
        assertNotEquals(passwordEncoder.encode(newPassword),userService.getUser(userId).getPassword());
    }

    @Test
    public void test_importUsers(){
        SysDeptDO dept = randomPojo(SysDeptDO.class, o -> { // 等会查询到
            o.setName("开发部");
            o.setSort("1");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        int depId = deptMapper.insert(dept);
        // 准备参数
        List<SysUserImportExcelVO> list = new ArrayList<>();
        list.add(randomPojo(SysUserImportExcelVO.class, o->{
            o.setDeptId(dept.getId());
            o.setSex(1);
            o.setStatus(1);
        }));
        list.add(randomPojo(SysUserImportExcelVO.class, o->{
            o.setDeptId(dept.getId());
            o.setSex(1);
            o.setStatus(1);
        }));
        list.add(randomPojo(SysUserImportExcelVO.class, o->{
            o.setDeptId(dept.getId());
            o.setSex(1);
            o.setStatus(1);
        }));
        // 批量插入
        SysUserImportRespVO respVO = userService.importUsers(list,false);
        System.out.println(respVO.getCreateUsernames().size());
        // 校验结果
        assertEquals(respVO.getCreateUsernames().size(),3);
        // 批量更新
        list.get(0).setSex(0);
        SysUserImportRespVO respVOUpdate = userService.importUsers(list,true);
        System.out.println(respVOUpdate.getUpdateUsernames().size());
        // 校验结果
        assertEquals(respVOUpdate.getUpdateUsernames().size(),3);
    }

    public SysUserCreateReqVO randomUserVO(){

        SysUserCreateReqVO userVO = new SysUserCreateReqVO();
        userVO.setUsername(RandomUtils.randomString());
        userVO.setNickname(RandomUtils.randomString());
        userVO.setMobile(RandomUtil.randomNumbers(11));
        userVO.setEmail(RandomUtils.randomString()+"@ruoyi.com");
        userVO.setDeptId(null);
        userVO.setPostIds(Collections.emptySet());

        userVO.setPassword(RandomUtils.randomString());
        return userVO;

    }
}
