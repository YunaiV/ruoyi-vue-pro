package cn.iocoder.yudao.module.hrm.service.recruit.config;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.config.HrmRecruitEliminateReasonSaveReqVO;
import cn.iocoder.yudao.module.hrm.service.config.HrmConfigService;
import cn.iocoder.yudao.module.hrm.enums.config.HrmConfigTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link HrmRecruitConfigServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmRecruitConfigServiceImpl.class)
public class HrmRecruitConfigServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmRecruitConfigServiceImpl recruitConfigService;
    @MockitoBean
    private HrmConfigService configService;

    @Test
    public void testSaveRecruitEliminateReason_success() {
        // 准备参数
        HrmRecruitEliminateReasonSaveReqVO saveReqVO = new HrmRecruitEliminateReasonSaveReqVO();
        saveReqVO.setReasons(Arrays.asList("简历不匹配", "薪资不匹配", "简历不匹配", "  面试未通过  "));

        // 调用
        recruitConfigService.saveRecruitEliminateReason(saveReqVO);

        // 断言
        List<String> reasons = Arrays.asList("简历不匹配", "薪资不匹配", "面试未通过");
        verify(configService).replaceConfigValueList(HrmConfigTypeEnum.RECRUIT_ELIMINATE.getType(), reasons);

        // mock 方法
        when(configService.getConfigValueList(HrmConfigTypeEnum.RECRUIT_ELIMINATE.getType())).thenReturn(reasons);

        // 调用，并断言
        assertEquals(reasons, recruitConfigService.getRecruitEliminateReasonList());
    }

}
