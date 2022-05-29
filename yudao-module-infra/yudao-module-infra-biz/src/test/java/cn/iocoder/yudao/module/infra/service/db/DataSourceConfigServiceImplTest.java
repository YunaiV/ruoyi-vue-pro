package cn.iocoder.yudao.module.infra.service.db;

import cn.hutool.core.util.ReflectUtil;
import cn.iocoder.yudao.framework.mybatis.core.type.EncryptTypeHandler;
import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.infra.controller.admin.db.vo.DataSourceConfigCreateReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.db.vo.DataSourceConfigUpdateReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.db.DataSourceConfigDO;
import cn.iocoder.yudao.module.infra.dal.mysql.db.DataSourceConfigMapper;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.DATA_SOURCE_CONFIG_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

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

    @MockBean
    private DynamicDataSourceProperties dynamicDataSourceProperties;

    @BeforeEach
    public void setUp() {
        // mock 一个空实现的 StringEncryptor，避免 EncryptTypeHandler 报错
        ReflectUtil.setFieldValue(EncryptTypeHandler.class, "encryptor", stringEncryptor);
        when(stringEncryptor.encrypt(anyString())).then((Answer<String>) invocation -> invocation.getArgument(0));
        when(stringEncryptor.decrypt(anyString())).then((Answer<String>) invocation -> invocation.getArgument(0));
    }

    @Test
    public void testCreateDataSourceConfig_success() {
        try (MockedStatic<JdbcUtils> databaseUtilsMock = mockStatic(JdbcUtils.class)) {
            // 准备参数
            DataSourceConfigCreateReqVO reqVO = randomPojo(DataSourceConfigCreateReqVO.class);
            // mock 方法
            databaseUtilsMock.when(() -> JdbcUtils.isConnectionOK(eq(reqVO.getUrl()),
                    eq(reqVO.getUsername()), eq(reqVO.getPassword()))).thenReturn(true);

            // 调用
            Long dataSourceConfigId = dataSourceConfigService.createDataSourceConfig(reqVO);
            // 断言
            assertNotNull(dataSourceConfigId);
            // 校验记录的属性是否正确
            DataSourceConfigDO dataSourceConfig = dataSourceConfigMapper.selectById(dataSourceConfigId);
            assertPojoEquals(reqVO, dataSourceConfig);
        }
    }

    @Test
    public void testUpdateDataSourceConfig_success() {
        try (MockedStatic<JdbcUtils> databaseUtilsMock = mockStatic(JdbcUtils.class)) {
            // mock 数据
            DataSourceConfigDO dbDataSourceConfig = randomPojo(DataSourceConfigDO.class);
            dataSourceConfigMapper.insert(dbDataSourceConfig);// @Sql: 先插入出一条存在的数据
            // 准备参数
            DataSourceConfigUpdateReqVO reqVO = randomPojo(DataSourceConfigUpdateReqVO.class, o -> {
                o.setId(dbDataSourceConfig.getId()); // 设置更新的 ID
            });
            // mock 方法
//            when(stringEncryptor.encrypt(eq(reqVO.getPassword()))).thenReturn("123456");
            databaseUtilsMock.when(() -> JdbcUtils.isConnectionOK(eq(reqVO.getUrl()),
                    eq(reqVO.getUsername()), eq(reqVO.getPassword()))).thenReturn(true);

            // 调用
            dataSourceConfigService.updateDataSourceConfig(reqVO);
            // 校验是否更新正确
            DataSourceConfigDO dataSourceConfig = dataSourceConfigMapper.selectById(reqVO.getId()); // 获取最新的
            assertPojoEquals(reqVO, dataSourceConfig);
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

    @Test // 测试使用 password 查询，可以查询到数据
    public void testSelectPassword() {
        // mock 数据
        DataSourceConfigDO dbDataSourceConfig = randomPojo(DataSourceConfigDO.class);
        dataSourceConfigMapper.insert(dbDataSourceConfig);// @Sql: 先插入出一条存在的数据

        // 调用
        DataSourceConfigDO result = dataSourceConfigMapper.selectOne(DataSourceConfigDO::getPassword,
                EncryptTypeHandler.encrypt(dbDataSourceConfig.getPassword()));
        System.out.println(result);
    }

}
