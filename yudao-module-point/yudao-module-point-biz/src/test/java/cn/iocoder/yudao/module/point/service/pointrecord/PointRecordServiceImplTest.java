package cn.iocoder.yudao.module.point.service.pointrecord;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.point.controller.admin.pointrecord.vo.*;
import cn.iocoder.yudao.module.point.dal.dataobject.pointrecord.PointRecordDO;
import cn.iocoder.yudao.module.point.dal.mysql.pointrecord.PointRecordMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import javax.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static cn.iocoder.yudao.module.point.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link PointRecordServiceImpl} 的单元测试类
 *
 * @author QingX
 */
@Import(PointRecordServiceImpl.class)
public class PointRecordServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PointRecordServiceImpl recordService;

    @Resource
    private PointRecordMapper recordMapper;

    @Test
    public void testCreateRecord_success() {
        // 准备参数
        PointRecordCreateReqVO reqVO = randomPojo(PointRecordCreateReqVO.class);

        // 调用
        Long recordId = recordService.createRecord(reqVO);
        // 断言
        assertNotNull(recordId);
        // 校验记录的属性是否正确
        PointRecordDO record = recordMapper.selectById(recordId);
        assertPojoEquals(reqVO, record);
    }

    @Test
    public void testUpdateRecord_success() {
        // mock 数据
        PointRecordDO dbRecord = randomPojo(PointRecordDO.class);
        recordMapper.insert(dbRecord);// @Sql: 先插入出一条存在的数据
        // 准备参数
        PointRecordUpdateReqVO reqVO = randomPojo(PointRecordUpdateReqVO.class, o -> {
            o.setId(dbRecord.getId()); // 设置更新的 ID
        });

        // 调用
        recordService.updateRecord(reqVO);
        // 校验是否更新正确
        PointRecordDO record = recordMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, record);
    }

    @Test
    public void testUpdateRecord_notExists() {
        // 准备参数
        PointRecordUpdateReqVO reqVO = randomPojo(PointRecordUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> recordService.updateRecord(reqVO), RECORD_NOT_EXISTS);
    }

    @Test
    public void testDeleteRecord_success() {
        // mock 数据
        PointRecordDO dbRecord = randomPojo(PointRecordDO.class);
        recordMapper.insert(dbRecord);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbRecord.getId();

        // 调用
        recordService.deleteRecord(id);
       // 校验数据不存在了
       assertNull(recordMapper.selectById(id));
    }

    @Test
    public void testDeleteRecord_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> recordService.deleteRecord(id), RECORD_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetRecordPage() {
       // mock 数据
       PointRecordDO dbRecord = randomPojo(PointRecordDO.class, o -> { // 等会查询到
           o.setBizId(null);
           o.setBizType(null);
           o.setType(null);
           o.setTitle(null);
           o.setStatus(null);
       });
       recordMapper.insert(dbRecord);
       // 测试 bizId 不匹配
       recordMapper.insert(cloneIgnoreId(dbRecord, o -> o.setBizId(null)));
       // 测试 bizType 不匹配
       recordMapper.insert(cloneIgnoreId(dbRecord, o -> o.setBizType(null)));
       // 测试 type 不匹配
       recordMapper.insert(cloneIgnoreId(dbRecord, o -> o.setType(null)));
       // 测试 title 不匹配
       recordMapper.insert(cloneIgnoreId(dbRecord, o -> o.setTitle(null)));
       // 测试 status 不匹配
       recordMapper.insert(cloneIgnoreId(dbRecord, o -> o.setStatus(null)));
       // 准备参数
       PointRecordPageReqVO reqVO = new PointRecordPageReqVO();
       reqVO.setBizId(null);
       reqVO.setBizType(null);
       reqVO.setType(null);
       reqVO.setTitle(null);
       reqVO.setStatus(null);

       // 调用
       PageResult<PointRecordDO> pageResult = recordService.getRecordPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbRecord, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetRecordList() {
       // mock 数据
       PointRecordDO dbRecord = randomPojo(PointRecordDO.class, o -> { // 等会查询到
           o.setBizId(null);
           o.setBizType(null);
           o.setType(null);
           o.setTitle(null);
           o.setStatus(null);
       });
       recordMapper.insert(dbRecord);
       // 测试 bizId 不匹配
       recordMapper.insert(cloneIgnoreId(dbRecord, o -> o.setBizId(null)));
       // 测试 bizType 不匹配
       recordMapper.insert(cloneIgnoreId(dbRecord, o -> o.setBizType(null)));
       // 测试 type 不匹配
       recordMapper.insert(cloneIgnoreId(dbRecord, o -> o.setType(null)));
       // 测试 title 不匹配
       recordMapper.insert(cloneIgnoreId(dbRecord, o -> o.setTitle(null)));
       // 测试 status 不匹配
       recordMapper.insert(cloneIgnoreId(dbRecord, o -> o.setStatus(null)));
       // 准备参数
       PointRecordExportReqVO reqVO = new PointRecordExportReqVO();
       reqVO.setBizId(null);
       reqVO.setBizType(null);
       reqVO.setType(null);
       reqVO.setTitle(null);
       reqVO.setStatus(null);

       // 调用
       List<PointRecordDO> list = recordService.getRecordList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbRecord, list.get(0));
    }

}
