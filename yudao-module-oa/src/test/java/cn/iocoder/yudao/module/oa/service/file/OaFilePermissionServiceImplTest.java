package cn.iocoder.yudao.module.oa.service.file;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.oa.controller.admin.file.vo.permission.OaFilePermissionSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.file.OaFilePermissionDO;
import cn.iocoder.yudao.module.oa.dal.mysql.file.OaFilePermissionMapper;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link OaFilePermissionServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(OaFilePermissionServiceImpl.class)
public class OaFilePermissionServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaFilePermissionService filePermissionService;

    @Resource
    private OaFilePermissionMapper filePermissionMapper;

    @MockitoBean
    private OaFileNodeService fileNodeService;
    @MockitoBean
    private AdminUserApi adminUserApi;
    @MockitoBean
    private DeptApi deptApi;

    @Test
    public void testSaveFilePermission_createUser() {
        // 准备参数
        OaFilePermissionSaveReqVO reqVO = new OaFilePermissionSaveReqVO().setNodeId(10L)
                .setSubjectType(1).setSubjectId(20L).setLevel(1).setInherit(true);

        // 调用
        Long id = filePermissionService.saveFilePermission(reqVO, 1L);
        // 断言
        OaFilePermissionDO permission = filePermissionMapper.selectById(id);
        assertEquals(10L, permission.getNodeId());
        assertEquals(20L, permission.getSubjectId());
        assertEquals(1, permission.getLevel());
        verify(adminUserApi).validateUser(20L);
        verify(fileNodeService).validateFileNodePermission(10L, 1L, 4);
        verifyNoInteractions(deptApi);
    }

    @Test
    public void testSaveFilePermission_updateAndClearExpireTime() {
        // mock 数据
        OaFilePermissionDO permission = randomFilePermissionDO(10L, 1, 20L);
        permission.setExpireTime(LocalDateTime.now().plusDays(1));
        filePermissionMapper.insert(permission);
        // 准备参数
        OaFilePermissionSaveReqVO reqVO = new OaFilePermissionSaveReqVO().setNodeId(10L)
                .setSubjectType(1).setSubjectId(20L).setLevel(3).setInherit(false);

        // 调用
        Long id = filePermissionService.saveFilePermission(reqVO, 1L);
        // 断言
        assertEquals(permission.getId(), id);
        assertEquals(1, filePermissionMapper.selectList().size());
        OaFilePermissionDO result = filePermissionMapper.selectById(id);
        assertEquals(3, result.getLevel());
        assertFalse(result.getInherit());
        assertNull(result.getExpireTime());
    }

    @Test
    public void testSaveFilePermission_departmentUsesIndependentSubject() {
        // mock 数据：同编号用户授权不能被部门授权覆盖
        OaFilePermissionDO userPermission = randomFilePermissionDO(10L, 1, 20L);
        filePermissionMapper.insert(userPermission);
        // 准备参数
        OaFilePermissionSaveReqVO reqVO = new OaFilePermissionSaveReqVO().setNodeId(10L)
                .setSubjectType(2).setSubjectId(20L).setLevel(2).setInherit(true);

        // 调用
        Long id = filePermissionService.saveFilePermission(reqVO, 1L);
        // 断言
        assertNotEquals(userPermission.getId(), id);
        assertEquals(2, filePermissionMapper.selectList().size());
        verify(deptApi).validateDeptList(Collections.singleton(20L));
        verifyNoInteractions(adminUserApi);
    }

    @Test
    public void testSaveFilePermission_invalidSubjectType() {
        // 准备参数
        OaFilePermissionSaveReqVO reqVO = new OaFilePermissionSaveReqVO().setNodeId(10L)
                .setSubjectType(99).setSubjectId(20L).setLevel(1).setInherit(true);

        // 调用，并断言
        assertThrows(IllegalArgumentException.class, () -> filePermissionService.saveFilePermission(reqVO, 1L));
        assertTrue(filePermissionMapper.selectList().isEmpty());
        verifyNoInteractions(adminUserApi, deptApi);
    }

    @Test
    public void testGetFilePermissionListByNodeIds() {
        // mock 数据
        OaFilePermissionDO permission = randomFilePermissionDO(10L, 1, 20L);
        filePermissionMapper.insert(permission);
        filePermissionMapper.insert(randomFilePermissionDO(30L, 1, 20L));

        // 调用，并断言
        assertEquals(permission.getId(), CollUtil.getFirst(filePermissionService.getFilePermissionListByNodeIds(Collections.singleton(10L))).getId());
        assertTrue(filePermissionService.getFilePermissionListByNodeIds(Collections.emptyList()).isEmpty());
    }

    @Test
    public void testDeleteFilePermissionsByNodeIds() {
        // mock 数据
        OaFilePermissionDO first = randomFilePermissionDO(10L, 1, 20L);
        OaFilePermissionDO second = randomFilePermissionDO(11L, 1, 20L);
        OaFilePermissionDO retained = randomFilePermissionDO(12L, 1, 20L);
        filePermissionMapper.insert(first);
        filePermissionMapper.insert(second);
        filePermissionMapper.insert(retained);

        // 调用
        filePermissionService.deleteFilePermissionsByNodeIds(Collections.emptyList());
        filePermissionService.deleteFilePermissionsByNodeIds(Arrays.asList(10L, 11L));
        // 断言
        assertNull(filePermissionMapper.selectById(first.getId()));
        assertNull(filePermissionMapper.selectById(second.getId()));
        assertNotNull(filePermissionMapper.selectById(retained.getId()));
    }

    @Test
    public void testGetValidFilePermissionList_userAndDepartment() {
        // mock 数据：本人及所属部门有效授权保留，过期、其他人和已删除授权排除
        OaFilePermissionDO user = randomFilePermissionDO(1L, 1, 10L).setExpireTime(null);
        filePermissionMapper.insert(user);
        OaFilePermissionDO dept = randomFilePermissionDO(2L, 2, 100L).setExpireTime(LocalDateTime.now().plusDays(1));
        filePermissionMapper.insert(dept);
        filePermissionMapper.insert(randomFilePermissionDO(3L, 1, 10L).setExpireTime(LocalDateTime.now().minusDays(1)));
        filePermissionMapper.insert(randomFilePermissionDO(4L, 1, 20L).setExpireTime(null));
        OaFilePermissionDO deleted = randomFilePermissionDO(5L, 1, 10L).setExpireTime(null);
        filePermissionMapper.insert(deleted);
        filePermissionMapper.deleteById(deleted.getId());

        // 调用
        List<OaFilePermissionDO> result = filePermissionService.getValidFilePermissionList(10L, 100L);
        // 断言
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(permission -> permission.getId().equals(user.getId())));
        assertTrue(result.stream().anyMatch(permission -> permission.getId().equals(dept.getId())));
    }

    @Test
    public void testGetValidFilePermissionList_withoutDepartment() {
        // mock 数据：没有部门时仅匹配本人授权
        OaFilePermissionDO user = randomFilePermissionDO(1L, 1, 10L).setExpireTime(null);
        filePermissionMapper.insert(user);
        filePermissionMapper.insert(randomFilePermissionDO(2L, 2, 10L).setExpireTime(null));

        // 调用
        List<OaFilePermissionDO> result = filePermissionService.getValidFilePermissionList(10L, null);
        // 断言
        assertEquals(1, result.size());
        assertEquals(user.getId(), CollUtil.getFirst(result).getId());
    }

    // ========== 随机对象 ==========

    /**
     * 构造允许继承且永不过期的查看权限。
     *
     * @param nodeId 文件节点编号
     * @param subjectType 授权对象类型
     * @param subjectId 授权对象编号
     * @return 未入库的测试对象
     */
    private static OaFilePermissionDO randomFilePermissionDO(Long nodeId, Integer subjectType, Long subjectId) {
        return randomPojo(OaFilePermissionDO.class, permission -> {
            permission.setNodeId(nodeId);
            permission.setSubjectType(subjectType);
            permission.setSubjectId(subjectId);
            permission.setLevel(1);
            permission.setInherit(true);
            permission.setExpireTime(null);
        });
    }

}
