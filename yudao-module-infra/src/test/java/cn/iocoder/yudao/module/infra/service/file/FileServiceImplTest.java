package cn.iocoder.yudao.module.infra.service.file;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.framework.test.core.util.AssertUtils;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FileCreateReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FilePageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FilePresignedUrlRespVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import cn.iocoder.yudao.module.infra.dal.mysql.file.FileMapper;
import cn.iocoder.yudao.module.infra.framework.file.core.client.FileClient;
import jakarta.annotation.Resource;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@Import({FileServiceImpl.class})
public class FileServiceImplTest extends BaseDbUnitTest {

    @Resource
    private FileServiceImpl fileService;

    @Resource
    private FileMapper fileMapper;

    @MockitoBean
    private FileConfigService fileConfigService;

    @BeforeEach
    public void setUp() {
        FileServiceImpl.PATH_PREFIX_DATE_ENABLE = true;
        FileServiceImpl.PATH_SUFFIX_TIMESTAMP_ENABLE = true;
        FileServiceImpl.PATH_SUFFIX_AS_DIRECTORY = true;
    }

    @Test
    public void testGetFilePage() {
        // mock 数据
        FileDO dbFile = randomPojo(FileDO.class, o -> { // 等会查询到
            o.setPath("yunai");
            o.setType("image/jpg");
            o.setCreateTime(buildTime(2021, 1, 15));
        });
        fileMapper.insert(dbFile);
        // 测试 path 不匹配
        fileMapper.insert(ObjectUtils.cloneIgnoreId(dbFile, o -> o.setPath("tudou")));
        // 测试 type 不匹配
        fileMapper.insert(ObjectUtils.cloneIgnoreId(dbFile, o -> {
            o.setType("image/png");
        }));
        // 测试 createTime 不匹配
        fileMapper.insert(ObjectUtils.cloneIgnoreId(dbFile, o -> {
            o.setCreateTime(buildTime(2020, 1, 15));
        }));
        // 准备参数
        FilePageReqVO reqVO = new FilePageReqVO();
        reqVO.setPath("yunai");
        reqVO.setType("jp");
        reqVO.setCreateTime((new LocalDateTime[]{buildTime(2021, 1, 10), buildTime(2021, 1, 20)}));

        // 调用
        PageResult<FileDO> pageResult = fileService.getFilePage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        AssertUtils.assertPojoEquals(dbFile, pageResult.getList().get(0));
    }

    /**
     * content、name、directory、type 都非空
     */
    @Test
    public void testCreateFile_success_01() throws Exception {
        // 准备参数
        byte[] content = ResourceUtil.readBytes("file/erweima.jpg");
        String name = "单测文件名";
        String directory = randomString();
        String type = "image/jpeg";
        // mock Master 文件客户端
        FileClient client = mock(FileClient.class);
        when(fileConfigService.getMasterFileClient()).thenReturn(client);
        String url = randomString();
        AtomicReference<String> pathRef = new AtomicReference<>();
        when(client.upload(same(content), argThat(path -> {
            assertTrue(path.matches(directory + "/\\d{8}/\\d+/" + name + ".jpg"));
            pathRef.set(path);
            return true;
        }), eq(type))).thenReturn(url);
        when(client.getId()).thenReturn(10L);
        // 调用
        String result = fileService.createFile(content, name, directory, type);
        // 断言
        assertEquals(result, url);
        // 校验数据
        FileDO file = fileMapper.selectOne(FileDO::getUrl, url);
        assertEquals(10L, file.getConfigId());
        assertEquals(pathRef.get(), file.getPath());
        assertEquals(url, file.getUrl());
        assertEquals(type, file.getType());
        assertEquals(content.length, file.getSize());
    }

    /**
     * content 非空，其它都空
     */
    @Test
    public void testCreateFile_success_02() throws Exception {
        // 准备参数
        byte[] content = ResourceUtil.readBytes("file/erweima.jpg");
        // mock Master 文件客户端
        String type = "image/jpeg";
        FileClient client = mock(FileClient.class);
        when(fileConfigService.getMasterFileClient()).thenReturn(client);
        String url = randomString();
        AtomicReference<String> pathRef = new AtomicReference<>();
        when(client.upload(same(content), argThat(path -> {
            assertTrue(path.matches("\\d{8}/\\d+/6318848e882d8a7e7e82789d87608f684ee52d41966bfc8cad3ce15aad2b970e\\.jpg"));
            pathRef.set(path);
            return true;
        }), eq(type))).thenReturn(url);
        when(client.getId()).thenReturn(10L);
        // 调用
        String result = fileService.createFile(content, null, null, null);
        // 断言
        assertEquals(result, url);
        // 校验数据
        FileDO file = fileMapper.selectOne(FileDO::getUrl, url);
        assertEquals(10L, file.getConfigId());
        assertEquals(pathRef.get(), file.getPath());
        assertEquals(url, file.getUrl());
        assertEquals(type, file.getType());
        assertEquals(content.length, file.getSize());
    }

    @Test
    public void testCreateFile_success_image() throws Exception {
        for (String extension : new String[]{"jpg", "png", "gif", "bmp"}) {
            // 准备参数
            String type = "jpg".equals(extension) ? "image/jpeg" : "image/" + extension;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            assertTrue(ImageIO.write(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB), extension, outputStream));
            byte[] content = outputStream.toByteArray();
            String name = "头像." + extension.toUpperCase(Locale.ROOT);
            // mock 方法
            FileClient client = mock(FileClient.class);
            when(fileConfigService.getMasterFileClient()).thenReturn(client);
            String url = randomString();
            when(client.upload(same(content), anyString(), eq(type))).thenReturn(url);

            // 调用
            String result = fileService.createFile(content, name, null, "application/octet-stream");
            // 断言
            assertEquals(url, result);
            FileDO file = fileMapper.selectOne(FileDO::getUrl, url);
            assertEquals(name, file.getName());
            assertEquals(type, file.getType());
        }
    }

    @Test
    public void testCreateFile_success_excel() throws Exception {
        for (String extension : new String[]{"xls", "xlsx"}) {
            // 准备参数
            String type = "xls".equals(extension) ? "application/vnd.ms-excel"
                    : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            byte[] content;
            try (Workbook workbook = "xls".equals(extension) ? new HSSFWorkbook() : new XSSFWorkbook();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.createSheet("用户").createRow(0).createCell(0).setCellValue("芋道源码");
                workbook.write(outputStream);
                content = outputStream.toByteArray();
            }
            // mock 方法
            FileClient client = mock(FileClient.class);
            when(fileConfigService.getMasterFileClient()).thenReturn(client);
            String url = randomString();
            when(client.upload(same(content), anyString(), eq(type))).thenReturn(url);

            // 调用
            String result = fileService.createFile(content, "用户." + extension, null, null);
            // 断言
            assertEquals(url, result);
            assertEquals(type, fileMapper.selectOne(FileDO::getUrl, url).getType());
        }
    }

    @Test
    public void testCreateFile_success_detectType() throws Exception {
        for (String declaredType : new String[]{null, "", "image/jpg", "text/html", "application/octet-stream"}) {
            // 准备参数
            byte[] content = ResourceUtil.readBytes("file/erweima.jpg");
            // mock 方法
            FileClient client = mock(FileClient.class);
            when(fileConfigService.getMasterFileClient()).thenReturn(client);
            String url = randomString();
            when(client.upload(same(content), anyString(), eq("image/jpeg"))).thenReturn(url);

            // 调用
            String result = fileService.createFile(content, "头像.jpg", null, declaredType);
            // 断言
            assertEquals(url, result);
            assertEquals("image/jpeg", fileMapper.selectOne(FileDO::getUrl, url).getType());
        }
    }

    @Test
    public void testCreateFile_success_document() throws Exception {
        for (String[] document : new String[][]{
                {"test.svg", "<svg xmlns=\"http://www.w3.org/2000/svg\"><script>alert(1)</script></svg>", "image/svg+xml"},
                {"test.html", "<html><script>alert(1)</script></html>", "text/html"},
                {"test.xml", "<?xml version=\"1.0\"?><root>test</root>", "application/xml"},
                {"test.markdown", "# test\n\nhello", "text/markdown"},
                {"test.eml", "From: a@example.com\r\nTo: b@example.com\r\nSubject: test\r\n\r\nhello", "message/rfc822"}}) {
            // 准备参数
            String name = document[0];
            byte[] content = document[1].getBytes(StandardCharsets.UTF_8);
            String type = document[2];
            // mock 方法
            FileClient client = mock(FileClient.class);
            when(fileConfigService.getMasterFileClient()).thenReturn(client);
            String url = randomString();
            when(client.upload(same(content), anyString(), eq(type))).thenReturn(url);

            // 调用
            String result = fileService.createFile(content, name, null, "application/octet-stream");
            // 断言
            assertEquals(url, result);
            assertEquals(type, fileMapper.selectOne(FileDO::getUrl, url).getType());
        }
    }

    @Test
    public void testCreateFile_success_fakeExtensionDetectContent() throws Exception {
        for (String name : new String[]{"图标.xml", "图标.txt"}) {
            // 准备参数：SVG 内容伪装成其他扩展名
            byte[] content = "<svg xmlns=\"http://www.w3.org/2000/svg\"><script>alert(1)</script></svg>"
                    .getBytes(StandardCharsets.UTF_8);
            // mock 方法
            FileClient client = mock(FileClient.class);
            when(fileConfigService.getMasterFileClient()).thenReturn(client);
            String url = randomString();
            when(client.upload(same(content), anyString(), eq("image/svg+xml"))).thenReturn(url);

            // 调用
            String result = fileService.createFile(content, name, null, "text/plain");
            // 断言：以内容识别的类型上传，文件存储器据此按附件下载
            assertEquals(url, result);
            FileDO file = fileMapper.selectOne(FileDO::getUrl, url);
            assertEquals(name, file.getName());
            assertEquals("image/svg+xml", file.getType());
        }
    }

    @Test
    public void testCreateFile_success_opus() throws Exception {
        // 准备参数
        byte[] content = new byte[64];
        System.arraycopy("OggS".getBytes(StandardCharsets.US_ASCII), 0, content, 0, 4);
        System.arraycopy("OpusHead".getBytes(StandardCharsets.US_ASCII), 0, content, 28, 8);
        // mock 方法
        FileClient client = mock(FileClient.class);
        when(fileConfigService.getMasterFileClient()).thenReturn(client);
        String url = randomString();
        when(client.upload(same(content), anyString(), eq("audio/opus"))).thenReturn(url);

        // 调用
        String result = fileService.createFile(content, "voice.ogg", null, "audio/ogg;codecs=opus");
        // 断言
        assertEquals(url, result);
        assertEquals("audio/opus", fileMapper.selectOne(FileDO::getUrl, url).getType());
    }

    @Test
    public void testCreateFile_typeNotAllowed_fakeImage() {
        for (String text : new String[]{"<html><script>alert(1)</script></html>",
                "<svg xmlns=\"http://www.w3.org/2000/svg\"><script>alert(1)</script></svg>", "普通文本"}) {
            // 准备参数
            byte[] content = text.getBytes(StandardCharsets.UTF_8);

            // 调用，并断言异常
            assertServiceException(() -> fileService.createFile(content, "头像.jpg", null, "image/jpeg"),
                    FILE_TYPE_NOT_ALLOWED);
            // 断言未上传文件、未保存记录
            verifyNoInteractions(fileConfigService);
            assertEquals(0L, fileMapper.selectCount());
        }
    }

    @Test
    public void testCreateFile_typeNotAllowed_extension() {
        for (String name : new String[]{"头像.svg", "头像.js", "头像.exe", "头像.unknown", "头像.png"}) {
            // 准备参数
            byte[] content = ResourceUtil.readBytes("file/erweima.jpg");

            // 调用，并断言异常
            assertServiceException(() -> fileService.createFile(content, name, null, "image/jpeg"), FILE_TYPE_NOT_ALLOWED);
            // 校验调用
            verifyNoInteractions(fileConfigService);
        }
    }

    @Test
    public void testCreateFile_success_bin() throws Exception {
        for (String name : new String[]{"固件.bin", "FIRMWARE.BIN", "firmware.Bin"}) {
            // 准备参数：IoT OTA 固件，Tika 无法识别的二进制内容
            byte[] content = buildFirmwareContent();
            // mock 方法
            FileClient client = mock(FileClient.class);
            when(fileConfigService.getMasterFileClient()).thenReturn(client);
            String url = randomString();
            when(client.upload(same(content), anyString(), eq("application/octet-stream"))).thenReturn(url);

            // 调用：不信任客户端传入的 type
            String result = fileService.createFile(content, name, "iot/ota", "image/png");
            // 断言
            assertEquals(url, result);
            FileDO file = fileMapper.selectOne(FileDO::getUrl, url);
            assertEquals(name, file.getName());
            assertEquals("application/octet-stream", file.getType());
            assertEquals((long) content.length, file.getSize());
        }
    }

    @Test
    public void testCreateFile_typeNotAllowed_binContent() {
        byte[] elf = buildFirmwareContent();
        System.arraycopy(new byte[]{0x7F, 'E', 'L', 'F', 2, 1, 1}, 0, elf, 0, 7);
        elf[16] = 2; // e_type = ET_EXEC
        byte[] exe = buildFirmwareContent();
        exe[0] = 'M';
        exe[1] = 'Z';
        for (byte[] content : new byte[][]{elf, exe,
                "#!/bin/sh\nrm -rf /tmp/test\n".getBytes(StandardCharsets.UTF_8),
                "<html><script>alert(1)</script></html>".getBytes(StandardCharsets.UTF_8),
                "<svg xmlns=\"http://www.w3.org/2000/svg\"><script>alert(1)</script></svg>".getBytes(StandardCharsets.UTF_8),
                ResourceUtil.readBytes("file/erweima.jpg")}) {
            // 调用，并断言异常：.bin 的内容，必须是无法识别为其他类型的二进制
            assertServiceException(() -> fileService.createFile(content, "固件.bin", null, "application/octet-stream"),
                    FILE_TYPE_NOT_ALLOWED);
            // 断言未上传文件、未保存记录
            verifyNoInteractions(fileConfigService);
            assertEquals(0L, fileMapper.selectCount());
        }
    }

    @Test
    public void testCreateFile_typeNotAllowed_binaryOtherExtension() {
        for (String name : new String[]{"固件.so", "固件.dump", "固件.unknown", "固件"}) {
            // 准备参数：同样识别为 application/octet-stream 的其它后缀，不放行
            byte[] content = buildFirmwareContent();

            // 调用，并断言异常
            assertServiceException(() -> fileService.createFile(content, name, null, "application/octet-stream"),
                    FILE_TYPE_NOT_ALLOWED);
            // 断言未上传文件、未保存记录
            verifyNoInteractions(fileConfigService);
            assertEquals(0L, fileMapper.selectCount());
        }
    }

    /**
     * 构造固件内容：类似 ARM Cortex-M 的向量表开头，包含 0x00 等不可打印字节，Tika 无法识别
     */
    private static byte[] buildFirmwareContent() {
        byte[] content = new byte[256];
        byte[] header = new byte[]{0x00, 0x50, 0x00, 0x20, (byte) 0xC1, 0x01, 0x00, 0x08,
                (byte) 0xC9, 0x01, 0x00, 0x08, (byte) 0xCB, 0x01, 0x00, 0x08};
        System.arraycopy(header, 0, content, 0, header.length);
        for (int i = header.length; i < content.length; i++) {
            content[i] = (byte) (i * 7);
        }
        return content;
    }

    @Test
    public void testCreateFile_fileIsEmpty() {
        // 准备参数
        byte[] content = new byte[0];

        // 调用，并断言异常
        assertServiceException(() -> fileService.createFile(content, "头像.jpg", null, null), FILE_IS_EMPTY);
        // 校验调用
        verifyNoInteractions(fileConfigService);
    }

    @Test
    public void testPresignPutUrl_success() {
        for (String extension : new String[]{"jpg", "JPEG", "jfif", "png", "gif", "webp", "bmp", "ico", "tiff",
                "avif", "heic", "heif", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "md",
                "zip", "rar", "mp3", "mp4", "svg", "html", "htm", "xml", "markdown", "eml", "msg", "epub",
                "json", "opus", "ogg", "m4a", "webm", "amr", "wma", "wmv", "flv", "mkv", "bin", "BIN"}) {
            // 准备参数
            String name = "测试文件." + extension;
            // mock 方法
            FileClient client = mock(FileClient.class);
            when(fileConfigService.getMasterFileClient()).thenReturn(client);
            when(client.getId()).thenReturn(10L);
            when(client.presignPutUrl(anyString())).thenReturn("upload-url");
            when(client.presignGetUrl(anyString(), isNull())).thenReturn("visit-url");

            // 调用
            FilePresignedUrlRespVO result = fileService.presignPutUrl(name, "avatar");
            // 断言
            assertEquals(10L, result.getConfigId());
            assertTrue(result.getPath().endsWith("/" + name));
            assertEquals("upload-url", result.getUploadUrl());
            assertEquals("visit-url", result.getUrl());
        }
    }

    @Test
    public void testPresignPutUrl_typeNotAllowed() {
        // 准备参数
        for (String name : new String[]{"test.php", "test.jsp", "test.js", "test.exe", "test.unknown", "test",
                "test.so", "test.dump", "test.sh", "test.bin.exe"}) {
            // 调用，并断言异常
            assertServiceException(() -> fileService.presignPutUrl(name, null), FILE_TYPE_NOT_ALLOWED);
            // 校验调用
            verifyNoInteractions(fileConfigService);
        }
    }

    @Test
    public void testDeleteFile_success() throws Exception {
        // mock 数据
        FileDO dbFile = randomPojo(FileDO.class, o -> o.setConfigId(10L).setPath("tudou.jpg"));
        fileMapper.insert(dbFile);// @Sql: 先插入出一条存在的数据
        // mock Master 文件客户端
        FileClient client = mock(FileClient.class);
        when(fileConfigService.getFileClient(eq(10L))).thenReturn(client);
        // 准备参数
        Long id = dbFile.getId();

        // 调用
        fileService.deleteFile(id);
        // 校验数据不存在了
        assertNull(fileMapper.selectById(id));
        // 校验调用
        verify(client).delete(eq("tudou.jpg"));
    }

    @Test
    public void testDeleteFile_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> fileService.deleteFile(id), FILE_NOT_EXISTS);
    }

    @Test
    public void testDeleteFile_pathInvalid() {
        // mock 数据
        FileDO dbFile = randomPojo(FileDO.class, o -> o.setConfigId(10L).setPath("../tudou.jpg"));
        fileMapper.insert(dbFile);

        // 调用，并断言异常
        assertServiceException(() -> fileService.deleteFile(dbFile.getId()), FILE_PATH_INVALID);
    }

    @Test
    public void testGetFileContent() throws Exception {
        // 准备参数
        Long configId = 10L;
        String path = "tudou.jpg";
        // mock 方法
        FileClient client = mock(FileClient.class);
        when(fileConfigService.getFileClient(eq(10L))).thenReturn(client);
        byte[] content = new byte[]{};
        when(client.getContent(eq("tudou.jpg"))).thenReturn(content);

        // 调用
        byte[] result = fileService.getFileContent(configId, path);
        // 断言
        assertSame(result, content);
    }

    @Test
    public void testGetFileContent_pathInvalid() {
        // 准备参数
        Long configId = 10L;
        String path = "../tudou.jpg";

        // 调用，并断言异常
        assertServiceException(() -> fileService.getFileContent(configId, path), FILE_PATH_INVALID);
    }

    @Test
    public void testGetFileByConfigIdAndPath() {
        // mock 数据
        FileDO dbFile = randomPojo(FileDO.class, o -> o.setConfigId(10L).setPath("avatar/中文 100%+文件.jpg"));
        fileMapper.insert(dbFile);
        FileDO latestFile = ObjectUtils.cloneIgnoreId(dbFile, o -> o.setName("最新文件名.jpg"));
        fileMapper.insert(latestFile);
        fileMapper.insert(ObjectUtils.cloneIgnoreId(dbFile, o -> o.setPath("avatar/other.jpg")));
        fileMapper.insert(ObjectUtils.cloneIgnoreId(dbFile, o -> o.setConfigId(20L)));

        // 调用
        FileDO result = fileService.getFileByConfigIdAndPath(10L, "avatar/中文 100%+文件.jpg");

        // 断言
        AssertUtils.assertPojoEquals(latestFile, result);
    }

    @Test
    public void testCreateFileByPresignedPath_success() {
        // 准备参数
        FileCreateReqVO reqVO = randomPojo(FileCreateReqVO.class, o -> {
            o.setPath("avatar/test.jpg");
            o.setName("test.jpg");
            o.setUrl("https://www.iocoder.cn/test.jpg?token=123");
        });

        // 调用
        Long fileId = fileService.createFile(reqVO);

        // 断言
        FileDO file = fileMapper.selectById(fileId);
        assertEquals("avatar/test.jpg", file.getPath());
        assertEquals("test.jpg", file.getName());
        assertEquals("https://www.iocoder.cn/test.jpg", file.getUrl());
        assertEquals("image/jpeg", file.getType());
    }

    @Test
    public void testCreateFileByPresignedPath_typeNotAllowed() {
        for (String[] file : new String[][]{{"test.js", "avatar/test.js"}, {"test.jpg", "avatar/test.html"},
                {"test.html", "avatar/test.jpg"}, {"test.jpg", "avatar/test.png"}}) {
            // 准备参数
            String name = file[0];
            String path = file[1];
            FileCreateReqVO reqVO = randomPojo(FileCreateReqVO.class, o -> {
                o.setName(name);
                o.setPath(path);
                o.setType("image/jpeg");
            });

            // 调用，并断言异常
            assertServiceException(() -> fileService.createFile(reqVO), FILE_TYPE_NOT_ALLOWED);
            // 断言未保存记录
            assertEquals(0L, fileMapper.selectCount());
        }
    }

    @Test
    public void testCreateFileByPresignedPath_success_bin() {
        // 准备参数：IoT OTA 固件，后缀大小写不敏感
        FileCreateReqVO reqVO = randomPojo(FileCreateReqVO.class, o -> {
            o.setPath("iot/ota/firmware.bin");
            o.setName("FIRMWARE.BIN");
            o.setUrl("https://www.iocoder.cn/iot/ota/firmware.bin");
            o.setType("text/html"); // 不信任客户端传入的 type
        });

        // 调用
        Long fileId = fileService.createFile(reqVO);

        // 断言
        FileDO file = fileMapper.selectById(fileId);
        assertEquals("iot/ota/firmware.bin", file.getPath());
        assertEquals("FIRMWARE.BIN", file.getName());
        assertEquals("application/octet-stream", file.getType());
    }

    @Test
    public void testCreateFileByPresignedPath_typeNotAllowed_bin() {
        for (String[] file : new String[][]{{"firmware.bin", "iot/firmware.exe"}, {"firmware.bin", "iot/firmware.so"},
                {"firmware.so", "iot/firmware.so"}, {"firmware.exe", "iot/firmware.bin"},
                {"firmware.bin", "iot/firmware.html"}, {"firmware", "iot/firmware.bin"}}) {
            // 准备参数
            String name = file[0];
            String path = file[1];
            FileCreateReqVO reqVO = randomPojo(FileCreateReqVO.class, o -> {
                o.setName(name);
                o.setPath(path);
                o.setType("application/octet-stream");
            });

            // 调用，并断言异常
            assertServiceException(() -> fileService.createFile(reqVO), FILE_TYPE_NOT_ALLOWED);
            // 断言未保存记录
            assertEquals(0L, fileMapper.selectCount());
        }
    }

    @Test
    public void testCreateFileByPresignedPath_nameInvalid() {
        // 准备参数
        FileCreateReqVO reqVO = randomPojo(FileCreateReqVO.class, o -> {
            o.setPath("avatar/test.jpg");
            o.setName("../test.jpg");
        });

        // 调用，并断言异常
        assertServiceException(() -> fileService.createFile(reqVO), FILE_PATH_INVALID);
    }

    @Test
    public void testCreateFileByPresignedPath_pathInvalid() {
        // 准备参数
        FileCreateReqVO reqVO = randomPojo(FileCreateReqVO.class, o -> {
            o.setPath("../test.jpg");
            o.setName("test.jpg");
        });

        // 调用，并断言异常
        assertServiceException(() -> fileService.createFile(reqVO), FILE_PATH_INVALID);
    }

    @Test
    public void testGenerateUploadPath_AllEnabled() {
        // 准备参数
        String name = "test.jpg";
        String directory = "avatar";
        FileServiceImpl.PATH_PREFIX_DATE_ENABLE = true;
        FileServiceImpl.PATH_SUFFIX_TIMESTAMP_ENABLE = true;

        // 调用
        String path = fileService.generateUploadPath(name, directory);

        // 断言
        // 格式为：avatar/yyyyMMdd/{时间戳+随机数}/test.jpg
        assertTrue(path.startsWith(directory + "/"));
        // 包含日期格式：8 位数字，如 20240517
        assertTrue(path.matches(directory + "/\\d{8}/\\d+/test\\.jpg"));
    }

    @Test
    public void testGenerateUploadPath_PrefixEnabled_SuffixDisabled() {
        // 准备参数
        String name = "test.jpg";
        String directory = "avatar";
        FileServiceImpl.PATH_PREFIX_DATE_ENABLE = true;
        FileServiceImpl.PATH_SUFFIX_TIMESTAMP_ENABLE = false;

        // 调用
        String path = fileService.generateUploadPath(name, directory);

        // 断言
        // 格式为：avatar/yyyyMMdd/test.jpg
        assertTrue(path.startsWith(directory + "/"));
        // 包含日期格式：8 位数字，如 20240517
        assertTrue(path.matches(directory + "/\\d{8}/test\\.jpg"));
    }

    @Test
    public void testGenerateUploadPath_PrefixDisabled_SuffixEnabled() {
        // 准备参数
        String name = "test.jpg";
        String directory = "avatar";
        FileServiceImpl.PATH_PREFIX_DATE_ENABLE = false;
        FileServiceImpl.PATH_SUFFIX_TIMESTAMP_ENABLE = true;

        // 调用
        String path = fileService.generateUploadPath(name, directory);

        // 断言
        // 格式为：avatar/{时间戳+随机数}/test.jpg
        assertTrue(path.startsWith(directory + "/"));
        assertTrue(path.matches(directory + "/\\d+/test\\.jpg"));
    }

    @Test
    public void testGenerateUploadPath_AllDisabled() {
        // 准备参数
        String name = "test.jpg";
        String directory = "avatar";
        FileServiceImpl.PATH_PREFIX_DATE_ENABLE = false;
        FileServiceImpl.PATH_SUFFIX_TIMESTAMP_ENABLE = false;

        // 调用
        String path = fileService.generateUploadPath(name, directory);

        // 断言
        // 格式为：avatar/test.jpg
        assertEquals(directory + "/" + name, path);
    }

    @Test
    public void testGenerateUploadPath_NoExtension() {
        // 准备参数
        String name = "test";
        String directory = "avatar";
        FileServiceImpl.PATH_PREFIX_DATE_ENABLE = true;
        FileServiceImpl.PATH_SUFFIX_TIMESTAMP_ENABLE = true;

        // 调用
        String path = fileService.generateUploadPath(name, directory);

        // 断言
        // 格式为：avatar/yyyyMMdd/{时间戳+随机数}/test
        assertTrue(path.startsWith(directory + "/"));
        assertTrue(path.matches(directory + "/\\d{8}/\\d+/test"));
    }

    @Test
    public void testGenerateUploadPath_DirectoryNull() {
        // 准备参数
        String name = "test.jpg";
        String directory = null;
        FileServiceImpl.PATH_PREFIX_DATE_ENABLE = true;
        FileServiceImpl.PATH_SUFFIX_TIMESTAMP_ENABLE = true;

        // 调用
        String path = fileService.generateUploadPath(name, directory);

        // 断言
        // 格式为：yyyyMMdd/{时间戳+随机数}/test.jpg
        assertTrue(path.matches("\\d{8}/\\d+/test\\.jpg"));
    }

    @Test
    public void testGenerateUploadPath_SuffixAsName_AllEnabled() {
        // 准备参数
        String name = "test.jpg";
        String directory = "avatar";
        FileServiceImpl.PATH_PREFIX_DATE_ENABLE = true;
        FileServiceImpl.PATH_SUFFIX_TIMESTAMP_ENABLE = true;
        FileServiceImpl.PATH_SUFFIX_AS_DIRECTORY = false;

        // 调用
        String path = fileService.generateUploadPath(name, directory);

        // 断言
        // 格式为：avatar/yyyyMMdd/test_{时间戳+随机数}.jpg
        assertTrue(path.matches(directory + "/\\d{8}/test_\\d+\\.jpg"));
    }

    @Test
    public void testGenerateUploadPath_SuffixAsName_PrefixDisabled() {
        // 准备参数
        String name = "test.jpg";
        String directory = "avatar";
        FileServiceImpl.PATH_PREFIX_DATE_ENABLE = false;
        FileServiceImpl.PATH_SUFFIX_TIMESTAMP_ENABLE = true;
        FileServiceImpl.PATH_SUFFIX_AS_DIRECTORY = false;

        // 调用
        String path = fileService.generateUploadPath(name, directory);

        // 断言
        // 格式为：avatar/test_{时间戳+随机数}.jpg
        assertTrue(path.matches(directory + "/test_\\d+\\.jpg"));
    }

    @Test
    public void testGenerateUploadPath_SuffixAsName_NoExtension() {
        // 准备参数
        String name = "test";
        String directory = "avatar";
        FileServiceImpl.PATH_PREFIX_DATE_ENABLE = true;
        FileServiceImpl.PATH_SUFFIX_TIMESTAMP_ENABLE = true;
        FileServiceImpl.PATH_SUFFIX_AS_DIRECTORY = false;

        // 调用
        String path = fileService.generateUploadPath(name, directory);

        // 断言
        // 格式为：avatar/yyyyMMdd/test_{时间戳+随机数}
        assertTrue(path.matches(directory + "/\\d{8}/test_\\d+"));
    }

    @Test
    public void testGenerateUploadPath_FileNameInvalid() {
        // 准备参数
        String name = "../test.jpg";
        String directory = "avatar";
        FileServiceImpl.PATH_PREFIX_DATE_ENABLE = false;
        FileServiceImpl.PATH_SUFFIX_TIMESTAMP_ENABLE = false;

        // 调用，并断言异常
        assertServiceException(() -> fileService.generateUploadPath(name, directory), FILE_PATH_INVALID);
    }

    @Test
    public void testGenerateUploadPath_DirectoryInvalid() {
        // 准备参数
        String name = "test.jpg";
        String directory = "../avatar";

        // 调用，并断言异常
        assertServiceException(() -> fileService.generateUploadPath(name, directory), FILE_PATH_INVALID);
    }

    @Test
    public void testGenerateUploadPath_DirectoryEmpty() {
        // 准备参数
        String name = "test.jpg";
        String directory = "";
        FileServiceImpl.PATH_PREFIX_DATE_ENABLE = true;
        FileServiceImpl.PATH_SUFFIX_TIMESTAMP_ENABLE = true;

        // 调用
        String path = fileService.generateUploadPath(name, directory);

        // 断言
        // 格式为：yyyyMMdd/{时间戳+随机数}/test.jpg
        assertTrue(path.matches("\\d{8}/\\d+/test\\.jpg"));
    }

}
