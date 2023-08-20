package cn.iocoder.yudao.module.promotion.service.decorate;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.promotion.dal.mysql.decorate.DecorateComponentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;

// TODO @芋艿：后续 review 下
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

//    @Test
//    void testResp(){
//        List<DecorateComponentDO> list = new ArrayList<>(1);
//        DecorateComponentDO decorateDO = new DecorateComponentDO()
//                .setPage(INDEX.getPage()).setValue("")
//                .setCode(ROLLING_NEWS.getCode()).setId(1L);
//        list.add(decorateDO);
//        //mock 方法
//        Mockito.when(decorateComponentMapper.selectListByPageAndStatus(eq(1))).thenReturn(list);
//    }
}
