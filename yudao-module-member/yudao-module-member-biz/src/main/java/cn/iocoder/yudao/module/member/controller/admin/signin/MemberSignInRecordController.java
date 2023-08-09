package cn.iocoder.yudao.module.member.controller.admin.signin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.MemberSignInRecordPageReqVO;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.MemberSignInRecordRespVO;
import cn.iocoder.yudao.module.member.convert.signin.MemberSignInRecordConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.signin.MemberSignInRecordDO;
import cn.iocoder.yudao.module.member.service.signin.MemberSignInRecordService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@Tag(name = "管理后台 - 用户签到积分")
@RestController
@RequestMapping("/point/sign-in-record")
@Validated
public class MemberSignInRecordController {

    @Resource
    private MemberSignInRecordService memberSignInRecordService;
    @Resource
    MemberUserApi memberUserApi;


    @GetMapping("/page")
    @Operation(summary = "获得用户签到积分分页")
    @PreAuthorize("@ss.hasPermission('point:sign-in-record:query')")
    public CommonResult <PageResult <MemberSignInRecordRespVO>> getSignInRecordPage(@Valid MemberSignInRecordPageReqVO pageVO) {
        //请求中如果有nickName，需要根据nickName查询出userId
        if(StringUtils.isNotBlank(pageVO.getNickName())){
            List<Long> users= memberUserApi.getUserListByNickname(pageVO.getNickName()).stream()
                    .map(user->user.getId()).collect(Collectors.toList());
            //如果根据昵称查出来的用户为空，则无需继续查找
            if(CollectionUtils.isEmpty(users)){
                return success(new PageResult <>());
            }
            pageVO.setUserIds(users);
        }
        //处理查询结果首先将数据信息查询，然后将userId转换为nickName
        PageResult <MemberSignInRecordDO> pageResult = memberSignInRecordService.getSignInRecordPage(pageVO);
        List <MemberSignInRecordDO> result = pageResult.getList();
        //设置获取的用户信息
        List <MemberUserRespDTO> users = null;
        if (!CollectionUtils.isEmpty(result)) {
            List <Long> ids = result.stream().map(user -> user.getUserId()).collect(Collectors.toList());
            users = memberUserApi.getUsers(ids);
        }
        return success(MemberSignInRecordConvert.INSTANCE.convertPage(pageResult, users));
    }
}
