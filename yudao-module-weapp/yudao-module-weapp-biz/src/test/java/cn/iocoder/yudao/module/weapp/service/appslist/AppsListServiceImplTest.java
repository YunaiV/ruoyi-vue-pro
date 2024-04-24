//package cn.iocoder.yudao.module.weapp.service.appslist;
//
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.mock.mockito.MockBean;
//
//import javax.annotation.Resource;
//
//import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
//
//import cn.iocoder.yudao.module.weapp.controller.admin.appslist.vo.*;
//import cn.iocoder.yudao.module.weapp.dal.dataobject.appslist.AppsListDO;
//import cn.iocoder.yudao.module.weapp.dal.mysql.appslist.AppsListMapper;
//import cn.iocoder.yudao.framework.common.pojo.PageResult;
//
//import javax.annotation.Resource;
//import org.springframework.context.annotation.Import;
//import java.util.*;
//import java.time.LocalDateTime;
//
//import static cn.hutool.core.util.RandomUtil.*;
//import static cn.iocoder.yudao.module.weapp.enums.ErrorCodeConstants.*;
//import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
//import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
//import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
//import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
//import static cn.iocoder.yudao.framework.common.util.date.DateUtils.*;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
///**
// * {@link AppsListServiceImpl} 的单元测试类
// *
// * @author 芋道源码
// */
//@Import(AppsListServiceImpl.class)
//public class AppsListServiceImplTest extends BaseDbUnitTest {
//
//    @Resource
//    private AppsListServiceImpl appsListService;
//
//    @Resource
//    private AppsListMapper appsListMapper;
//
//    @Test
//    public void testCreateAppsList_success() {
//        // 准备参数
//        AppsListSaveReqVO createReqVO = randomPojo(AppsListSaveReqVO.class).setId(null);
//
//        // 调用
//        Integer appsListId = appsListService.createAppsList(createReqVO);
//        // 断言
//        assertNotNull(appsListId);
//        // 校验记录的属性是否正确
//        AppsListDO appsList = appsListMapper.selectById(appsListId);
//        assertPojoEquals(createReqVO, appsList, "id");
//    }
//
//    @Test
//    public void testUpdateAppsList_success() {
//        // mock 数据
//        AppsListDO dbAppsList = randomPojo(AppsListDO.class);
//        appsListMapper.insert(dbAppsList);// @Sql: 先插入出一条存在的数据
//        // 准备参数
//        AppsListSaveReqVO updateReqVO = randomPojo(AppsListSaveReqVO.class, o -> {
//            o.setId(dbAppsList.getId()); // 设置更新的 ID
//        });
//
//        // 调用
//        appsListService.updateAppsList(updateReqVO);
//        // 校验是否更新正确
//        AppsListDO appsList = appsListMapper.selectById(updateReqVO.getId()); // 获取最新的
//        assertPojoEquals(updateReqVO, appsList);
//    }
//
//    @Test
//    public void testUpdateAppsList_notExists() {
//        // 准备参数
//        AppsListSaveReqVO updateReqVO = randomPojo(AppsListSaveReqVO.class);
//
//        // 调用, 并断言异常
//        assertServiceException(() -> appsListService.updateAppsList(updateReqVO), APPS_LIST_NOT_EXISTS);
//    }
//
//    @Test
//    public void testDeleteAppsList_success() {
//        // mock 数据
//        AppsListDO dbAppsList = randomPojo(AppsListDO.class);
//        appsListMapper.insert(dbAppsList);// @Sql: 先插入出一条存在的数据
//        // 准备参数
//        Integer id = dbAppsList.getId();
//
//        // 调用
//        appsListService.deleteAppsList(id);
//       // 校验数据不存在了
//       assertNull(appsListMapper.selectById(id));
//    }
//
//    @Test
//    public void testDeleteAppsList_notExists() {
//        // 准备参数
//        Integer id = randomIntegerId();
//
//        // 调用, 并断言异常
//        assertServiceException(() -> appsListService.deleteAppsList(id), APPS_LIST_NOT_EXISTS);
//    }
//
//    @Test
//    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
//    public void testGetAppsListPage() {
//       // mock 数据
//       AppsListDO dbAppsList = randomPojo(AppsListDO.class, o -> { // 等会查询到
//           o.setWeappName(null);
//           o.setWeappOpenid(null);
//           o.setClassId(null);
//           o.setDescription(null);
//           o.setLogoImg(null);
//           o.setStatus(null);
//           o.setUpdatedBy(null);
//           o.setUpdatedTime(null);
//       });
//       appsListMapper.insert(dbAppsList);
//       // 测试 weappName 不匹配
//       appsListMapper.insert(cloneIgnoreId(dbAppsList, o -> o.setWeappName(null)));
//       // 测试 weappOpenid 不匹配
//       appsListMapper.insert(cloneIgnoreId(dbAppsList, o -> o.setWeappOpenid(null)));
//       // 测试 classId 不匹配
//       appsListMapper.insert(cloneIgnoreId(dbAppsList, o -> o.setClassId(null)));
//       // 测试 description 不匹配
//       appsListMapper.insert(cloneIgnoreId(dbAppsList, o -> o.setDescription(null)));
//       // 测试 logoImg 不匹配
//       appsListMapper.insert(cloneIgnoreId(dbAppsList, o -> o.setLogoImg(null)));
//       // 测试 status 不匹配
//       appsListMapper.insert(cloneIgnoreId(dbAppsList, o -> o.setStatus(null)));
//       // 测试 updatedBy 不匹配
//       appsListMapper.insert(cloneIgnoreId(dbAppsList, o -> o.setUpdatedBy(null)));
//       // 测试 updatedTime 不匹配
//       appsListMapper.insert(cloneIgnoreId(dbAppsList, o -> o.setUpdatedTime(null)));
//       // 准备参数
//       AppsListPageReqVO reqVO = new AppsListPageReqVO();
//       reqVO.setWeappName(null);
//       reqVO.setWeappOpenid(null);
//       reqVO.setClassId(null);
//       reqVO.setDescription(null);
//       reqVO.setLogoImg(null);
//       reqVO.setStatus(null);
//       reqVO.setUpdatedBy(null);
//       reqVO.setUpdatedTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
//
//       // 调用
//       PageResult<AppsListDO> pageResult = appsListService.getAppsListPage(reqVO);
//       // 断言
//       assertEquals(1, pageResult.getTotal());
//       assertEquals(1, pageResult.getList().size());
//       assertPojoEquals(dbAppsList, pageResult.getList().get(0));
//    }
//
//}