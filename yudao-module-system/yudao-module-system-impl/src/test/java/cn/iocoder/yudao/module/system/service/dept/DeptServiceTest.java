package cn.iocoder.yudao.module.system.service.dept;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptListReqVO;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptUpdateReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.dal.mysql.dept.DeptMapper;
import cn.iocoder.yudao.module.system.enums.dept.DeptIdEnum;
import cn.iocoder.yudao.module.system.mq.producer.dept.DeptProducer;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
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
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * {@link DeptServiceImpl} 的单元测试类
 *
 * @author niudehua
 */
@Import(DeptServiceImpl.class)
public class DeptServiceTest extends BaseDbUnitTest {

    @Resource
    private DeptServiceImpl deptService;
    @Resource
    private DeptMapper deptMapper;
    @MockBean
    private DeptProducer deptProducer;

    @Test
    @SuppressWarnings("unchecked")
    void testInitLocalCache() {
        // mock 数据
        DeptDO deptDO1 = randomDeptDO();
        deptMapper.insert(deptDO1);
        DeptDO deptDO2 = randomDeptDO();
        deptMapper.insert(deptDO2);

        // 调用
        deptService.initLocalCache();
        // 断言 deptCache 缓存
        Map<Long, DeptDO> deptCache = (Map<Long, DeptDO>) getFieldValue(deptService, "deptCache");
        assertEquals(2, deptCache.size());
        assertPojoEquals(deptDO1, deptCache.get(deptDO1.getId()));
        assertPojoEquals(deptDO2, deptCache.get(deptDO2.getId()));
        // 断言 parentDeptCache 缓存
        Multimap<Long, DeptDO> parentDeptCache = (Multimap<Long, DeptDO>) getFieldValue(deptService, "parentDeptCache");
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
        DeptDO dept = randomPojo(DeptDO.class, o -> { // 等会查询到
            o.setName("开发部");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        deptMapper.insert(dept);
        // 测试 name 不匹配
        deptMapper.insert(ObjectUtils.cloneIgnoreId(dept, o -> o.setName("发")));
        // 测试 status 不匹配
        deptMapper.insert(ObjectUtils.cloneIgnoreId(dept, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // 准备参数
        DeptListReqVO reqVO = new DeptListReqVO();
        reqVO.setName("开");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        // 调用
        List<DeptDO> sysDeptDOS = deptService.getSimpleDepts(reqVO);
        // 断言
        assertEquals(1, sysDeptDOS.size());
        assertPojoEquals(dept, sysDeptDOS.get(0));
    }

    @Test
    void testCreateDept_success() {
        // 准备参数
        DeptCreateReqVO reqVO = randomPojo(DeptCreateReqVO.class,
            o -> {
                o.setParentId(DeptIdEnum.ROOT.getId());
                o.setStatus(randomCommonStatus());
            });
        // 调用
        Long deptId = deptService.createDept(reqVO);
        // 断言
        assertNotNull(deptId);
        // 校验记录的属性是否正确
        DeptDO deptDO = deptMapper.selectById(deptId);
        assertPojoEquals(reqVO, deptDO);
        // 校验调用
        verify(deptProducer, times(1)).sendDeptRefreshMessage();
    }

    @Test
    void testUpdateDept_success() {
        // mock 数据
        DeptDO dbDeptDO = randomPojo(DeptDO.class, o -> o.setStatus(randomCommonStatus()));
        deptMapper.insert(dbDeptDO);// @Sql: 先插入出一条存在的数据
        // 准备参数
        DeptUpdateReqVO reqVO = randomPojo(DeptUpdateReqVO.class, o -> {
            // 设置更新的 ID
            o.setParentId(DeptIdEnum.ROOT.getId());
            o.setId(dbDeptDO.getId());
            o.setStatus(randomCommonStatus());
        });
        // 调用
        deptService.updateDept(reqVO);
        // 校验是否更新正确
        DeptDO deptDO = deptMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, deptDO);
    }

    @Test
    void testDeleteDept_success() {
        // mock 数据
        DeptDO dbDeptDO = randomPojo(DeptDO.class, o -> o.setStatus(randomCommonStatus()));
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
        DeptDO deptDO = randomDeptDO();
        // 设置根节点部门
        deptDO.setParentId(DeptIdEnum.ROOT.getId());
        deptMapper.insert(deptDO);
        // mock 数据 稍后模拟重复它的 name
        DeptDO nameDeptDO = randomDeptDO();
        // 设置根节点部门
        nameDeptDO.setParentId(DeptIdEnum.ROOT.getId());
        deptMapper.insert(nameDeptDO);
        // 准备参数
        DeptUpdateReqVO reqVO = randomPojo(DeptUpdateReqVO.class,
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
        DeptCreateReqVO reqVO = randomPojo(DeptCreateReqVO.class,
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
        DeptDO parentDept = randomPojo(DeptDO.class, o -> o.setStatus(randomCommonStatus()));
        deptMapper.insert(parentDept);// @Sql: 先插入出一条存在的数据
        // 准备参数
        DeptDO childrenDeptDO = randomPojo(DeptDO.class, o -> {
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
        DeptDO dbDeptDO = randomPojo(DeptDO.class, o -> o.setStatus(randomCommonStatus()));
        deptMapper.insert(dbDeptDO);
        // 准备参数
        DeptUpdateReqVO reqVO = randomPojo(DeptUpdateReqVO.class,
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
        DeptDO deptDO = randomPojo(DeptDO.class, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus()));
        deptMapper.insert(deptDO);
        // 准备参数
        DeptCreateReqVO reqVO = randomPojo(DeptCreateReqVO.class,
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
        DeptDO parentDept = randomPojo(DeptDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        deptMapper.insert(parentDept);
        DeptDO childDept = randomPojo(DeptDO.class, o -> {
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setParentId(parentDept.getId());
        });
        deptMapper.insert(childDept);
        // 初始化本地缓存
        deptService.initLocalCache();
        // 准备参数
        DeptUpdateReqVO reqVO = randomPojo(DeptUpdateReqVO.class,
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
    private static DeptDO randomDeptDO(Consumer<DeptDO>... consumers) {
        Consumer<DeptDO> consumer = (o) -> {
            o.setStatus(randomEle(CommonStatusEnum.values()).getStatus()); // 保证 status 的范围
        };
        return randomPojo(DeptDO.class, ArrayUtils.append(consumer, consumers));
    }

}
