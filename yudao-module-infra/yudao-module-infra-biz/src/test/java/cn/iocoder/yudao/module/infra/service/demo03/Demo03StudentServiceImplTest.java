package cn.iocoder.yudao.module.infra.service.demo03;

import cn.iocoder.yudao.module.infra.service.demo.demo03.Demo03StudentServiceImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.infra.controller.admin.demo.demo03.normal.vo.*;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo.demo03.Demo03StudentDO;
import cn.iocoder.yudao.module.infra.dal.mysql.demo.demo03.Demo03StudentMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.springframework.context.annotation.Import;

import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link Demo03StudentServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(Demo03StudentServiceImpl.class)
public class Demo03StudentServiceImplTest extends BaseDbUnitTest {

    @Resource
    private Demo03StudentServiceImpl demo03StudentService;

    @Resource
    private Demo03StudentMapper demo03StudentMapper;

    @Test
    public void testCreateDemo03Student_success() {
        // 准备参数
        Demo03StudentSaveReqVO createReqVO = randomPojo(Demo03StudentSaveReqVO.class).setId(null);

        // 调用
        Long demo03StudentId = demo03StudentService.createDemo03Student(createReqVO);
        // 断言
        assertNotNull(demo03StudentId);
        // 校验记录的属性是否正确
        Demo03StudentDO demo03Student = demo03StudentMapper.selectById(demo03StudentId);
        assertPojoEquals(createReqVO, demo03Student, "id");
    }

    @Test
    public void testUpdateDemo03Student_success() {
        // mock 数据
        Demo03StudentDO dbDemo03Student = randomPojo(Demo03StudentDO.class);
        demo03StudentMapper.insert(dbDemo03Student);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Demo03StudentSaveReqVO updateReqVO = randomPojo(Demo03StudentSaveReqVO.class, o -> {
            o.setId(dbDemo03Student.getId()); // 设置更新的 ID
        });

        // 调用
        demo03StudentService.updateDemo03Student(updateReqVO);
        // 校验是否更新正确
        Demo03StudentDO demo03Student = demo03StudentMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, demo03Student);
    }

    @Test
    public void testUpdateDemo03Student_notExists() {
        // 准备参数
        Demo03StudentSaveReqVO updateReqVO = randomPojo(Demo03StudentSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> demo03StudentService.updateDemo03Student(updateReqVO), DEMO03_STUDENT_NOT_EXISTS);
    }

    @Test
    public void testDeleteDemo03Student_success() {
        // mock 数据
        Demo03StudentDO dbDemo03Student = randomPojo(Demo03StudentDO.class);
        demo03StudentMapper.insert(dbDemo03Student);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbDemo03Student.getId();

        // 调用
        demo03StudentService.deleteDemo03Student(id);
       // 校验数据不存在了
       assertNull(demo03StudentMapper.selectById(id));
    }

    @Test
    public void testDeleteDemo03Student_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> demo03StudentService.deleteDemo03Student(id), DEMO03_STUDENT_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetDemo03StudentPage() {
       // mock 数据
       Demo03StudentDO dbDemo03Student = randomPojo(Demo03StudentDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setSex(null);
           o.setDescription(null);
           o.setCreateTime(null);
       });
       demo03StudentMapper.insert(dbDemo03Student);
       // 测试 name 不匹配
       demo03StudentMapper.insert(cloneIgnoreId(dbDemo03Student, o -> o.setName(null)));
       // 测试 sex 不匹配
       demo03StudentMapper.insert(cloneIgnoreId(dbDemo03Student, o -> o.setSex(null)));
       // 测试 description 不匹配
       demo03StudentMapper.insert(cloneIgnoreId(dbDemo03Student, o -> o.setDescription(null)));
       // 测试 createTime 不匹配
       demo03StudentMapper.insert(cloneIgnoreId(dbDemo03Student, o -> o.setCreateTime(null)));
       // 准备参数
       Demo03StudentPageReqVO reqVO = new Demo03StudentPageReqVO();
       reqVO.setName(null);
       reqVO.setSex(null);
       reqVO.setDescription(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<Demo03StudentDO> pageResult = demo03StudentService.getDemo03StudentPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbDemo03Student, pageResult.getList().get(0));
    }

}