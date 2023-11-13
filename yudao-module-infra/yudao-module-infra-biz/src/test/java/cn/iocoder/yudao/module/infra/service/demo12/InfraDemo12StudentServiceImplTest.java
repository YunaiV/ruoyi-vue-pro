package cn.iocoder.yudao.module.infra.service.demo12;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.infra.controller.admin.demo12.vo.*;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo12.InfraDemo12StudentDO;
import cn.iocoder.yudao.module.infra.dal.mysql.demo12.InfraDemo12StudentMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import javax.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link InfraDemo12StudentServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(InfraDemo12StudentServiceImpl.class)
public class InfraDemo12StudentServiceImplTest extends BaseDbUnitTest {

    @Resource
    private InfraDemo12StudentServiceImpl demo12StudentService;

    @Resource
    private InfraDemo12StudentMapper demo12StudentMapper;

    @Test
    public void testCreateDemo12Student_success() {
        // 准备参数
        InfraDemo12StudentCreateReqVO reqVO = randomPojo(InfraDemo12StudentCreateReqVO.class);

        // 调用
        Long demo12StudentId = demo12StudentService.createDemo12Student(reqVO);
        // 断言
        assertNotNull(demo12StudentId);
        // 校验记录的属性是否正确
        InfraDemo12StudentDO demo12Student = demo12StudentMapper.selectById(demo12StudentId);
        assertPojoEquals(reqVO, demo12Student);
    }

    @Test
    public void testUpdateDemo12Student_success() {
        // mock 数据
        InfraDemo12StudentDO dbDemo12Student = randomPojo(InfraDemo12StudentDO.class);
        demo12StudentMapper.insert(dbDemo12Student);// @Sql: 先插入出一条存在的数据
        // 准备参数
        InfraDemo12StudentUpdateReqVO reqVO = randomPojo(InfraDemo12StudentUpdateReqVO.class, o -> {
            o.setId(dbDemo12Student.getId()); // 设置更新的 ID
        });

        // 调用
        demo12StudentService.updateDemo12Student(reqVO);
        // 校验是否更新正确
        InfraDemo12StudentDO demo12Student = demo12StudentMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, demo12Student);
    }

    @Test
    public void testUpdateDemo12Student_notExists() {
        // 准备参数
        InfraDemo12StudentUpdateReqVO reqVO = randomPojo(InfraDemo12StudentUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> demo12StudentService.updateDemo12Student(reqVO), DEMO12_STUDENT_NOT_EXISTS);
    }

    @Test
    public void testDeleteDemo12Student_success() {
        // mock 数据
        InfraDemo12StudentDO dbDemo12Student = randomPojo(InfraDemo12StudentDO.class);
        demo12StudentMapper.insert(dbDemo12Student);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbDemo12Student.getId();

        // 调用
        demo12StudentService.deleteDemo12Student(id);
       // 校验数据不存在了
       assertNull(demo12StudentMapper.selectById(id));
    }

    @Test
    public void testDeleteDemo12Student_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> demo12StudentService.deleteDemo12Student(id), DEMO12_STUDENT_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetDemo12StudentPage() {
       // mock 数据
       InfraDemo12StudentDO dbDemo12Student = randomPojo(InfraDemo12StudentDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setBirthday(null);
           o.setSex(null);
           o.setEnabled(null);
           o.setCreateTime(null);
       });
       demo12StudentMapper.insert(dbDemo12Student);
       // 测试 name 不匹配
       demo12StudentMapper.insert(cloneIgnoreId(dbDemo12Student, o -> o.setName(null)));
       // 测试 birthday 不匹配
       demo12StudentMapper.insert(cloneIgnoreId(dbDemo12Student, o -> o.setBirthday(null)));
       // 测试 sex 不匹配
       demo12StudentMapper.insert(cloneIgnoreId(dbDemo12Student, o -> o.setSex(null)));
       // 测试 enabled 不匹配
       demo12StudentMapper.insert(cloneIgnoreId(dbDemo12Student, o -> o.setEnabled(null)));
       // 测试 createTime 不匹配
       demo12StudentMapper.insert(cloneIgnoreId(dbDemo12Student, o -> o.setCreateTime(null)));
       // 准备参数
       InfraDemo12StudentPageReqVO reqVO = new InfraDemo12StudentPageReqVO();
       reqVO.setName(null);
       reqVO.setBirthday(null);
       reqVO.setSex(null);
       reqVO.setEnabled(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<InfraDemo12StudentDO> pageResult = demo12StudentService.getDemo12StudentPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbDemo12Student, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetDemo12StudentList() {
       // mock 数据
       InfraDemo12StudentDO dbDemo12Student = randomPojo(InfraDemo12StudentDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setBirthday(null);
           o.setSex(null);
           o.setEnabled(null);
           o.setCreateTime(null);
       });
       demo12StudentMapper.insert(dbDemo12Student);
       // 测试 name 不匹配
       demo12StudentMapper.insert(cloneIgnoreId(dbDemo12Student, o -> o.setName(null)));
       // 测试 birthday 不匹配
       demo12StudentMapper.insert(cloneIgnoreId(dbDemo12Student, o -> o.setBirthday(null)));
       // 测试 sex 不匹配
       demo12StudentMapper.insert(cloneIgnoreId(dbDemo12Student, o -> o.setSex(null)));
       // 测试 enabled 不匹配
       demo12StudentMapper.insert(cloneIgnoreId(dbDemo12Student, o -> o.setEnabled(null)));
       // 测试 createTime 不匹配
       demo12StudentMapper.insert(cloneIgnoreId(dbDemo12Student, o -> o.setCreateTime(null)));
       // 准备参数
       InfraDemo12StudentExportReqVO reqVO = new InfraDemo12StudentExportReqVO();
       reqVO.setName(null);
       reqVO.setBirthday(null);
       reqVO.setSex(null);
       reqVO.setEnabled(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<InfraDemo12StudentDO> list = demo12StudentService.getDemo12StudentList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbDemo12Student, list.get(0));
    }

}