package cn.iocoder.yudao.coreservice.modules.infra.service.file;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.iocoder.yudao.coreservice.BaseDbUnitTest;
import cn.iocoder.yudao.coreservice.modules.infra.dal.dataobject.file.InfFileDO;
import cn.iocoder.yudao.coreservice.modules.infra.dal.mysql.file.InfFileCoreMapper;
import cn.iocoder.yudao.coreservice.modules.infra.framework.file.config.FileProperties;
import cn.iocoder.yudao.coreservice.modules.infra.service.file.impl.InfFileCoreServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.coreservice.modules.infra.enums.SysErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static org.junit.jupiter.api.Assertions.*;

@Import({InfFileCoreServiceImpl.class, FileProperties.class})
public class InfFileCoreServiceTest extends BaseDbUnitTest {

    @Resource
    private InfFileCoreService fileCoreService;

    @MockBean
    private FileProperties fileProperties;

    @Resource
    private InfFileCoreMapper fileMapper;

    @Test
    public void testCreateFile_success() {
        // 准备参数
        String path = randomString();
        byte[] content = ResourceUtil.readBytes("file/erweima.jpg");

        // 调用
        String url = fileCoreService.createFile(path, content);
        // 断言
        assertEquals(fileProperties.getBasePath() + path, url);
        // 校验数据
        InfFileDO file = fileMapper.selectById(path);
        assertEquals(path, file.getId());
        assertEquals("jpg", file.getType());
        assertArrayEquals(content, file.getContent());
    }

    @Test
    public void testCreateFile_exists() {
        // mock 数据
        InfFileDO dbFile = randomPojo(InfFileDO.class);
        fileMapper.insert(dbFile);
        // 准备参数
        String path = dbFile.getId(); // 模拟已存在
        byte[] content = ResourceUtil.readBytes("file/erweima.jpg");

        // 调用，并断言异常
        assertServiceException(() -> fileCoreService.createFile(path, content), FILE_PATH_EXISTS);
    }

    @Test
    public void testDeleteFile_success() {
        // mock 数据
        InfFileDO dbFile = randomPojo(InfFileDO.class);
        fileMapper.insert(dbFile);// @Sql: 先插入出一条存在的数据
        // 准备参数
        String id = dbFile.getId();

        // 调用
        fileCoreService.deleteFile(id);
        // 校验数据不存在了
        assertNull(fileMapper.selectById(id));
    }

    @Test
    public void testDeleteFile_notExists() {
        // 准备参数
        String id = randomString();

        // 调用, 并断言异常
        assertServiceException(() -> fileCoreService.deleteFile(id), FILE_NOT_EXISTS);
    }

}
