package com.somle.esb.converter;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptListReqVO;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptSaveReqVO;
import cn.iocoder.yudao.module.system.controller.admin.user.vo.user.UserSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.service.dept.DeptService;
import com.dingtalk.api.response.OapiV2UserGetResponse;
import com.somle.dingtalk.model.DingTalkDepartment;
import com.somle.dingtalk.service.DingTalkService;
import com.somle.erp.model.ErpDepartment;
import com.somle.erp.service.ErpService;
import com.somle.esb.model.EsbMapping;
import com.somle.esb.repository.EsbMappingRepository;
import com.somle.esb.service.EsbMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class DingTalkToErpConverter {
    @Autowired
    DingTalkService dingTalkService;


    @Autowired
    DeptService deptService;

    @Autowired
    EsbMappingService mappingService;


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

    public DeptSaveReqVO toErp(DingTalkDepartment dept) {
        DeptSaveReqVO erpDept = new DeptSaveReqVO();
        // try to translate parent id
        try {
            var mapping = mappingService.toMapping(dept);
            mapping.setExternalId(dept.getParentId().toString());
            mapping = mappingService.findMapping(mapping);
            erpDept
                .setParentId(mapping.getInternalId());
        } catch (Exception e) {
            throw new RuntimeException("parent mapping not found");
        }
        //try to translate id
        try {
            var mapping = mappingService.toMapping(dept);
            mapping = mappingService.findMapping(mapping);
            erpDept
                .setId(mapping.getInternalId());
        } catch (Exception e) {
            log.debug("mapping not found");
        }
        //translate the rest
        erpDept
            .setName(dept.getName())
            .setSort(1024)
            .setStatus(CommonStatusEnum.ENABLE.getStatus());
        return erpDept;
    }

    public UserSaveReqVO toErp(OapiV2UserGetResponse.UserGetResponse user) {
        UserSaveReqVO erpUser = new UserSaveReqVO();
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
                .setExternalId(user.getDeptIdList().getFirst().toString());
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
}
