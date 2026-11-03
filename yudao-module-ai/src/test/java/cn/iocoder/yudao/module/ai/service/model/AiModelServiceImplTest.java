package cn.iocoder.yudao.module.ai.service.model;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.AiModelSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.mysql.model.AiChatMapper;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.AiModelFactory;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AiModelServiceImpl} 的单元测试类
 *
 * 重点：输入模态 inputModalities 按前端传入的数组原样保存，后端不做规范化
 *
 * @author 芋道源码
 */
public class AiModelServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private AiModelServiceImpl modelService;

    @Mock
    private AiApiKeyService apiKeyService;
    @Mock
    private AiChatMapper modelMapper;
    @Mock
    private AiModelFactory modelFactory;

    @Test
    public void testCreateModel_inputModalities() {
        // 准备参数：顺序、重复按前端原样
        List<String> inputModalities = asList("image", "text");
        AiModelSaveReqVO reqVO = buildSaveReqVO(null).setInputModalities(inputModalities);

        // 调用
        modelService.createModel(reqVO);
        // 断言
        ArgumentCaptor<AiModelDO> modelCaptor = ArgumentCaptor.forClass(AiModelDO.class);
        verify(modelMapper).insert(modelCaptor.capture());
        assertEquals(inputModalities, modelCaptor.getValue().getInputModalities());
    }

    @Test
    public void testUpdateModel_inputModalities() {
        // mock 数据：库里原有 image、video
        Long id = randomLongId();
        when(modelMapper.selectById(eq(id))).thenReturn(new AiModelDO().setId(id)
                .setInputModalities(asList("text", "image", "video")));
        // 准备参数：前端只传 text
        List<String> inputModalities = asList("text");
        AiModelSaveReqVO reqVO = buildSaveReqVO(id).setInputModalities(inputModalities);

        // 调用
        modelService.updateModel(reqVO);
        // 断言：直接采用前端数组，不合并库里旧值
        ArgumentCaptor<AiModelDO> modelCaptor = ArgumentCaptor.forClass(AiModelDO.class);
        verify(modelMapper).updateForSave(modelCaptor.capture());
        assertEquals(inputModalities, modelCaptor.getValue().getInputModalities());
    }

    @Test
    public void testSaveReqVO_inputModalitiesValid() {
        // 准备参数
        AiModelSaveReqVO reqVO = buildSaveReqVO(null).setInputModalities(asList("text", "image", "video"));

        // 调用，并断言：合法枚举值通过
        assertDoesNotThrow(() -> ValidationUtils.validate(reqVO));
    }

    @Test
    public void testSaveReqVO_inputModalitiesInvalid() {
        // 准备参数：audio 不在枚举中
        AiModelSaveReqVO reqVO = buildSaveReqVO(null).setInputModalities(asList("text", "audio"));

        // 调用，并断言：@InEnum 拒绝非法值
        assertThrows(ConstraintViolationException.class, () -> ValidationUtils.validate(reqVO));
    }

    private static AiModelSaveReqVO buildSaveReqVO(Long id) {
        AiModelSaveReqVO reqVO = new AiModelSaveReqVO();
        reqVO.setId(id);
        reqVO.setKeyId(randomLongId());
        reqVO.setName("自定义视觉模型");
        reqVO.setModel("my-vlm-x");
        reqVO.setPlatform(AiPlatformEnum.DEEP_SEEK.getPlatform());
        reqVO.setType(AiModelTypeEnum.CHAT.getType());
        reqVO.setSort(1);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return reqVO;
    }

}
