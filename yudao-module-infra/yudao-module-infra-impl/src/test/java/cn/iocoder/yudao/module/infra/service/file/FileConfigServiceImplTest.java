package cn.iocoder.yudao.module.infra.service.file;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.config.FileConfigCreateReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.config.FileConfigPageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.config.FileConfigUpdateReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileConfigDO;
import cn.iocoder.yudao.module.infra.dal.mysql.file.FileConfigMapper;
import cn.iocoder.yudao.module.infra.test.BaseDbUnitTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.FILE_CONFIG_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    public void testCreateFileConfig_success() {
        // 准备参数
        FileConfigCreateReqVO reqVO = randomPojo(FileConfigCreateReqVO.class);

        // 调用
        Long fileConfigId = fileConfigService.createFileConfig(reqVO);
        // 断言
        assertNotNull(fileConfigId);
        // 校验记录的属性是否正确
        FileConfigDO fileConfig = fileConfigMapper.selectById(fileConfigId);
        assertPojoEquals(reqVO, fileConfig);
    }

    @Test
    public void testUpdateFileConfig_success() {
        // mock 数据
        FileConfigDO dbFileConfig = randomPojo(FileConfigDO.class);
        fileConfigMapper.insert(dbFileConfig);// @Sql: 先插入出一条存在的数据
        // 准备参数
        FileConfigUpdateReqVO reqVO = randomPojo(FileConfigUpdateReqVO.class, o -> {
            o.setId(dbFileConfig.getId()); // 设置更新的 ID
        });

        // 调用
        fileConfigService.updateFileConfig(reqVO);
        // 校验是否更新正确
        FileConfigDO fileConfig = fileConfigMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, fileConfig);
    }

    @Test
    public void testUpdateFileConfig_notExists() {
        // 准备参数
        FileConfigUpdateReqVO reqVO = randomPojo(FileConfigUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> fileConfigService.updateFileConfig(reqVO), FILE_CONFIG_NOT_EXISTS);
    }

    @Test
    public void testDeleteFileConfig_success() {
        // mock 数据
        FileConfigDO dbFileConfig = randomPojo(FileConfigDO.class);
        fileConfigMapper.insert(dbFileConfig);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbFileConfig.getId();

        // 调用
        fileConfigService.deleteFileConfig(id);
       // 校验数据不存在了
       assertNull(fileConfigMapper.selectById(id));
    }

    @Test
    public void testDeleteFileConfig_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> fileConfigService.deleteFileConfig(id), FILE_CONFIG_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetFileConfigPage() {
       // mock 数据
       FileConfigDO dbFileConfig = randomPojo(FileConfigDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setStorage(null);
           o.setCreateTime(null);
       });
       fileConfigMapper.insert(dbFileConfig);
       // 测试 name 不匹配
       fileConfigMapper.insert(cloneIgnoreId(dbFileConfig, o -> o.setName(null)));
       // 测试 storage 不匹配
       fileConfigMapper.insert(cloneIgnoreId(dbFileConfig, o -> o.setStorage(null)));
       // 测试 createTime 不匹配
       fileConfigMapper.insert(cloneIgnoreId(dbFileConfig, o -> o.setCreateTime(null)));
       // 准备参数
       FileConfigPageReqVO reqVO = new FileConfigPageReqVO();
       reqVO.setName(null);
       reqVO.setStorage(null);
       reqVO.setBeginCreateTime(null);
       reqVO.setEndCreateTime(null);

       // 调用
       PageResult<FileConfigDO> pageResult = fileConfigService.getFileConfigPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbFileConfig, pageResult.getList().get(0));
    }

}
