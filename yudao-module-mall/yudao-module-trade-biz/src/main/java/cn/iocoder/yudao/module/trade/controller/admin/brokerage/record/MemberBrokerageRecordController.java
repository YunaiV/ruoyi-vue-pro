package cn.iocoder.yudao.module.trade.controller.admin.brokerage.record;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.record.vo.MemberBrokerageRecordPageReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.record.vo.MemberBrokerageRecordRespVO;
import cn.iocoder.yudao.module.trade.convert.brokerage.record.MemberBrokerageRecordConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.record.MemberBrokerageRecordDO;
import cn.iocoder.yudao.module.trade.service.brokerage.record.MemberBrokerageRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 佣金记录")
@RestController
@RequestMapping("/trade/member-brokerage-record")
@Validated
public class MemberBrokerageRecordController {

    @Resource
    private MemberBrokerageRecordService memberBrokerageRecordService;

    @GetMapping("/get")
    @Operation(summary = "获得佣金记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('member:member-brokerage-record:query')")
    public CommonResult<MemberBrokerageRecordRespVO> getMemberBrokerageRecord(@RequestParam("id") Integer id) {
        MemberBrokerageRecordDO memberBrokerageRecord = memberBrokerageRecordService.getMemberBrokerageRecord(id);
        return success(MemberBrokerageRecordConvert.INSTANCE.convert(memberBrokerageRecord));
    }

    @GetMapping("/page")
    @Operation(summary = "获得佣金记录分页")
    @PreAuthorize("@ss.hasPermission('member:member-brokerage-record:query')")
    public CommonResult<PageResult<MemberBrokerageRecordRespVO>> getMemberBrokerageRecordPage(@Valid MemberBrokerageRecordPageReqVO pageVO) {
        PageResult<MemberBrokerageRecordDO> pageResult = memberBrokerageRecordService.getMemberBrokerageRecordPage(pageVO);
        return success(MemberBrokerageRecordConvert.INSTANCE.convertPage(pageResult));
    }

}
