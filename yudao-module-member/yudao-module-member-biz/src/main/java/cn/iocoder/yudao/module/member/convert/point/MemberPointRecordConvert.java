package cn.iocoder.yudao.module.member.convert.point;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.member.controller.admin.point.vo.recrod.MemberPointRecordRespVO;
import cn.iocoder.yudao.module.member.dal.dataobject.point.MemberPointRecordDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户积分记录 Convert
 *
 * @author QingX
 */
@Mapper
public interface MemberPointRecordConvert {

    MemberPointRecordConvert INSTANCE = Mappers.getMapper(MemberPointRecordConvert.class);

    default PageResult <MemberPointRecordRespVO> convertPage(PageResult <MemberPointRecordDO> page, List <MemberUserRespDTO> users) {
        if (page == null) {
            return null;
        }

        PageResult <MemberPointRecordRespVO> pageResult = new PageResult <MemberPointRecordRespVO>();

        pageResult.setList(memberPointRecordDOListToMemberPointRecordRespVOList(page.getList(), users));
        pageResult.setTotal(page.getTotal());

        return pageResult;
    }

    default List <MemberPointRecordRespVO> memberPointRecordDOListToMemberPointRecordRespVOList(List <MemberPointRecordDO> list, List <MemberUserRespDTO> users) {
        if (list == null) {
            return null;
        }
        Map <Long, String> maps = null;
        //是否需要转换用户标志
        boolean userNickFlag = !CollectionUtils.isEmpty(users);
        if (userNickFlag) {
            maps = users.stream()
                    .collect(Collectors.toMap(MemberUserRespDTO::getId, MemberUserRespDTO::getNickname, (key1, key2) -> key2));
        }
        List <MemberPointRecordRespVO> list1 = new ArrayList <MemberPointRecordRespVO>(list.size());
        for (MemberPointRecordDO memberPointRecordDO : list) {
            MemberPointRecordRespVO recordRespVO = memberPointRecordDOToMemberPointRecordRespVO(memberPointRecordDO);
            recordRespVO.setNickName(maps.get(memberPointRecordDO.getUserId()));
            list1.add(recordRespVO);
        }

        return list1;
    }

    default MemberPointRecordRespVO memberPointRecordDOToMemberPointRecordRespVO(MemberPointRecordDO memberPointRecordDO) {
        if (memberPointRecordDO == null) {
            return null;
        }

        MemberPointRecordRespVO memberPointRecordRespVO = new MemberPointRecordRespVO();

        memberPointRecordRespVO.setBizId(memberPointRecordDO.getBizId());
        memberPointRecordRespVO.setBizType(memberPointRecordDO.getBizType());
        memberPointRecordRespVO.setType(memberPointRecordDO.getType());
        memberPointRecordRespVO.setTitle(memberPointRecordDO.getTitle());
        memberPointRecordRespVO.setDescription(memberPointRecordDO.getDescription());
        memberPointRecordRespVO.setPoint(memberPointRecordDO.getPoint());
        memberPointRecordRespVO.setTotalPoint(memberPointRecordDO.getTotalPoint());
        memberPointRecordRespVO.setStatus(memberPointRecordDO.getStatus());
        memberPointRecordRespVO.setFreezingTime(memberPointRecordDO.getFreezingTime());
        memberPointRecordRespVO.setThawingTime(memberPointRecordDO.getThawingTime());
        memberPointRecordRespVO.setId(memberPointRecordDO.getId());
        memberPointRecordRespVO.setCreateTime(memberPointRecordDO.getCreateTime());

        return memberPointRecordRespVO;
    }


}
