package cn.iocoder.yudao.module.infra.service.file;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.file.core.client.FileClient;
import cn.iocoder.yudao.framework.file.core.client.FileClientConfig;
import cn.iocoder.yudao.framework.file.core.client.FileClientFactory;
import cn.iocoder.yudao.framework.file.core.client.local.LocalFileClient;
import cn.iocoder.yudao.framework.file.core.client.local.LocalFileClientConfig;
import cn.iocoder.yudao.framework.file.core.enums.FileStorageEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.config.FileConfigCreateReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.config.FileConfigPageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.config.FileConfigUpdateReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileConfigDO;
import cn.iocoder.yudao.module.infra.dal.mysql.file.FileConfigMapper;
import cn.iocoder.yudao.module.infra.mq.producer.file.FileConfigProducer;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import javax.validation.Validator;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.FILE_CONFIG_DELETE_FAIL_MASTER;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.FILE_CONFIG_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
* {@link FileConfigServiceImpl} 的单元测试类
*
* @author 芋道源码
*/
@Import(FileConfigServiceImpl.class)
public class FileConfigServiceImplTest extends BaseDbUnitTest {

    @Resource
    private FileConfigServiceImpl fileConfigService;

    @Resource
    private FileConfigMapper fileConfigMapper;

    @MockBean
    private FileConfigProducer fileConfigProducer;
    @MockBean
    private Validator validator;
    @MockBean
    private FileClientFactory fileClientFactory;

    @Test
    public void testInitLocalCache() {
        // mock 数据
        FileConfigDO configDO1 = randomFileConfigDO().setId(1L).setMaster(true);
        fileConfigMapper.insert(configDO1);
        FileConfigDO configDO2 = randomFileConfigDO().setId(2L).setMaster(false);
        fileConfigMapper.insert(configDO2);
        // mock fileClientFactory 获得 master
        FileClient masterFileClient = mock(FileClient.class);
        when(fileClientFactory.getFileClient(eq(1L))).thenReturn(masterFileClient);

        // 调用
        fileConfigService.initLocalCache();
        // 断言 fileClientFactory 调用
        verify(fileClientFactory).createOrUpdateFileClient(eq(1L),
                eq(configDO1.getStorage()), eq(configDO1.getConfig()));
        verify(fileClientFactory).createOrUpdateFileClient(eq(2L),
                eq(configDO2.getStorage()), eq(configDO2.getConfig()));
        assertSame(masterFileClient, fileConfigService.getMasterFileClient());
    }

    @Test
    public void testCreateFileConfig_success() {
        // 准备参数
        Map<String, Object> config = MapUtil.<String, Object>builder().put("basePath", "/yunai")
                .put("domain", "https://www.iocoder.cn").build();
        FileConfigCreateReqVO reqVO = randomPojo(FileConfigCreateReqVO.class,
                o -> o.setStorage(FileStorageEnum.LOCAL.getStorage()).setConfig(config));

        // 调用
        Long fileConfigId = fileConfigService.createFileConfig(reqVO);
        // 断言
        assertNotNull(fileConfigId);
        // 校验记录的属性是否正确
        FileConfigDO fileConfig = fileConfigMapper.selectById(fileConfigId);
        assertPojoEquals(reqVO, fileConfig, "config");
        assertFalse(fileConfig.getMaster());
        assertEquals("/yunai", ((LocalFileClientConfig) fileConfig.getConfig()).getBasePath());
        assertEquals("https://www.iocoder.cn", ((LocalFileClientConfig) fileConfig.getConfig()).getDomain());
        // verify 调用
        verify(fileConfigProducer).sendFileConfigRefreshMessage();
    }

    @Test
    public void testUpdateFileConfig_success() {
        // mock 数据
        FileConfigDO dbFileConfig = randomPojo(FileConfigDO.class, o -> o.setStorage(FileStorageEnum.LOCAL.getStorage())
                .setConfig(new LocalFileClientConfig().setBasePath("/yunai").setDomain("https://www.iocoder.cn")));
        fileConfigMapper.insert(dbFileConfig);// @Sql: 先插入出一条存在的数据
        // 准备参数
        FileConfigUpdateReqVO reqVO = randomPojo(FileConfigUpdateReqVO.class, o -> {
            o.setId(dbFileConfig.getId()); // 设置更新的 ID
            Map<String, Object> config = MapUtil.<String, Object>builder().put("basePath", "/yunai2")
                    .put("domain", "https://doc.iocoder.cn").build();
            o.setConfig(config);
        });

        // 调用
        fileConfigService.updateFileConfig(reqVO);
        // 校验是否更新正确
        FileConfigDO fileConfig = fileConfigMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, fileConfig, "config");
        assertEquals("/yunai2", ((LocalFileClientConfig) fileConfig.getConfig()).getBasePath());
        assertEquals("https://doc.iocoder.cn", ((LocalFileClientConfig) fileConfig.getConfig()).getDomain());
        // verify 调用
        verify(fileConfigProducer).sendFileConfigRefreshMessage();
    }

    @Test
    public void testUpdateFileConfig_notExists() {
        // 准备参数
        FileConfigUpdateReqVO reqVO = randomPojo(FileConfigUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> fileConfigService.updateFileConfig(reqVO), FILE_CONFIG_NOT_EXISTS);
    }

    @Test
    public void testUpdateFileConfigMaster_success() {
        // mock 数据
        FileConfigDO dbFileConfig = randomFileConfigDO().setMaster(false);
        fileConfigMapper.insert(dbFileConfig);// @Sql: 先插入出一条存在的数据
        FileConfigDO masterFileConfig = randomFileConfigDO().setMaster(true);
        fileConfigMapper.insert(masterFileConfig);// @Sql: 先插入出一条存在的数据

        // 调用
        fileConfigService.updateFileConfigMaster(dbFileConfig.getId());
        // 断言数据
        assertTrue(fileConfigMapper.selectById(dbFileConfig.getId()).getMaster());
        assertFalse(fileConfigMapper.selectById(masterFileConfig.getId()).getMaster());
        // verify 调用
        verify(fileConfigProducer).sendFileConfigRefreshMessage();
    }

    @Test
    public void testUpdateFileConfigMaster_notExists() {
        // 调用, 并断言异常
        assertServiceException(() -> fileConfigService.updateFileConfigMaster(randomLongId()), FILE_CONFIG_NOT_EXISTS);
    }

    @Test
    public void testDeleteFileConfig_success() {
        // mock 数据
        FileConfigDO dbFileConfig = randomFileConfigDO().setMaster(false);
        fileConfigMapper.insert(dbFileConfig);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbFileConfig.getId();

        // 调用
        fileConfigService.deleteFileConfig(id);
       // 校验数据不存在了
       assertNull(fileConfigMapper.selectById(id));
        // verify 调用
        verify(fileConfigProducer).sendFileConfigRefreshMessage();
    }

    @Test
    public void testDeleteFileConfig_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> fileConfigService.deleteFileConfig(id), FILE_CONFIG_NOT_EXISTS);
    }

    @Test
    public void testDeleteFileConfig_master() {
        // mock 数据
        FileConfigDO dbFileConfig = randomFileConfigDO().setMaster(true);
        fileConfigMapper.insert(dbFileConfig);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbFileConfig.getId();

        // 调用, 并断言异常
        assertServiceException(() -> fileConfigService.deleteFileConfig(id), FILE_CONFIG_DELETE_FAIL_MASTER);
    }

    @Test
    public void testGetFileConfigPage() {
       // mock 数据
       FileConfigDO dbFileConfig = randomFileConfigDO().setName("芋道源码")
               .setStorage(FileStorageEnum.LOCAL.getStorage());
       dbFileConfig.setCreateTime(LocalDateTimeUtil.parse("2020-01-23", DatePattern.NORM_DATE_PATTERN));// 等会查询到
       fileConfigMapper.insert(dbFileConfig);
       // 测试 name 不匹配
       fileConfigMapper.insert(cloneIgnoreId(dbFileConfig, o -> o.setName("源码")));
       // 测试 storage 不匹配
       fileConfigMapper.insert(cloneIgnoreId(dbFileConfig, o -> o.setStorage(FileStorageEnum.DB.getStorage())));
       // 测试 createTime 不匹配
       fileConfigMapper.insert(cloneIgnoreId(dbFileConfig, o -> o.setCreateTime(LocalDateTimeUtil.parse("2020-11-23", DatePattern.NORM_DATE_PATTERN))));
       // 准备参数
       FileConfigPageReqVO reqVO = new FileConfigPageReqVO();
       reqVO.setName("芋道");
       reqVO.setStorage(FileStorageEnum.LOCAL.getStorage());
       reqVO.setCreateTime((new LocalDateTime[]{buildTime(2020, 1, 1),
               buildTime(2020, 1, 24)}));

       // 调用
       PageResult<FileConfigDO> pageResult = fileConfigService.getFileConfigPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbFileConfig, pageResult.getList().get(0));
    }

    @Test
    public void testFileConfig() throws Exception {
        // mock 数据
        FileConfigDO dbFileConfig = randomFileConfigDO().setMaster(false);
        fileConfigMapper.insert(dbFileConfig);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbFileConfig.getId();
        // mock 获得 Client
        FileClient fileClient = mock(FileClient.class);
        when(fileClientFactory.getFileClient(eq(id))).thenReturn(fileClient);
        when(fileClient.upload(any(), any(), any())).thenReturn("https://www.iocoder.cn");

        // 调用，并断言
        assertEquals("https://www.iocoder.cn", fileConfigService.testFileConfig(id));
    }

    @Test
    public void testGetFileConfig() {
        // mock 数据
        FileConfigDO dbFileConfig = randomFileConfigDO().setMaster(false);
        fileConfigMapper.insert(dbFileConfig);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbFileConfig.getId();

        // 调用，并断言
        assertPojoEquals(dbFileConfig, fileConfigService.getFileConfig(id));
    }

    @Test
    public void testGetFileClient() {
        // 准备参数
        Long id = randomLongId();
        // mock 获得 Client
        FileClient fileClient = new LocalFileClient(id, new LocalFileClientConfig());
        when(fileClientFactory.getFileClient(eq(id))).thenReturn(fileClient);

        // 调用，并断言
        assertSame(fileClient, fileConfigService.getFileClient(id));
    }

    private FileConfigDO randomFileConfigDO() {
        return randomPojo(FileConfigDO.class).setStorage(randomEle(FileStorageEnum.values()).getStorage())
                .setConfig(new EmptyFileClientConfig());
    }

    @Data
    public static class EmptyFileClientConfig implements FileClientConfig, Serializable {

    }

}
