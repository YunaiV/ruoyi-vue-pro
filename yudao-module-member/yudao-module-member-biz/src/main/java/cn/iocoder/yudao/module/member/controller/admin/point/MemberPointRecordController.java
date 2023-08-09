package cn.iocoder.yudao.module.member.controller.admin.point;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.member.controller.admin.point.vo.recrod.MemberPointRecordPageReqVO;
import cn.iocoder.yudao.module.member.controller.admin.point.vo.recrod.MemberPointRecordRespVO;
import cn.iocoder.yudao.module.member.convert.point.MemberPointRecordConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.point.MemberPointRecordDO;
import cn.iocoder.yudao.module.member.dal.dataobject.signin.MemberSignInRecordDO;
import cn.iocoder.yudao.module.member.service.point.MemberPointRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 用户积分记录")
@RestController
@RequestMapping("/point/record")
@Validated
public class MemberPointRecordController {

    @Resource
    private MemberPointRecordService recordService;
    @Resource
    MemberUserApi memberUserApi;

    @GetMapping("/page")
    @Operation(summary = "获得用户积分记录分页")
    @PreAuthorize("@ss.hasPermission('point:record:query')")
    public CommonResult <PageResult <MemberPointRecordRespVO>> getRecordPage(@Valid MemberPointRecordPageReqVO pageVO) {
        //根据用户昵称查询出用户ids
        List <Long> userIds = null;
        if (StringUtils.isNotBlank(pageVO.getNickName())) {
            List <MemberUserRespDTO> users = memberUserApi.getUserListByNickname(pageVO.getNickName());
            //如果查询用户结果为空直接返回无需继续查询
            if (CollectionUtils.isEmpty(users)) {
                return success(new PageResult <>());
            }
            userIds=users.stream().map(user->user.getId()).collect(Collectors.toList());
            pageVO.setUserIds(userIds);
        }

        PageResult <MemberPointRecordDO> pageResult = recordService.getRecordPage(pageVO);
        //根据查询的userId转换成nickName
        List <MemberPointRecordDO> result = pageResult.getList();
        List <MemberUserRespDTO> users = null;
        if (!CollectionUtils.isEmpty(result)) {
            List <Long> ids = result.stream().map(user -> user.getUserId()).collect(Collectors.toList());
            users = memberUserApi.getUsers(ids);
        }
        return success(MemberPointRecordConvert.INSTANCE.convertPage(pageResult,users));
    }

}
