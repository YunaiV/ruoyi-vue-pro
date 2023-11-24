package cn.iocoder.yudao.module.report.service.ureport;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.report.controller.admin.ureport.vo.*;
import cn.iocoder.yudao.module.report.dal.dataobject.ureport.UreportFileDO;
import cn.iocoder.yudao.module.report.dal.mysql.ureport.UreportFileMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import javax.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static cn.iocoder.yudao.module.report.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link UreportFileServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(UreportFileServiceImpl.class)
public class UreportFileServiceImplTest extends BaseDbUnitTest {

    @Resource
    private UreportFileServiceImpl ureportFileService;

    @Resource
    private UreportFileMapper ureportFileMapper;

    @Test
    public void testCreateUreportFile_success() {
        // 准备参数
        UreportFileSaveReqVO createReqVO = randomPojo(UreportFileSaveReqVO.class).setId(null);

        // 调用
        Long ureportFileId = ureportFileService.createUreportFile(createReqVO);
        // 断言
        assertNotNull(ureportFileId);
        // 校验记录的属性是否正确
        UreportFileDO ureportFile = ureportFileMapper.selectById(ureportFileId);
        assertPojoEquals(createReqVO, ureportFile, "id");
    }

    @Test
    public void testUpdateUreportFile_success() {
        // mock 数据
        UreportFileDO dbUreportFile = randomPojo(UreportFileDO.class);
        ureportFileMapper.insert(dbUreportFile);// @Sql: 先插入出一条存在的数据
        // 准备参数
        UreportFileSaveReqVO updateReqVO = randomPojo(UreportFileSaveReqVO.class, o -> {
            o.setId(dbUreportFile.getId()); // 设置更新的 ID
        });

        // 调用
        ureportFileService.updateUreportFile(updateReqVO);
        // 校验是否更新正确
        UreportFileDO ureportFile = ureportFileMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, ureportFile);
    }

    @Test
    public void testUpdateUreportFile_notExists() {
        // 准备参数
        UreportFileSaveReqVO updateReqVO = randomPojo(UreportFileSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> ureportFileService.updateUreportFile(updateReqVO), UREPORT_FILE_NOT_EXISTS);
    }

    @Test
    public void testDeleteUreportFile_success() {
        // mock 数据
        UreportFileDO dbUreportFile = randomPojo(UreportFileDO.class);
        ureportFileMapper.insert(dbUreportFile);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbUreportFile.getId();

        // 调用
        ureportFileService.deleteUreportFile(id);
       // 校验数据不存在了
       assertNull(ureportFileMapper.selectById(id));
    }

    @Test
    public void testDeleteUreportFile_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> ureportFileService.deleteUreportFile(id), UREPORT_FILE_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetUreportFilePage() {
       // mock 数据
       UreportFileDO dbUreportFile = randomPojo(UreportFileDO.class, o -> { // 等会查询到
           o.setFileName(null);
           o.setStatus(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       ureportFileMapper.insert(dbUreportFile);
       // 测试 fileName 不匹配
       ureportFileMapper.insert(cloneIgnoreId(dbUreportFile, o -> o.setFileName(null)));
       // 测试 status 不匹配
       ureportFileMapper.insert(cloneIgnoreId(dbUreportFile, o -> o.setStatus(null)));
       // 测试 remark 不匹配
       ureportFileMapper.insert(cloneIgnoreId(dbUreportFile, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       ureportFileMapper.insert(cloneIgnoreId(dbUreportFile, o -> o.setCreateTime(null)));
       // 准备参数
       UreportFilePageReqVO reqVO = new UreportFilePageReqVO();
       reqVO.setFileName(null);
       reqVO.setStatus(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<UreportFileDO> pageResult = ureportFileService.getUreportFilePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbUreportFile, pageResult.getList().get(0));
    }

}