package cn.iocoder.yudao.module.promotion.service.seckilltime;

import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.time.SeckillTimeCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.time.SeckillTimeUpdateReqVO;
import cn.iocoder.yudao.module.promotion.service.seckill.seckilltime.SeckillTimeServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckilltime.SeckillTimeDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.seckill.seckilltime.SeckillTimeMapper;

import org.springframework.context.annotation.Import;

import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
* {@link SeckillTimeServiceImpl} 的单元测试类
*
* @author 芋道源码
*/
@Import(SeckillTimeServiceImpl.class)
public class SeckillTimeServiceImplTest extends BaseDbUnitTest {

    @Resource
    private SeckillTimeServiceImpl seckillTimeService;

    @Resource
    private SeckillTimeMapper seckillTimeMapper;

    @Resource
    private ObjectMapper objectMapper;

    @Test
    public void testJacksonSerializ(){

        // 准备参数
        SeckillTimeCreateReqVO reqVO = randomPojo(SeckillTimeCreateReqVO.class);
//        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String string = objectMapper.writeValueAsString(reqVO);
            System.out.println(string);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }


    }

    @Test
    public void testCreateSeckillTime_success() {
        // 准备参数
        SeckillTimeCreateReqVO reqVO = randomPojo(SeckillTimeCreateReqVO.class);

        // 调用
        Long seckillTimeId = seckillTimeService.createSeckillTime(reqVO);
        // 断言
        assertNotNull(seckillTimeId);
        // 校验记录的属性是否正确
        SeckillTimeDO seckillTime = seckillTimeMapper.selectById(seckillTimeId);
        assertPojoEquals(reqVO, seckillTime);
    }

    @Test
    public void testUpdateSeckillTime_success() {
        // mock 数据
        SeckillTimeDO dbSeckillTime = randomPojo(SeckillTimeDO.class);
        seckillTimeMapper.insert(dbSeckillTime);// @Sql: 先插入出一条存在的数据
        // 准备参数
        SeckillTimeUpdateReqVO reqVO = randomPojo(SeckillTimeUpdateReqVO.class, o -> {
            o.setId(dbSeckillTime.getId()); // 设置更新的 ID
        });

        // 调用
        seckillTimeService.updateSeckillTime(reqVO);
        // 校验是否更新正确
        SeckillTimeDO seckillTime = seckillTimeMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, seckillTime);
    }

    @Test
    public void testUpdateSeckillTime_notExists() {
        // 准备参数
        SeckillTimeUpdateReqVO reqVO = randomPojo(SeckillTimeUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> seckillTimeService.updateSeckillTime(reqVO), SECKILL_TIME_NOT_EXISTS);
    }

    @Test
    public void testDeleteSeckillTime_success() {
        // mock 数据
        SeckillTimeDO dbSeckillTime = randomPojo(SeckillTimeDO.class);
        seckillTimeMapper.insert(dbSeckillTime);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbSeckillTime.getId();

        // 调用
        seckillTimeService.deleteSeckillTime(id);
       // 校验数据不存在了
       assertNull(seckillTimeMapper.selectById(id));
    }

    @Test
    public void testDeleteSeckillTime_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> seckillTimeService.deleteSeckillTime(id), SECKILL_TIME_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetSeckillTimePage() {
       // mock 数据
//       SeckillTimeDO dbSeckillTime = randomPojo(SeckillTimeDO.class, o -> { // 等会查询到
//           o.setName(null);
//           o.setStartTime(null);
//           o.setEndTime(null);
//           o.setCreateTime(null);
//       });
//       seckillTimeMapper.insert(dbSeckillTime);
//       // 测试 name 不匹配
//       seckillTimeMapper.insert(cloneIgnoreId(dbSeckillTime, o -> o.setName(null)));
//       // 测试 startTime 不匹配
//       seckillTimeMapper.insert(cloneIgnoreId(dbSeckillTime, o -> o.setStartTime(null)));
//       // 测试 endTime 不匹配
//       seckillTimeMapper.insert(cloneIgnoreId(dbSeckillTime, o -> o.setEndTime(null)));
//       // 测试 createTime 不匹配
//       seckillTimeMapper.insert(cloneIgnoreId(dbSeckillTime, o -> o.setCreateTime(null)));
//       // 准备参数
//       SeckillTimePageReqVO reqVO = new SeckillTimePageReqVO();
//       reqVO.setName(null);
////       reqVO.setStartTime((new LocalTime()));
////       reqVO.setEndTime((new LocalTime[]{}));
////       reqVO.setCreateTime((new Date[]{}));
//
//       // 调用
//       PageResult<SeckillTimeDO> pageResult = seckillTimeService.getSeckillTimePage(reqVO);
//       // 断言
//       assertEquals(1, pageResult.getTotal());
//       assertEquals(1, pageResult.getList().size());
//       assertPojoEquals(dbSeckillTime, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetSeckillTimeList() {
       // mock 数据
       SeckillTimeDO dbSeckillTime = randomPojo(SeckillTimeDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setStartTime(null);
           o.setEndTime(null);
           o.setCreateTime(null);
       });
       seckillTimeMapper.insert(dbSeckillTime);
       // 测试 name 不匹配
       seckillTimeMapper.insert(cloneIgnoreId(dbSeckillTime, o -> o.setName(null)));
       // 测试 startTime 不匹配
       seckillTimeMapper.insert(cloneIgnoreId(dbSeckillTime, o -> o.setStartTime(null)));
       // 测试 endTime 不匹配
       seckillTimeMapper.insert(cloneIgnoreId(dbSeckillTime, o -> o.setEndTime(null)));
       // 测试 createTime 不匹配
       seckillTimeMapper.insert(cloneIgnoreId(dbSeckillTime, o -> o.setCreateTime(null)));
       // 准备参数
//       SeckillTimeExportReqVO reqVO = new SeckillTimeExportReqVO();
//       reqVO.setName(null);
//       reqVO.setStartTime((new LocalTime[]{}));
//       reqVO.setEndTime((new LocalTime[]{}));
//       reqVO.setCreateTime((new Date[]{}));
//
//       // 调用
//       List<SeckillTimeDO> list = seckillTimeService.getSeckillTimeList(reqVO);
//       // 断言
//       assertEquals(1, list.size());
//       assertPojoEquals(dbSeckillTime, list.get(0));
    }

}
