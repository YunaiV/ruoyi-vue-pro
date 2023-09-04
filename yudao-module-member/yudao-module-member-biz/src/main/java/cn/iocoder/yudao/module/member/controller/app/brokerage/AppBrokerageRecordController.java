package cn.iocoder.yudao.module.member.controller.app.brokerage;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.module.member.controller.app.brokerage.vo.record.AppBrokerageRecordPageReqVO;
import cn.iocoder.yudao.module.member.controller.app.brokerage.vo.record.AppBrokerageRecordRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static java.util.Arrays.asList;

@Tag(name = "用户 APP - 分销用户")
@RestController
@RequestMapping("/member/brokerage-record")
@Validated
@Slf4j
public class AppBrokerageRecordController {

    // TODO 芋艿：临时 mock =>
    @GetMapping("/page")
    @Operation(summary = "获得分销记录分页")
    @PreAuthenticated
    public CommonResult<PageResult<AppBrokerageRecordRespVO>> getBrokerageRecordPage(AppBrokerageRecordPageReqVO pageReqVO) {
        AppBrokerageRecordRespVO vo1 = new AppBrokerageRecordRespVO()
                .setId(1L).setPrice(10).setTitle("收到钱").setCreateTime(LocalDateTime.now())
                .setFinishTime(LocalDateTime.now());
        AppBrokerageRecordRespVO vo2 = new AppBrokerageRecordRespVO()
                .setId(2L).setPrice(-20).setTitle("提现钱").setCreateTime(LocalDateTime.now())
                .setFinishTime(LocalDateTime.now());
        return success(new PageResult<>(asList(vo1, vo2), 10L));
    }

}
