package cn.iocoder.yudao.module.member.convert.signin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.MemberSignInRecordRespVO;
import cn.iocoder.yudao.module.member.dal.dataobject.signin.MemberSignInRecordDO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户签到积分 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface MemberSignInRecordConvert {

    MemberSignInRecordConvert INSTANCE = Mappers.getMapper(MemberSignInRecordConvert.class);

    default public MemberSignInRecordRespVO convert(MemberSignInRecordDO bean) {
        if (bean == null) {
            return null;
        }

        MemberSignInRecordRespVO memberSignInRecordRespVO = new MemberSignInRecordRespVO();

        memberSignInRecordRespVO.setUserId(bean.getUserId());
        memberSignInRecordRespVO.setDay(bean.getDay());
        memberSignInRecordRespVO.setPoint(bean.getPoint());
        memberSignInRecordRespVO.setId(bean.getId());
        memberSignInRecordRespVO.setCreateTime(bean.getCreateTime());

        return memberSignInRecordRespVO;
    }

    default PageResult <MemberSignInRecordRespVO> convertPage(PageResult <MemberSignInRecordDO> page, List <MemberUserRespDTO> userRespDTOS) {
        if (page == null) {
            return null;
        }

        PageResult <MemberSignInRecordRespVO> pageResult = new PageResult <MemberSignInRecordRespVO>();

        pageResult.setList(memberSignInRecordDOListToMemberSignInRecordRespVOList(page.getList(),userRespDTOS));
        pageResult.setTotal(page.getTotal());

        return pageResult;
    }

    default List <MemberSignInRecordRespVO> memberSignInRecordDOListToMemberSignInRecordRespVOList(List <MemberSignInRecordDO> list,List <MemberUserRespDTO> userRespDTOS) {
        if (list == null) {
            return null;
        }
        Map <Long, String> maps=null;
        //是否需要转换用户标志
        boolean userNickFlag = !CollectionUtils.isEmpty(userRespDTOS);
        if(userNickFlag){
            maps = userRespDTOS.stream()
                    .collect(Collectors.toMap(MemberUserRespDTO::getId, MemberUserRespDTO::getNickname, (key1, key2) -> key2));
        }
        List <MemberSignInRecordRespVO> list1 = new ArrayList <MemberSignInRecordRespVO>(list.size());
        for (MemberSignInRecordDO memberSignInRecordDO : list) {
            MemberSignInRecordRespVO recordRespVO = convert(memberSignInRecordDO);
            if(userNickFlag){
                recordRespVO.setNickName(maps.get(recordRespVO.getUserId()));
            }
            list1.add(recordRespVO);
        }

        return list1;
    }


}
