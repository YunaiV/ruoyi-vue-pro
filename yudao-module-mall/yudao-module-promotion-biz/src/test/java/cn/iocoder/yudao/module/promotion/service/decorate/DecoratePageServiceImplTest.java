package cn.iocoder.yudao.module.promotion.service.decorate;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.promotion.api.decorate.dto.CommonStyle;
import cn.iocoder.yudao.module.promotion.api.decorate.dto.PageComponentDTO;
import cn.iocoder.yudao.module.promotion.api.decorate.dto.RollingBannerComponent;
import cn.iocoder.yudao.module.promotion.controller.admin.decorate.vo.DecoratePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.decorate.vo.DecoratePageRespVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.decorate.PageDecorateDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.decorate.PageDecorateMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.enums.CommonStatusEnum.ENABLE;
import static cn.iocoder.yudao.module.promotion.enums.decorate.DecoratePageTypeEnum.INDEX;
import static cn.iocoder.yudao.module.promotion.enums.decorate.PageComponentEnum.ROLLING_BANNER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;

/**
 * @author jason
 */
public class DecoratePageServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private DecoratePageServiceImpl decoratePageService;

    @Mock
    private PageDecorateMapper pageDecorateMapper;

    private PageComponentDTO<RollingBannerComponent.Config,
            CommonStyle,
            List<RollingBannerComponent.DataStructure>> bannerComponent;
    @BeforeEach
    public void init(){
        CommonStyle commonStyle = new CommonStyle();
        RollingBannerComponent.DataStructure banner1 = new RollingBannerComponent.DataStructure().setImg("http://127.0.0.1:8084/a.jpg")
                .setPath("/pages/coupon_center/coupon_center")
                .setStatus(ENABLE.getStatus());
        List<RollingBannerComponent.DataStructure> banners = new ArrayList<>(1);
        banners.add(banner1);
        bannerComponent
                = new PageComponentDTO<RollingBannerComponent.Config, CommonStyle, List<RollingBannerComponent.DataStructure>>().setTitle("首页横幅广告")
                .setConfig(new RollingBannerComponent.Config().setEnabled(Boolean.TRUE))
                .setStyle(commonStyle)
                .setData(banners);
    }
    @Test
    void testReq() {
        // 准备请求参数
        DecoratePageReqVO.ComponentReqVO cReq = new DecoratePageReqVO.ComponentReqVO()
                .setComponentCode(ROLLING_BANNER.getCode())
                .setValue(JsonUtils.toJsonString(bannerComponent));
        List<DecoratePageReqVO.ComponentReqVO> cReqList = new ArrayList<>();
        cReqList.add(cReq);
        DecoratePageReqVO reqVO = new DecoratePageReqVO();
        reqVO.setType(1);
        reqVO.setComponents(cReqList);
        System.out.printf("请求数据:%s%n",JsonUtils.toJsonPrettyString(reqVO));
        List<PageDecorateDO> list = decoratePageService.testReq(reqVO);
        assertThat(list).hasSize(1);
    }
    @Test
    void testResp(){
        List<PageDecorateDO> list = new ArrayList<>(1);
        PageDecorateDO decorateDO = new PageDecorateDO()
                .setType(INDEX.getType()).setComponentValue(JsonUtils.toJsonString(bannerComponent))
                .setComponentCode(ROLLING_BANNER.getCode()).setId(1L);
        list.add(decorateDO);
        //mock 方法
        Mockito.when(pageDecorateMapper.selectByPageType(eq(1))).thenReturn(list);

        DecoratePageRespVO respVO = decoratePageService.testResp(1);
        System.out.printf("响应数据:%s%n",JsonUtils.toJsonPrettyString(respVO));
    }
}