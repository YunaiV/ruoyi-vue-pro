package cn.iocoder.yudao.module.weapp.service.appsclass;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.weapp.controller.admin.appsclass.vo.*;
import cn.iocoder.yudao.module.weapp.dal.dataobject.appsclass.AppsClassDO;
import cn.iocoder.yudao.module.weapp.dal.mysql.appsclass.AppsClassMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import javax.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static cn.iocoder.yudao.module.weapp.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link AppsClassServiceImpl} 的单元测试类
 *
 * @author jingjianqian
 */
@Import(AppsClassServiceImpl.class)
public class AppsClassServiceImplTest extends BaseDbUnitTest {

    @Resource
    private AppsClassServiceImpl appsClassService;

    @Resource
    private AppsClassMapper appsClassMapper;

    @Test
    public void testCreateAppsClass_success() {
        // 准备参数
        AppsClassSaveReqVO createReqVO = randomPojo(AppsClassSaveReqVO.class).setId(null);

        // 调用
        Integer appsClassId = appsClassService.createAppsClass(createReqVO);
        // 断言
        assertNotNull(appsClassId);
        // 校验记录的属性是否正确
        AppsClassDO appsClass = appsClassMapper.selectById(appsClassId);
        assertPojoEquals(createReqVO, appsClass, "id");
    }

    @Test
    public void testUpdateAppsClass_success() {
        // mock 数据
        AppsClassDO dbAppsClass = randomPojo(AppsClassDO.class);
        appsClassMapper.insert(dbAppsClass);// @Sql: 先插入出一条存在的数据
        // 准备参数
        AppsClassSaveReqVO updateReqVO = randomPojo(AppsClassSaveReqVO.class, o -> {
            o.setId(dbAppsClass.getId()); // 设置更新的 ID
        });

        // 调用
        appsClassService.updateAppsClass(updateReqVO);
        // 校验是否更新正确
        AppsClassDO appsClass = appsClassMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, appsClass);
    }

    @Test
    public void testUpdateAppsClass_notExists() {
        // 准备参数
        AppsClassSaveReqVO updateReqVO = randomPojo(AppsClassSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> appsClassService.updateAppsClass(updateReqVO), APPS_CLASS_NOT_EXISTS);
    }

    @Test
    public void testDeleteAppsClass_success() {
        // mock 数据
        AppsClassDO dbAppsClass = randomPojo(AppsClassDO.class);
        appsClassMapper.insert(dbAppsClass);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Integer id = dbAppsClass.getId();

        // 调用
        appsClassService.deleteAppsClass(id);
       // 校验数据不存在了
       assertNull(appsClassMapper.selectById(id));
    }

    @Test
    public void testDeleteAppsClass_notExists() {
        // 准备参数
        Integer id = 1;//randomIntegerId();

        // 调用, 并断言异常
        assertServiceException(() -> appsClassService.deleteAppsClass(id), APPS_CLASS_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetAppsClassPage() {
       // mock 数据
       AppsClassDO dbAppsClass = randomPojo(AppsClassDO.class, o -> { // 等会查询到
           o.setClassName(null);
           o.setStatus(null);
           o.setCreateTime(null);
           o.setIndexNum(null);
       });
       appsClassMapper.insert(dbAppsClass);
       // 测试 className 不匹配
       appsClassMapper.insert(cloneIgnoreId(dbAppsClass, o -> o.setClassName(null)));
       // 测试 status 不匹配
       appsClassMapper.insert(cloneIgnoreId(dbAppsClass, o -> o.setStatus(null)));
       // 测试 createTime 不匹配
       appsClassMapper.insert(cloneIgnoreId(dbAppsClass, o -> o.setCreateTime(null)));
       // 测试 index 不匹配
       appsClassMapper.insert(cloneIgnoreId(dbAppsClass, o -> o.setIndexNum(null)));
       // 准备参数
       AppsClassPageReqVO reqVO = new AppsClassPageReqVO();
       reqVO.setClassName(null);
       reqVO.setStatus(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setIndexNum(null);

       // 调用
       PageResult<AppsClassDO> pageResult = appsClassService.getAppsClassPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbAppsClass, pageResult.getList().get(0));
    }

}