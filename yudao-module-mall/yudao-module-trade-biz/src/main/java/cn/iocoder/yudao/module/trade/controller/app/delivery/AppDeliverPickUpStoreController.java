package cn.iocoder.yudao.module.trade.controller.app.delivery;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.trade.controller.app.delivery.vo.pickup.AppDeliveryPickUpStoreRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 自提门店")
@RestController
@RequestMapping("/trade/delivery/pick-up-store")
@Validated
public class AppDeliverPickUpStoreController {

    @GetMapping("/list")
    @Operation(summary = "获得自提门店列表")
    public CommonResult<List<AppDeliveryPickUpStoreRespVO>> getDeliveryPickUpStoreList(
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude) {
        List<AppDeliveryPickUpStoreRespVO> list = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            AppDeliveryPickUpStoreRespVO store = new AppDeliveryPickUpStoreRespVO();
            store.setId(random.nextLong());
            store.setName(RandomUtil.randomString(10));
            store.setLogo("https://www.iocoder.cn/" + (i + 1) + ".png");
            store.setPhone("15601691300");
            store.setAreaId(random.nextInt(100000));
            store.setAreaName("上海-" + RandomUtil.randomString(10));
            store.setDetailAddress("普陀区-" + RandomUtil.randomString(10));
            store.setLatitude(random.nextDouble() * 10);
            store.setLongitude(random.nextDouble() * 10);
            store.setDistance(random.nextInt(1000));

            list.add(store);
        }

        return success(list);
    }

    @GetMapping("/get")
    @Operation(summary = "获得自提门店")
    @Parameter(name = "id", description = "门店编号")
    public CommonResult<AppDeliveryPickUpStoreRespVO> getOrder(@RequestParam("id") Long id) {
        AppDeliveryPickUpStoreRespVO store = new AppDeliveryPickUpStoreRespVO();
        Random random = new Random();
        store.setId(random.nextLong());
        store.setName(RandomUtil.randomString(10));
        store.setLogo("https://www.iocoder.cn/" + (1) + ".png");
        store.setPhone("15601691300");
        store.setAreaId(random.nextInt(100000));
        store.setAreaName("上海-" + RandomUtil.randomString(10));
        store.setDetailAddress("普陀区-" + RandomUtil.randomString(10));
        store.setLatitude(random.nextDouble() * 10);
        store.setLongitude(random.nextDouble() * 10);
        store.setDistance(random.nextInt(1000));
        return success(store);
    }

}
