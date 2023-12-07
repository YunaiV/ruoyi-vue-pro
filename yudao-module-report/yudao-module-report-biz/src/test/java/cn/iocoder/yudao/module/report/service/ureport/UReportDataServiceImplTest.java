package cn.iocoder.yudao.module.report.service.ureport;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.report.controller.admin.ureport.vo.UReportDataPageReqVO;
import cn.iocoder.yudao.module.report.controller.admin.ureport.vo.UReportDataSaveReqVO;
import cn.iocoder.yudao.module.report.dal.dataobject.ureport.UReportDataDO;
import cn.iocoder.yudao.module.report.dal.mysql.ureport.UReportDataMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.report.enums.ErrorCodeConstants.UREPORT_DATA_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

// TODO 芋艿：最后搞单测
/**
 * {@link UReportDataServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(UReportDataServiceImpl.class)
public class UReportDataServiceImplTest extends BaseDbUnitTest {

    @Resource
    private UReportDataServiceImpl uReportDataService;

    @Resource
    private UReportDataMapper uReportDataMapper;

    @Test
    public void testCreateUReportData_success() {
        // 准备参数
        UReportDataSaveReqVO createReqVO = randomPojo(UReportDataSaveReqVO.class).setId(null);

        // 调用
        Long uReportDataId = uReportDataService.createUReportData(createReqVO);
        // 断言
        assertNotNull(uReportDataId);
        // 校验记录的属性是否正确
        UReportDataDO uReportData = uReportDataMapper.selectById(uReportDataId);
        assertPojoEquals(createReqVO, uReportData, "id");
    }

    @Test
    public void testUpdateUReportData_success() {
        // mock 数据
        UReportDataDO dbUReportData = randomPojo(UReportDataDO.class);
        uReportDataMapper.insert(dbUReportData);// @Sql: 先插入出一条存在的数据
        // 准备参数
        UReportDataSaveReqVO updateReqVO = randomPojo(UReportDataSaveReqVO.class, o -> {
            o.setId(dbUReportData.getId()); // 设置更新的 ID
        });

        // 调用
        uReportDataService.updateUReportData(updateReqVO);
        // 校验是否更新正确
        UReportDataDO uReportData = uReportDataMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, uReportData);
    }

    @Test
    public void testUpdateUReportData_notExists() {
        // 准备参数
        UReportDataSaveReqVO updateReqVO = randomPojo(UReportDataSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> uReportDataService.updateUReportData(updateReqVO), UREPORT_DATA_NOT_EXISTS);
    }

    @Test
    public void testDeleteUReportData_success() {
        // mock 数据
        UReportDataDO dbUReportData = randomPojo(UReportDataDO.class);
        uReportDataMapper.insert(dbUReportData);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbUReportData.getId();

        // 调用
        uReportDataService.deleteUReportData(id);
       // 校验数据不存在了
       assertNull(uReportDataMapper.selectById(id));
    }

    @Test
    public void testDeleteUReportData_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> uReportDataService.deleteUReportData(id), UREPORT_DATA_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetUReportDataPage() {
       // mock 数据
       UReportDataDO dbUReportData = randomPojo(UReportDataDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setStatus(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       uReportDataMapper.insert(dbUReportData);
       // 测试 name 不匹配
       uReportDataMapper.insert(cloneIgnoreId(dbUReportData, o -> o.setName(null)));
       // 测试 status 不匹配
       uReportDataMapper.insert(cloneIgnoreId(dbUReportData, o -> o.setStatus(null)));
       // 测试 remark 不匹配
       uReportDataMapper.insert(cloneIgnoreId(dbUReportData, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       uReportDataMapper.insert(cloneIgnoreId(dbUReportData, o -> o.setCreateTime(null)));
       // 准备参数
       UReportDataPageReqVO reqVO = new UReportDataPageReqVO();
       reqVO.setName(null);
       reqVO.setStatus(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<UReportDataDO> pageResult = uReportDataService.getUReportDataPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbUReportData, pageResult.getList().get(0));
    }

}
