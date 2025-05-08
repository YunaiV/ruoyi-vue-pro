package com.somle.esb.converter;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptReqDTO;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptSaveReqDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserReqDTO;
import com.dingtalk.api.response.OapiV2UserGetResponse;
import com.somle.dingtalk.model.DingTalkDepartment;
import com.somle.dingtalk.service.DingTalkService;
import com.somle.esb.model.EsbMapping;
import com.somle.esb.service.EsbMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class DingTalkToErpConverter {
    @Autowired
    DingTalkService dingTalkService;


    @Autowired
    private DeptApi deptApi;

    @Autowired
    private AdminUserApi adminUserApi;

    @Autowired
    EsbMappingService mappingService;
    private static final String CHINESE_CHAR_PATTERN = "[\\u4E00-\\u9FA5]+";
    private static final String ENGLISH_CHAR_PATTERN = "^[a-zA-Z\\s]+$";


//    public ErpDepartment toErp(DingTalkDepartment dept) {
//        ErpDepartment erpDepartment = ErpDepartment.builder()
//            .id(dept.getDeptId())
//            .nameZh(dept.getName())
//            .level(dept.getLevel())
//            .parentId(dept.getParentId())
//            .build();
//        return erpDepartment;
//    }

//    public Stream<ErpDepartment> toErpStream(ErpDepartment erpParent) {
//        List<DingTalkDepartment> childList = dingTalkService.getOrphans(Long.valueOf(erpParent.getId())).toList();
//        if ( childList.isEmpty()) {
//            log.debug("null child list");
//            return Stream.of();
//        } else {
//            log.debug("non null child list");
//            log.debug(childList.toString());
//            List<ErpDepartment> erpChildList = childList.stream().map(child->{
//                ErpDepartment erpChild = toErp(child);
//                erpChild.setParent(erpParent);
//                erpChild.setLevel((erpParent.getLevel() + 1));
//                return erpChild;
//            }).toList();
//            return Stream.concat(erpChildList.stream(), erpChildList.stream().flatMap(n -> toErpStream(n)));
//            // return Stream.concat(childList.stream().filter(n->! keys.stream().anyMatch(key->n.getName().startsWith(key))), childList.stream().flatMap(n -> getDepartmentsResursive(n)));
//        }
//    }
//
//    public Stream<ErpDepartment> getErpDepartmentStream() {
//        return toErpStream(toErp(
//            DingTalkDepartment.builder()
//                .deptId(1l)
//                .level(0)
//                .build()
//        ));
//    }

    public DeptSaveReqDTO toErp(DingTalkDepartment dept) {
        DeptSaveReqDTO erpDept = new DeptSaveReqDTO();
        // translate parent id
        if (dept.getDeptId() == 1L) {
            erpDept.setParentId(0L);
        } else {
            try {
                var result = deptApi.getDeptByExternalId(dept.getParentId().toString());
                erpDept.setParentId(result.getId());
            } catch (Exception e) {
                throw new RuntimeException("external id not found: " + dept.getParentId());
            }
        }
        // translate id
        try {
            var result = deptApi.getDeptByExternalId(dept.getDeptId().toString());
            erpDept.setId(result.getId());
        } catch (Exception e) {
            log.debug("external id not found: " + dept.getDeptId());
        }
        //translate the rest
        erpDept
            .setName(dept.getName())
            .setSort(1024)
            .setStatus(CommonStatusEnum.ENABLE.getStatus());
        return erpDept;
    }

    public AdminUserReqDTO toErp(OapiV2UserGetResponse.UserGetResponse user) {
        AdminUserReqDTO erpUser = new AdminUserReqDTO();
        //try to translate id
        try {
            var mapping = mappingService.toMapping(user);
            mapping = mappingService.findMapping(mapping);
            erpUser
                .setId(mapping.getInternalId());
        } catch (Exception e) {
            log.debug("user mapping not found");
        }
        //try to translate dept id
        try {
            var mapping = new EsbMapping();
            mapping
                .setType("department")
                .setDomain("dingtalk")
                .setExternalId(user.getDeptIdList().get(0).toString());
            mapping = mappingService.findMapping(mapping);
            erpUser
                .setDeptId(mapping.getInternalId());
        } catch (Exception e) {
            log.debug("dept mapping not found");
        }
        //translate the rest
        erpUser
            .setUsername(null)
            .setMobile(user.getMobile())
            .setEmail(user.getEmail())
            .setNickname(user.getName())
            .setPassword("123456")
            .setAvatar(user.getAvatar());
        return erpUser;
    }
    /**
     * @Author Wqh
     * @Description 自动生成用户名
     * 中文名直接转为拼音
     * 英文名出去空格变为小写
     * 非法字符直接抛出异常（数字，中英结合，符号等）
     * @Date 13:09 2024/12/9
     * @Param [name]
     * @return java.lang.String
     **/
    public String generateUserName(String nickname) {
        String initUsername;
        //判断是否都为中文字符
        if(nickname.matches(CHINESE_CHAR_PATTERN)){
            //将昵称转化为拼音获取username
            initUsername = StrUtils.getPinyin(nickname).replaceAll("\\s+", "");
        }else if (nickname.matches(ENGLISH_CHAR_PATTERN)){
            //英文字符一律去掉空格，一律变小写
            initUsername = nickname.toLowerCase().replaceAll("\\s+", "");
        }else {
            //存在特殊字符则抛出异常
            throw new RuntimeException("昵称\""+nickname+"\"不规范，请联系管理员");
        }
        //获取以相同用户名开头的用户数量
        Integer usernameIndex = adminUserApi.getUsernameIndex(nickname);
        //自动生成用户账户
        return usernameIndex == 0 ? initUsername : initUsername + "." + usernameIndex;
    }
}
