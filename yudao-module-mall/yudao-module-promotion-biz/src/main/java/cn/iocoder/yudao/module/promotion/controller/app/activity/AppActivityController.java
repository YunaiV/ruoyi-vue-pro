package cn.iocoder.yudao.module.promotion.controller.app.activity;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.promotion.controller.app.activity.vo.AppActivityRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 营销活动") // 用于提供跨多个活动的 HTTP 接口
@RestController
@RequestMapping("/promotion/activity")
@Validated
public class AppActivityController {

    @GetMapping("/list-by-spu-id")
    @Operation(summary = "获得单个商品，近期参与的每个活动") // 每种活动，只返回一个
    @Parameter(name = "spuId", description = "商品编号", required = true)
    public CommonResult<List<AppActivityRespVO>> getActivityListBySpuId(@RequestParam("spuId") Long spuId) {
        // TODO 芋艿，实现
        List<AppActivityRespVO> randomList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 3; i++) { // 生成5个随机对象
            AppActivityRespVO vo = new AppActivityRespVO();
            vo.setId(random.nextLong()); // 随机生成一个长整型 ID
            vo.setType(i + 1); // 随机生成一个介于0到2之间的整数，对应枚举类型的三种类型之一
            vo.setName(String.format("活动%d", random.nextInt(100))); // 随机生成一个类似于“活动XX”的活动名称，XX为0到99之间的随机整数
            vo.setStartTime(LocalDateTime.now()); // 随机生成一个在过去的一年内的开始时间（以毫秒为单位）
            vo.setEndTime(LocalDateTime.now()); // 随机生成一个在未来的一年内的结束时间（以毫秒为单位）
            randomList.add(vo);
        }
        return success(randomList);
    }

    @GetMapping("/list-by-spu-ids")
    @Operation(summary = "获得多个商品，近期参与的每个活动") // 每种活动，只返回一个；key 为 SPU 编号
    @Parameter(name = "spuIds", description = "商品编号数组", required = true)
    public CommonResult<Map<Long, List<AppActivityRespVO>>> getActivityListBySpuIds(@RequestParam("spuIds") List<Long> spuIds) {
        // TODO 芋艿，实现
        List<AppActivityRespVO> randomList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 5; i++) { // 生成5个随机对象
            AppActivityRespVO vo = new AppActivityRespVO();
            vo.setId(random.nextLong()); // 随机生成一个长整型 ID
            vo.setType(random.nextInt(3)); // 随机生成一个介于0到2之间的整数，对应枚举类型的三种类型之一
            vo.setName(String.format("活动%d", random.nextInt(100))); // 随机生成一个类似于“活动XX”的活动名称，XX为0到99之间的随机整数
            vo.setStartTime(LocalDateTime.now()); // 随机生成一个在过去的一年内的开始时间（以毫秒为单位）
            vo.setEndTime(LocalDateTime.now()); // 随机生成一个在未来的一年内的结束时间（以毫秒为单位）
            randomList.add(vo);
        }
        Map<Long, List<AppActivityRespVO>> map = new HashMap<>();
        map.put(109L, randomList);
        map.put(2L, randomList);
        return success(map);
    }

}
