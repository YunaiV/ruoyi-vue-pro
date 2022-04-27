package cn.iocoder.yudao.module.infra.service.db;

import cn.iocoder.yudao.framework.mybatis.core.util.DatabaseUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.infra.controller.admin.db.vo.DataSourceConfigCreateReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.db.vo.DataSourceConfigUpdateReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.db.DataSourceConfigDO;
import cn.iocoder.yudao.module.infra.dal.mysql.db.DataSourceConfigMapper;
import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.DATA_SOURCE_CONFIG_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link DataSourceConfigServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(DataSourceConfigServiceImpl.class)
public class DataSourceConfigServiceImplTest extends BaseDbUnitTest {

    @Resource
    private DataSourceConfigServiceImpl dataSourceConfigService;

    @Resource
    private DataSourceConfigMapper dataSourceConfigMapper;

    @MockBean
    private StringEncryptor stringEncryptor;

    @Test
    public void testCreateDataSourceConfig_success() {
        try (MockedStatic<DatabaseUtils> databaseUtilsMock = mockStatic(DatabaseUtils.class)) {
            // 准备参数
            DataSourceConfigCreateReqVO reqVO = randomPojo(DataSourceConfigCreateReqVO.class);
            // mock 方法
            when(stringEncryptor.encrypt(eq(reqVO.getPassword()))).thenReturn("123456");
            databaseUtilsMock.when(() -> DatabaseUtils.isConnectionOK(eq(reqVO.getUrl()),
                    eq(reqVO.getUsername()), eq(reqVO.getPassword()))).thenReturn(true);

            // 调用
            Long dataSourceConfigId = dataSourceConfigService.createDataSourceConfig(reqVO);
            // 断言
            assertNotNull(dataSourceConfigId);
            // 校验记录的属性是否正确
            DataSourceConfigDO dataSourceConfig = dataSourceConfigMapper.selectById(dataSourceConfigId);
            assertPojoEquals(reqVO, dataSourceConfig, "password");
            assertEquals("123456", dataSourceConfig.getPassword());
        }
    }

    @Test
    public void testUpdateDataSourceConfig_success() {
        try (MockedStatic<DatabaseUtils> databaseUtilsMock = mockStatic(DatabaseUtils.class)) {
            // mock 数据
            DataSourceConfigDO dbDataSourceConfig = randomPojo(DataSourceConfigDO.class);
            dataSourceConfigMapper.insert(dbDataSourceConfig);// @Sql: 先插入出一条存在的数据
            // 准备参数
            DataSourceConfigUpdateReqVO reqVO = randomPojo(DataSourceConfigUpdateReqVO.class, o -> {
                o.setId(dbDataSourceConfig.getId()); // 设置更新的 ID
            });
            // mock 方法
            when(stringEncryptor.encrypt(eq(reqVO.getPassword()))).thenReturn("123456");
            databaseUtilsMock.when(() -> DatabaseUtils.isConnectionOK(eq(reqVO.getUrl()),
                    eq(reqVO.getUsername()), eq(reqVO.getPassword()))).thenReturn(true);

            // 调用
            dataSourceConfigService.updateDataSourceConfig(reqVO);
            // 校验是否更新正确
            DataSourceConfigDO dataSourceConfig = dataSourceConfigMapper.selectById(reqVO.getId()); // 获取最新的
            assertPojoEquals(reqVO, dataSourceConfig, "password");
            assertEquals("123456", dataSourceConfig.getPassword());
        }
    }

    @Test
    public void testUpdateDataSourceConfig_notExists() {
        // 准备参数
        DataSourceConfigUpdateReqVO reqVO = randomPojo(DataSourceConfigUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> dataSourceConfigService.updateDataSourceConfig(reqVO), DATA_SOURCE_CONFIG_NOT_EXISTS);
    }

    @Test
    public void testDeleteDataSourceConfig_success() {
        // mock 数据
        DataSourceConfigDO dbDataSourceConfig = randomPojo(DataSourceConfigDO.class);
        dataSourceConfigMapper.insert(dbDataSourceConfig);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbDataSourceConfig.getId();

        // 调用
        dataSourceConfigService.deleteDataSourceConfig(id);
        // 校验数据不存在了
        assertNull(dataSourceConfigMapper.selectById(id));
    }

    @Test
    public void testDeleteDataSourceConfig_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> dataSourceConfigService.deleteDataSourceConfig(id), DATA_SOURCE_CONFIG_NOT_EXISTS);
    }

}
