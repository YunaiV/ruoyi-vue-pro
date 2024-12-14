package cn.iocoder.yudao.module.iot.service.plugininfo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.iot.controller.admin.plugininfo.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.plugininfo.PluginInfoDO;
import cn.iocoder.yudao.module.iot.dal.mysql.plugininfo.PluginInfoMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.springframework.context.annotation.Import;

import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link PluginInfoServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(PluginInfoServiceImpl.class)
public class PluginInfoServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PluginInfoServiceImpl pluginInfoService;

    @Resource
    private PluginInfoMapper pluginInfoMapper;

    @Test
    public void testCreatePluginInfo_success() {
        // 准备参数
        PluginInfoSaveReqVO createReqVO = randomPojo(PluginInfoSaveReqVO.class).setId(null);

        // 调用
        Long pluginInfoId = pluginInfoService.createPluginInfo(createReqVO);
        // 断言
        assertNotNull(pluginInfoId);
        // 校验记录的属性是否正确
        PluginInfoDO pluginInfo = pluginInfoMapper.selectById(pluginInfoId);
        assertPojoEquals(createReqVO, pluginInfo, "id");
    }

    @Test
    public void testUpdatePluginInfo_success() {
        // mock 数据
        PluginInfoDO dbPluginInfo = randomPojo(PluginInfoDO.class);
        pluginInfoMapper.insert(dbPluginInfo);// @Sql: 先插入出一条存在的数据
        // 准备参数
        PluginInfoSaveReqVO updateReqVO = randomPojo(PluginInfoSaveReqVO.class, o -> {
            o.setId(dbPluginInfo.getId()); // 设置更新的 ID
        });

        // 调用
        pluginInfoService.updatePluginInfo(updateReqVO);
        // 校验是否更新正确
        PluginInfoDO pluginInfo = pluginInfoMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, pluginInfo);
    }

    @Test
    public void testUpdatePluginInfo_notExists() {
        // 准备参数
        PluginInfoSaveReqVO updateReqVO = randomPojo(PluginInfoSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> pluginInfoService.updatePluginInfo(updateReqVO), PLUGIN_INFO_NOT_EXISTS);
    }

    @Test
    public void testDeletePluginInfo_success() {
        // mock 数据
        PluginInfoDO dbPluginInfo = randomPojo(PluginInfoDO.class);
        pluginInfoMapper.insert(dbPluginInfo);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbPluginInfo.getId();

        // 调用
        pluginInfoService.deletePluginInfo(id);
       // 校验数据不存在了
       assertNull(pluginInfoMapper.selectById(id));
    }

    @Test
    public void testDeletePluginInfo_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> pluginInfoService.deletePluginInfo(id), PLUGIN_INFO_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetPluginInfoPage() {
       // mock 数据
       PluginInfoDO dbPluginInfo = randomPojo(PluginInfoDO.class, o -> { // 等会查询到
           o.setPluginId(null);
           o.setName(null);
           o.setDescription(null);
           o.setDeployType(null);
           o.setFile(null);
           o.setVersion(null);
           o.setType(null);
           o.setProtocol(null);
           o.setStatus(null);
           o.setConfigSchema(null);
           o.setConfig(null);
           o.setScript(null);
           o.setCreateTime(null);
       });
       pluginInfoMapper.insert(dbPluginInfo);
       // 测试 pluginId 不匹配
       pluginInfoMapper.insert(cloneIgnoreId(dbPluginInfo, o -> o.setPluginId(null)));
       // 测试 name 不匹配
       pluginInfoMapper.insert(cloneIgnoreId(dbPluginInfo, o -> o.setName(null)));
       // 测试 description 不匹配
       pluginInfoMapper.insert(cloneIgnoreId(dbPluginInfo, o -> o.setDescription(null)));
       // 测试 deployType 不匹配
       pluginInfoMapper.insert(cloneIgnoreId(dbPluginInfo, o -> o.setDeployType(null)));
       // 测试 file 不匹配
       pluginInfoMapper.insert(cloneIgnoreId(dbPluginInfo, o -> o.setFile(null)));
       // 测试 version 不匹配
       pluginInfoMapper.insert(cloneIgnoreId(dbPluginInfo, o -> o.setVersion(null)));
       // 测试 type 不匹配
       pluginInfoMapper.insert(cloneIgnoreId(dbPluginInfo, o -> o.setType(null)));
       // 测试 protocol 不匹配
       pluginInfoMapper.insert(cloneIgnoreId(dbPluginInfo, o -> o.setProtocol(null)));
       // 测试 state 不匹配
       pluginInfoMapper.insert(cloneIgnoreId(dbPluginInfo, o -> o.setStatus(null)));
       // 测试 configSchema 不匹配
       pluginInfoMapper.insert(cloneIgnoreId(dbPluginInfo, o -> o.setConfigSchema(null)));
       // 测试 config 不匹配
       pluginInfoMapper.insert(cloneIgnoreId(dbPluginInfo, o -> o.setConfig(null)));
       // 测试 script 不匹配
       pluginInfoMapper.insert(cloneIgnoreId(dbPluginInfo, o -> o.setScript(null)));
       // 测试 createTime 不匹配
       pluginInfoMapper.insert(cloneIgnoreId(dbPluginInfo, o -> o.setCreateTime(null)));
       // 准备参数
       PluginInfoPageReqVO reqVO = new PluginInfoPageReqVO();
       reqVO.setPluginId(null);
       reqVO.setName(null);
       reqVO.setDescription(null);
       reqVO.setDeployType(null);
       reqVO.setFile(null);
       reqVO.setVersion(null);
       reqVO.setType(null);
       reqVO.setProtocol(null);
       reqVO.setStatus(null);
       reqVO.setConfigSchema(null);
       reqVO.setConfig(null);
       reqVO.setScript(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<PluginInfoDO> pageResult = pluginInfoService.getPluginInfoPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbPluginInfo, pageResult.getList().get(0));
    }

}