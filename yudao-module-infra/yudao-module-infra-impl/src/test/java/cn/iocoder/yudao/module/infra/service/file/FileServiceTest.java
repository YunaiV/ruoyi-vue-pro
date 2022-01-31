package cn.iocoder.yudao.module.infra.service.file;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.test.core.util.AssertUtils;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.FilePageReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import cn.iocoder.yudao.module.infra.dal.mysql.file.FileMapper;
import cn.iocoder.yudao.module.infra.framework.file.config.FileProperties;
import cn.iocoder.yudao.module.infra.test.BaseDbUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.buildTime;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;

@Import({FileServiceImpl.class, FileProperties.class})
public class FileServiceTest extends BaseDbUnitTest {

    @Resource
    private FileService fileService;

    @MockBean
    private FileProperties fileProperties;

    @Resource
    private FileMapper fileMapper;

    @Test
    public void testGetFilePage() {
        // mock 数据
        FileDO dbFile = randomPojo(FileDO.class, o -> { // 等会查询到
            o.setId("yudao");
            o.setType("jpg");
            o.setCreateTime(buildTime(2021, 1, 15));
        });
        fileMapper.insert(dbFile);
        // 测试 id 不匹配
        fileMapper.insert(ObjectUtils.cloneIgnoreId(dbFile, o -> o.setId("tudou")));
        // 测试 type 不匹配
        fileMapper.insert(ObjectUtils.cloneIgnoreId(dbFile, o -> {
            o.setId("yudao02");
            o.setType("png");
        }));
        // 测试 createTime 不匹配
        fileMapper.insert(ObjectUtils.cloneIgnoreId(dbFile, o -> {
            o.setId("yudao03");
            o.setCreateTime(buildTime(2020, 1, 15));
        }));
        // 准备参数
        FilePageReqVO reqVO = new FilePageReqVO();
        reqVO.setId("yudao");
        reqVO.setType("jp");
        reqVO.setBeginCreateTime(buildTime(2021, 1, 10));
        reqVO.setEndCreateTime(buildTime(2021, 1, 20));

        // 调用
        PageResult<FileDO> pageResult = fileService.getFilePage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        AssertUtils.assertPojoEquals(dbFile, pageResult.getList().get(0), "content");
    }

    @Test
    public void testCreateFile_success() {
        // 准备参数
        String path = randomString();
        byte[] content = ResourceUtil.readBytes("file/erweima.jpg");

        // 调用
        String url = fileService.createFile(path, content);
        // 断言
        assertEquals(fileProperties.getBasePath() + path, url);
        // 校验数据
        FileDO file = fileMapper.selectById(path);
        assertEquals(path, file.getId());
        assertEquals("jpg", file.getType());
        assertArrayEquals(content, file.getContent());
    }

    @Test
    public void testCreateFile_exists() {
        // mock 数据
        FileDO dbFile = randomPojo(FileDO.class);
        fileMapper.insert(dbFile);
        // 准备参数
        String path = dbFile.getId(); // 模拟已存在
        byte[] content = ResourceUtil.readBytes("file/erweima.jpg");

        // 调用，并断言异常
        assertServiceException(() -> fileService.createFile(path, content), FILE_PATH_EXISTS);
    }

    @Test
    public void testDeleteFile_success() {
        // mock 数据
        FileDO dbFile = randomPojo(FileDO.class);
        fileMapper.insert(dbFile);// @Sql: 先插入出一条存在的数据
        // 准备参数
        String id = dbFile.getId();

        // 调用
        fileService.deleteFile(id);
        // 校验数据不存在了
        assertNull(fileMapper.selectById(id));
    }

    @Test
    public void testDeleteFile_notExists() {
        // 准备参数
        String id = randomString();

        // 调用, 并断言异常
        assertServiceException(() -> fileService.deleteFile(id), FILE_NOT_EXISTS);
    }

}
