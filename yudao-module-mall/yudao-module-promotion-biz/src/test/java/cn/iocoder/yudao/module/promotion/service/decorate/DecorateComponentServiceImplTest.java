package cn.iocoder.yudao.module.promotion.service.decorate;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.promotion.dal.dataobject.decorate.DecorateComponentDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.decorate.DecorateComponentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.module.promotion.enums.decorate.DecorateComponentEnum.ROLLING_BANNER;
import static cn.iocoder.yudao.module.promotion.enums.decorate.DecoratePageTypeEnum.INDEX;
import static org.mockito.ArgumentMatchers.eq;

/**
 * @author jason
 */
public class DecorateComponentServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private DecorateComponentServiceImpl decoratePageService;

    @Mock
    private DecorateComponentMapper decorateComponentMapper;

    @BeforeEach
    public void init(){

    }

    @Test
    void testResp(){
        List<DecorateComponentDO> list = new ArrayList<>(1);
        DecorateComponentDO decorateDO = new DecorateComponentDO()
                .setType(INDEX.getType()).setComponentValue("")
                .setComponentCode(ROLLING_BANNER.getCode()).setId(1L);
        list.add(decorateDO);
        //mock 方法
        Mockito.when(decorateComponentMapper.selectByPageType(eq(1))).thenReturn(list);
    }
}