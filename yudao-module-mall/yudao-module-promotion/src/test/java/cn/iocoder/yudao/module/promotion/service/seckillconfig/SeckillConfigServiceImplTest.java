package cn.iocoder.yudao.module.promotion.service.seckillconfig;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.config.SeckillConfigCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.config.SeckillConfigUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.SeckillConfigDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.seckill.seckillconfig.SeckillConfigMapper;
import cn.iocoder.yudao.module.promotion.service.seckill.SeckillConfigServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import jakarta.annotation.Resource;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.SECKILL_CONFIG_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link SeckillConfigServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(SeckillConfigServiceImpl.class)
@Disabled // TODO 芋艿：未来开启；后续要 review 下
public class SeckillConfigServiceImplTest extends BaseDbUnitTest {

    @Resource
    private SeckillConfigServiceImpl SeckillConfigService;

    @Resource
    private SeckillConfigMapper seckillConfigMapper;

    @Resource
    private ObjectMapper objectMapper;

    @Test
    public void testJacksonSerializ() {

        // 准备参数
        SeckillConfigCreateReqVO reqVO = randomPojo(SeckillConfigCreateReqVO.class);
//        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String string = objectMapper.writeValueAsString(reqVO);
            System.out.println(string);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }


    }

    @Test
    public void testCreateSeckillConfig_success() {
        // 准备参数
        SeckillConfigCreateReqVO reqVO = randomPojo(SeckillConfigCreateReqVO.class);

        // 调用
        Long SeckillConfigId = SeckillConfigService.createSeckillConfig(reqVO);
        // 断言
        assertNotNull(SeckillConfigId);
        // 校验记录的属性是否正确
        SeckillConfigDO SeckillConfig = seckillConfigMapper.selectById(SeckillConfigId);
        assertPojoEquals(reqVO, SeckillConfig);
    }

    @Test
    public void testUpdateSeckillConfig_success() {
        // mock 数据
        SeckillConfigDO dbSeckillConfig = randomPojo(SeckillConfigDO.class);
        seckillConfigMapper.insert(dbSeckillConfig);// @Sql: 先插入出一条存在的数据
        // 准备参数
        SeckillConfigUpdateReqVO reqVO = randomPojo(SeckillConfigUpdateReqVO.class, o -> {
            o.setId(dbSeckillConfig.getId()); // 设置更新的 ID
        });

        // 调用
        SeckillConfigService.updateSeckillConfig(reqVO);
        // 校验是否更新正确
        SeckillConfigDO SeckillConfig = seckillConfigMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, SeckillConfig);
    }

    @Test
    public void testUpdateSeckillConfig_notExists() {
        // 准备参数
        SeckillConfigUpdateReqVO reqVO = randomPojo(SeckillConfigUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> SeckillConfigService.updateSeckillConfig(reqVO), SECKILL_CONFIG_NOT_EXISTS);
    }

    @Test
    public void testDeleteSeckillConfig_success() {
        // mock 数据
        SeckillConfigDO dbSeckillConfig = randomPojo(SeckillConfigDO.class);
        seckillConfigMapper.insert(dbSeckillConfig);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbSeckillConfig.getId();

        // 调用
        SeckillConfigService.deleteSeckillConfig(id);
        // 校验数据不存在了
        assertNull(seckillConfigMapper.selectById(id));
    }

    @Test
    public void testDeleteSeckillConfig_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> SeckillConfigService.deleteSeckillConfig(id), SECKILL_CONFIG_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetSeckillConfigPage() {
        // mock 数据
//       SeckillConfigDO dbSeckillConfig = randomPojo(SeckillConfigDO.class, o -> { // 等会查询到
//           o.setName(null);
//           o.setStartTime(null);
//           o.setEndTime(null);
//           o.setCreateTime(null);
//       });
//       seckillConfigMapper.insert(dbSeckillConfig);
//       // 测试 name 不匹配
//       seckillConfigMapper.insert(cloneIgnoreId(dbSeckillConfig, o -> o.setName(null)));
//       // 测试 startTime 不匹配
//       seckillConfigMapper.insert(cloneIgnoreId(dbSeckillConfig, o -> o.setStartTime(null)));
//       // 测试 endTime 不匹配
//       seckillConfigMapper.insert(cloneIgnoreId(dbSeckillConfig, o -> o.setEndTime(null)));
//       // 测试 createTime 不匹配
//       seckillConfigMapper.insert(cloneIgnoreId(dbSeckillConfig, o -> o.setCreateTime(null)));
//       // 准备参数
//       SeckillConfigPageReqVO reqVO = new SeckillConfigPageReqVO();
//       reqVO.setName(null);
////       reqVO.setStartTime((new LocalTime()));
////       reqVO.setEndTime((new LocalTime[]{}));
////       reqVO.setCreateTime((new Date[]{}));
//
//       // 调用
//       PageResult<SeckillConfigDO> pageResult = SeckillConfigService.getSeckillConfigPage(reqVO);
//       // 断言
//       assertEquals(1, pageResult.getTotal());
//       assertEquals(1, pageResult.getList().size());
//       assertPojoEquals(dbSeckillConfig, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetSeckillConfigList() {
        // mock 数据
        SeckillConfigDO dbSeckillConfig = randomPojo(SeckillConfigDO.class, o -> { // 等会查询到
            o.setName(null);
            o.setStartTime(null);
            o.setEndTime(null);
            o.setCreateTime(null);
        });
        seckillConfigMapper.insert(dbSeckillConfig);
        // 测试 name 不匹配
        seckillConfigMapper.insert(cloneIgnoreId(dbSeckillConfig, o -> o.setName(null)));
        // 测试 startTime 不匹配
        seckillConfigMapper.insert(cloneIgnoreId(dbSeckillConfig, o -> o.setStartTime(null)));
        // 测试 endTime 不匹配
        seckillConfigMapper.insert(cloneIgnoreId(dbSeckillConfig, o -> o.setEndTime(null)));
        // 测试 createTime 不匹配
        seckillConfigMapper.insert(cloneIgnoreId(dbSeckillConfig, o -> o.setCreateTime(null)));
        // 准备参数
//       SeckillConfigExportReqVO reqVO = new SeckillConfigExportReqVO();
//       reqVO.setName(null);
//       reqVO.setStartTime((new LocalTime[]{}));
//       reqVO.setEndTime((new LocalTime[]{}));
//       reqVO.setCreateTime((new Date[]{}));
//
//       // 调用
//       List<SeckillConfigDO> list = SeckillConfigService.getSeckillConfigList(reqVO);
//       // 断言
//       assertEquals(1, list.size());
//       assertPojoEquals(dbSeckillConfig, list.get(0));
    }

}
