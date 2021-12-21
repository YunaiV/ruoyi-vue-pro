package cn.iocoder.yudao.adminserver.modules.system.service.dept;

import cn.iocoder.yudao.adminserver.BaseDbUnitTest;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.adminserver.modules.system.controller.dept.vo.dept.SysDeptCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.dept.vo.dept.SysDeptListReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.dept.vo.dept.SysDeptUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.dept.SysDeptDO;
import cn.iocoder.yudao.adminserver.modules.system.dal.mysql.dept.SysDeptMapper;
import cn.iocoder.yudao.adminserver.modules.system.enums.dept.DeptIdEnum;
import cn.iocoder.yudao.adminserver.modules.system.mq.producer.dept.SysDeptProducer;
import cn.iocoder.yudao.adminserver.modules.system.service.dept.impl.SysDeptServiceImpl;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import com.google.common.collect.Multimap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static cn.hutool.core.bean.BeanUtil.getFieldValue;
import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.adminserver.modules.system.enums.SysErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * {@link SysDeptServiceImpl} 的单元测试类
 *
 * @author niudehua
 */
@Import(SysDeptServiceImpl.class)
class SysDeptServiceTest extends BaseDbUnitTest {

    @Resource
    private SysDeptServiceImpl deptService;
    @Resource
    private SysDeptMapper deptMapper;
    @MockBean
    private SysDeptProducer deptProducer;

    @Test
    @SuppressWarnings("unchecked")
    void testInitLocalCache() {
        // mock 数据
        SysDeptDO deptDO1 = randomDeptDO();
        deptMapper.insert(deptDO1);
        SysDeptDO deptDO2 = randomDeptDO();
        deptMapper.insert(deptDO2);

        // 调用
        deptService.initLocalCache();
        // 断言 deptCache 缓存
        Map<Long, SysDeptDO> deptCache = (Map<Long, SysDeptDO>) getFieldValue(deptService, "deptCache");
        assertEquals(2, deptCache.size());
        assertPojoEquals(deptDO1, deptCache.get(deptDO1.getId()));
        assertPojoEquals(deptDO2, deptCache.get(deptDO2.getId()));
        // 断言 parentDeptCache 缓存
        Multimap<Long, SysDeptDO> parentDeptCache = (Multimap<Long, SysDeptDO>) getFieldValue(deptService, "parentDeptCache");
        assertEquals(2, parentDeptCache.size());
        assertPojoEquals(deptDO1, parentDeptCache.get(deptDO1.getParentId()));
        assertPojoEquals(deptDO2, parentDeptCache.get(deptDO2.getParentId()));
        // 断言 maxUpdateTime 缓存
        Date maxUpdateTime = (Date) getFieldValue(deptService, "maxUpdateTime");
        assertEquals(ObjectUtils.max(deptDO1.getUpdateTime(), deptDO2.getUpdateTime()), maxUpdateTime);
    }

    @Test
    void testListDepts() {
        // mock 数据
        SysDeptDO dept = randomPojo(SysDeptDO.class, o -> { // 等会查询到
            o.setName("开发部");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        deptMapper.insert(dept);
        // 测试 name 不匹配
        deptMapper.insert(ObjectUtils.cloneIgnoreId(dept, o -> o.setName("发")));
        // 测试 status 不匹配
        deptMapper.insert(ObjectUtils.cloneIgnoreId(dept, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // 准备参数
        SysDeptListReqVO reqVO = new SysDeptListReqVO();
        reqVO.setName("开");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        // 调用
        List<SysDeptDO> sysDeptDOS = deptService.getSimpleDepts(reqVO);
        // 断言
        assertEquals(1, sysDeptDOS.size());
        assertPojoEquals(dept, sysDeptDOS.get(0));
    }

    @Test
    void testCreateDept_success() {
        // 准备参数
        SysDeptCreateReqVO reqVO = randomPojo(SysDeptCreateReqVO.class,
            o -> {
                o.setParentId(DeptIdEnum.ROOT.getId());
                o.setStatus(randomCommonStatus());
            });
        // 调用
        Long deptId = deptService.createDept(reqVO);
        // 断言
        assertNotNull(deptId);
        // 校验记录的属性是否正确
        SysDeptDO deptDO = deptMapper.selectById(deptId);
        assertPojoEquals(reqVO, deptDO);
        // 校验调用
        verify(deptProducer, times(1)).sendDeptRefreshMessage();
    }

    @Test
    void testUpdateDept_success() {
        // mock 数据
        SysDeptDO dbDeptDO = randomPojo(SysDeptDO.class, o -> o.setStatus(randomCommonStatus()));
        deptMapper.insert(dbDeptDO);// @Sql: 先插入出一条存在的数据
        // 准备参数
        SysDeptUpdateReqVO reqVO = randomPojo(SysDeptUpdateReqVO.class, o -> {
            // 设置更新的 ID
            o.setParentId(DeptIdEnum.ROOT.getId());
            o.setId(dbDeptDO.getId());
            o.setStatus(randomCommonStatus());
        });
        // 调用
        deptService.updateDept(reqVO);
        // 校验是否更新正确
        SysDeptDO deptDO = deptMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, deptDO);
    }

    @Test
    void testDeleteDept_success() {
        // mock 数据
        SysDeptDO dbDeptDO = randomPojo(SysDeptDO.class, o -> o.setStatus(randomCommonStatus()));
        deptMapper.insert(dbDeptDO);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbDeptDO.getId();
        // 调用
        deptService.deleteDept(id);
        // 校验数据不存在了
        assertNull(deptMapper.selectById(id));
    }

    @Test
    void testCheckDept_nameDuplicateForUpdate() {
        // mock 数据
        SysDeptDO deptDO = randomDeptDO();
        // 设置根节点部门
        deptDO.setParentId(DeptIdEnum.ROOT.getId());
        deptMapper.insert(deptDO);
        // mock 数据 稍后模拟重复它的 name
        SysDeptDO nameDeptDO = randomDeptDO();
        // 设置根节点部门
        nameDeptDO.setParentId(DeptIdEnum.ROOT.getId());
        deptMapper.insert(nameDeptDO);
        // 准备参数
        SysDeptUpdateReqVO reqVO = randomPojo(SysDeptUpdateReqVO.class,
            o -> {
                // 设置根节点部门
                o.setParentId(DeptIdEnum.ROOT.getId());
                // 设置更新的 ID
                o.setId(deptDO.getId());
                // 模拟 name 重复
                o.setName(nameDeptDO.getName());
            });
        // 调用, 并断言异常
        assertServiceException(() -> deptService.updateDept(reqVO), DEPT_NAME_DUPLICATE);

    }

    @Test
    void testCheckDept_parentNotExitsForCreate() {
        SysDeptCreateReqVO reqVO = randomPojo(SysDeptCreateReqVO.class,
            o -> o.setStatus(randomCommonStatus()));
        // 调用,并断言异常
        assertServiceException(() -> deptService.createDept(reqVO), DEPT_PARENT_NOT_EXITS);
    }

    @Test
    void testCheckDept_notFoundForDelete() {
        // 准备参数
        Long id = randomLongId();
        // 调用, 并断言异常
        assertServiceException(() -> deptService.deleteDept(id), DEPT_NOT_FOUND);
    }

    @Test
    void testCheckDept_exitsChildrenForDelete() {
        // mock 数据
        SysDeptDO parentDept = randomPojo(SysDeptDO.class, o -> o.setStatus(randomCommonStatus()));
        deptMapper.insert(parentDept);// @Sql: 先插入出一条存在的数据
        // 准备参数
        SysDeptDO childrenDeptDO = randomPojo(SysDeptDO.class, o -> {
            o.setParentId(parentDept.getId());
            o.setStatus(randomCommonStatus());
        });
        // 插入子部门
        deptMapper.insert(childrenDeptDO);
        // 调用, 并断言异常
        assertServiceException(() -> deptService.deleteDept(parentDept.getId()), DEPT_EXITS_CHILDREN);
    }

    @Test
    void testCheckDept_parentErrorForUpdate() {
        // mock 数据
        SysDeptDO dbDeptDO = randomPojo(SysDeptDO.class, o -> o.setStatus(randomCommonStatus()));
        deptMapper.insert(dbDeptDO);
        // 准备参数
        SysDeptUpdateReqVO reqVO = randomPojo(SysDeptUpdateReqVO.class,
            o -> {
                // 设置自己为父部门
                o.setParentId(dbDeptDO.getId());
                // 设置更新的 ID
                o.setId(dbDeptDO.getId());
            });
        // 调用, 并断言异常
        assertServiceException(() -> deptService.updateDept(reqVO), DEPT_PARENT_ERROR);
    }

    @Test
    void testCheckDept_notEnableForCreate() {
        // mock 数据
        SysDeptDO deptDO = randomPojo(SysDeptDO.class, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus()));
        deptMapper.insert(deptDO);
        // 准备参数
        SysDeptCreateReqVO reqVO = randomPojo(SysDeptCreateReqVO.class,
            o -> {
                // 设置未启用的部门为副部门
                o.setParentId(deptDO.getId());
            });
        // 调用, 并断言异常
        assertServiceException(() -> deptService.createDept(reqVO), DEPT_NOT_ENABLE);
    }

    @Test
    void testCheckDept_parentIsChildForUpdate() {
        // mock 数据
        SysDeptDO parentDept = randomPojo(SysDeptDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        deptMapper.insert(parentDept);
        SysDeptDO childDept = randomPojo(SysDeptDO.class, o -> {
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setParentId(parentDept.getId());
        });
        deptMapper.insert(childDept);
        // 初始化本地缓存
        deptService.initLocalCache();
        // 准备参数
        SysDeptUpdateReqVO reqVO = randomPojo(SysDeptUpdateReqVO.class,
            o -> {
                // 设置自己的子部门为父部门
                o.setParentId(childDept.getId());
                // 设置更新的 ID
                o.setId(parentDept.getId());
            });
        // 调用, 并断言异常
        assertServiceException(() -> deptService.updateDept(reqVO), DEPT_PARENT_IS_CHILD);
    }

    @SafeVarargs
    private static SysDeptDO randomDeptDO(Consumer<SysDeptDO>... consumers) {
        Consumer<SysDeptDO> consumer = (o) -> {
            o.setStatus(randomEle(CommonStatusEnum.values()).getStatus()); // 保证 status 的范围
        };
        return randomPojo(SysDeptDO.class, ArrayUtils.append(consumer, consumers));
    }

}
