package cn.iocoder.yudao.module.mp.service.material;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import cn.iocoder.yudao.module.mp.controller.admin.material.vo.MpMaterialUploadNewsImageReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.material.vo.MpMaterialUploadPermanentReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.material.vo.MpMaterialUploadTemporaryReqVO;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import cn.iocoder.yudao.module.mp.dal.mysql.material.MpMaterialMapper;
import cn.iocoder.yudao.module.mp.framework.mp.core.MpServiceFactory;
import cn.iocoder.yudao.module.mp.service.account.MpAccountService;
import me.chanjar.weixin.common.bean.result.WxMediaUploadResult;
import me.chanjar.weixin.common.error.WxError;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpMaterialService;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.material.WxMediaImgUploadResult;
import me.chanjar.weixin.mp.bean.material.WxMpMaterial;
import me.chanjar.weixin.mp.bean.material.WxMpMaterialUploadResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.mp.enums.ErrorCodeConstants.MATERIAL_UPLOAD_FAIL;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link MpMaterialServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
public class MpMaterialServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private MpMaterialServiceImpl materialService;

    @Mock
    private MpMaterialMapper mpMaterialMapper;
    @Mock
    private FileApi fileApi;
    @Mock
    private MpAccountService mpAccountService;
    @Mock
    private MpServiceFactory mpServiceFactory;

    @Mock
    private WxMpService mpService;
    @Mock
    private WxMpMaterialService mpMaterialService;

    /**
     * 模拟系统临时目录中，与上传原始文件名相同的其它文件
     */
    private File victimFile;

    @BeforeEach
    public void setUp() {
        victimFile = FileUtil.writeUtf8String("victim", new File(FileUtil.getTmpDir(), IdUtil.fastSimpleUUID() + ".jpg"));
    }

    @AfterEach
    public void tearDown() {
        FileUtil.del(victimFile);
    }

    @Test
    public void testUploadTemporaryMaterial_originalFilenameNotUsed() throws Exception {
        // 准备参数：原始文件名与临时目录中的其它文件同名
        MpMaterialUploadTemporaryReqVO reqVO = new MpMaterialUploadTemporaryReqVO();
        reqVO.setAccountId(1L);
        reqVO.setType("image");
        reqVO.setFile(buildMultipartFile(victimFile.getName()));
        // mock 方法
        mockMpService();
        mockUploadFile();
        List<File> uploadFiles = new ArrayList<>();
        when(mpMaterialService.mediaUpload(eq("image"), any(File.class))).thenAnswer(invocation -> {
            File file = invocation.getArgument(1);
            assertTempFile(file, ".jpg");
            uploadFiles.add(file);
            return buildMediaUploadResult();
        });

        // 调用
        materialService.uploadTemporaryMaterial(reqVO);
        // 断言：同名文件未被覆盖、删除；临时文件已清理
        assertEquals(1, uploadFiles.size());
        assertTrue(victimFile.exists());
        assertEquals("victim", FileUtil.readUtf8String(victimFile));
        assertFalse(uploadFiles.get(0).exists());
    }

    @Test
    public void testUploadTemporaryMaterial_traversalFilename() throws Exception {
        // 准备参数：原始文件名包含目录穿越与绝对路径
        MpMaterialUploadTemporaryReqVO reqVO = new MpMaterialUploadTemporaryReqVO();
        reqVO.setAccountId(1L);
        reqVO.setType("image");
        reqVO.setFile(buildMultipartFile("../../" + victimFile.getAbsolutePath()));
        // mock 方法
        mockMpService();
        mockUploadFile();
        List<File> uploadFiles = new ArrayList<>();
        when(mpMaterialService.mediaUpload(eq("image"), any(File.class))).thenAnswer(invocation -> {
            File file = invocation.getArgument(1);
            assertTempFile(file, ".jpg");
            uploadFiles.add(file);
            return buildMediaUploadResult();
        });

        // 调用
        materialService.uploadTemporaryMaterial(reqVO);
        // 断言
        assertEquals("victim", FileUtil.readUtf8String(victimFile));
        assertFalse(uploadFiles.get(0).exists());
    }

    @Test
    public void testUploadTemporaryMaterial_sameFilenameConcurrent() throws Exception {
        // 准备参数：两次上传相同的原始文件名
        MpMaterialUploadTemporaryReqVO reqVO = new MpMaterialUploadTemporaryReqVO();
        reqVO.setAccountId(1L);
        reqVO.setType("image");
        reqVO.setFile(buildMultipartFile("same.jpg"));
        // mock 方法
        mockMpService();
        mockUploadFile();
        List<File> uploadFiles = new ArrayList<>();
        when(mpMaterialService.mediaUpload(eq("image"), any(File.class))).thenAnswer(invocation -> {
            File file = invocation.getArgument(1);
            assertTempFile(file, ".jpg");
            uploadFiles.add(file);
            return buildMediaUploadResult();
        });

        // 调用
        materialService.uploadTemporaryMaterial(reqVO);
        materialService.uploadTemporaryMaterial(reqVO);
        // 断言：每次都是独立的临时文件
        assertEquals(2, uploadFiles.size());
        assertNotEquals(uploadFiles.get(0).getAbsolutePath(), uploadFiles.get(1).getAbsolutePath());
    }

    @Test
    public void testUploadTemporaryMaterial_wxError() throws Exception {
        // 准备参数
        MpMaterialUploadTemporaryReqVO reqVO = new MpMaterialUploadTemporaryReqVO();
        reqVO.setAccountId(1L);
        reqVO.setType("image");
        reqVO.setFile(buildMultipartFile(victimFile.getName()));
        // mock 方法
        mockMpService();
        List<File> uploadFiles = new ArrayList<>();
        when(mpMaterialService.mediaUpload(eq("image"), any(File.class))).thenAnswer(invocation -> {
            uploadFiles.add(invocation.getArgument(1));
            throw new WxErrorException(WxError.builder().errorCode(40001).errorMsg("invalid").build());
        });

        // 调用，并断言异常
        assertServiceException(() -> materialService.uploadTemporaryMaterial(reqVO), MATERIAL_UPLOAD_FAIL, "invalid");
        // 断言：失败时，也只清理自己创建的临时文件
        assertFalse(uploadFiles.get(0).exists());
        assertEquals("victim", FileUtil.readUtf8String(victimFile));
    }

    @Test
    public void testUploadPermanentMaterial_originalFilenameNotUsed() throws Exception {
        // 准备参数
        MpMaterialUploadPermanentReqVO reqVO = new MpMaterialUploadPermanentReqVO();
        reqVO.setAccountId(1L);
        reqVO.setType("image");
        reqVO.setName("永久素材");
        reqVO.setFile(buildMultipartFile(victimFile.getName()));
        // mock 方法
        mockMpService();
        mockUploadFile();
        List<File> uploadFiles = new ArrayList<>();
        when(mpMaterialService.materialFileUpload(eq("image"), any(WxMpMaterial.class))).thenAnswer(invocation -> {
            WxMpMaterial material = invocation.getArgument(1);
            assertTempFile(material.getFile(), ".jpg");
            uploadFiles.add(material.getFile());
            WxMpMaterialUploadResult result = new WxMpMaterialUploadResult();
            result.setMediaId("media_02");
            return result;
        });

        // 调用
        materialService.uploadPermanentMaterial(reqVO);
        // 断言
        assertEquals("victim", FileUtil.readUtf8String(victimFile));
        assertFalse(uploadFiles.get(0).exists());
    }

    @Test
    public void testUploadNewsImage_originalFilenameNotUsed() throws Exception {
        // 准备参数：扩展名不合法时，不保留扩展名
        MpMaterialUploadNewsImageReqVO reqVO = new MpMaterialUploadNewsImageReqVO();
        reqVO.setAccountId(1L);
        reqVO.setFile(buildMultipartFile(victimFile.getName() + ".p%ng"));
        // mock 方法
        when(mpServiceFactory.getRequiredMpService(eq(1L))).thenReturn(mpService);
        when(mpService.getMaterialService()).thenReturn(mpMaterialService);
        List<File> uploadFiles = new ArrayList<>();
        when(mpMaterialService.mediaImgUpload(any(File.class))).thenAnswer(invocation -> {
            File file = invocation.getArgument(0);
            assertTempFile(file, ".tmp");
            uploadFiles.add(file);
            WxMediaImgUploadResult result = new WxMediaImgUploadResult();
            result.setUrl("https://mmbiz.qpic.cn/test.jpg");
            return result;
        });

        // 调用
        String url = materialService.uploadNewsImage(reqVO);
        // 断言
        assertEquals("https://mmbiz.qpic.cn/test.jpg", url);
        assertEquals("victim", FileUtil.readUtf8String(victimFile));
        assertFalse(uploadFiles.get(0).exists());
    }

    private void mockMpService() {
        when(mpServiceFactory.getRequiredMpService(eq(1L))).thenReturn(mpService);
        when(mpService.getMaterialService()).thenReturn(mpMaterialService);
    }

    private void mockUploadFile() {
        when(fileApi.createFile(any(byte[].class), anyString())).thenReturn("https://www.iocoder.cn/media.jpg");
        when(mpAccountService.getRequiredAccount(eq(1L))).thenReturn(new MpAccountDO().setId(1L).setAppId("wx_app"));
    }

    private static WxMediaUploadResult buildMediaUploadResult() {
        WxMediaUploadResult result = new WxMediaUploadResult();
        result.setMediaId("media_01");
        return result;
    }

    private static MockMultipartFile buildMultipartFile(String originalFilename) {
        return new MockMultipartFile("file", originalFilename, "image/jpeg",
                "content".getBytes(StandardCharsets.UTF_8));
    }

    private static void assertTempFile(File file, String suffix) throws Exception {
        assertTrue(file.exists());
        assertEquals(FileUtil.getTmpDir().getCanonicalFile(), file.getParentFile().getCanonicalFile());
        assertTrue(file.getName().startsWith("mp-material-"));
        assertTrue(file.getName().endsWith(suffix));
        assertEquals("content", FileUtil.readUtf8String(file));
    }

}
