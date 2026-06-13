package cn.iocoder.yudao.module.wms.service.md.item;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.brand.WmsItemBrandSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemBrandDO;
import cn.iocoder.yudao.module.wms.dal.mysql.md.item.WmsItemBrandMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.ITEM_BRAND_NAME_DUPLICATE;

@Import(WmsItemBrandServiceImpl.class)
public class WmsItemBrandServiceImplTest extends BaseDbUnitTest {

    @Resource
    private WmsItemBrandServiceImpl brandService;

    @Resource
    private WmsItemBrandMapper brandMapper;

    @MockitoBean
    private WmsItemService itemService;

    @Test
    public void testCreateItemBrand_nameDuplicate() {
        // mock 数据
        brandMapper.insert(createBrand("B001", "华为"));
        WmsItemBrandSaveReqVO reqVO = createBrandSaveReqVO(null, "B002", "华为");

        // 调用，并断言
        assertServiceException(() -> brandService.createItemBrand(reqVO), ITEM_BRAND_NAME_DUPLICATE);
    }

    @Test
    public void testUpdateItemBrand_nameDuplicate() {
        // mock 数据
        brandMapper.insert(createBrand("B001", "华为"));
        WmsItemBrandDO brand = createBrand("B002", "小米");
        brandMapper.insert(brand);
        WmsItemBrandSaveReqVO reqVO = createBrandSaveReqVO(brand.getId(), "B002", "华为");

        // 调用，并断言
        assertServiceException(() -> brandService.updateItemBrand(reqVO), ITEM_BRAND_NAME_DUPLICATE);
    }

    private static WmsItemBrandDO createBrand(String code, String name) {
        return WmsItemBrandDO.builder()
                .code(code)
                .name(name)
                .build();
    }

    private static WmsItemBrandSaveReqVO createBrandSaveReqVO(Long id, String code, String name) {
        WmsItemBrandSaveReqVO reqVO = new WmsItemBrandSaveReqVO();
        reqVO.setId(id);
        reqVO.setCode(code);
        reqVO.setName(name);
        return reqVO;
    }

}
