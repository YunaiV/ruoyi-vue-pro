package cn.iocoder.yudao.module.member.service.point;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.point.vo.recrod.MemberPointRecordPageReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.point.MemberPointRecordDO;
import cn.iocoder.yudao.module.member.dal.mysql.point.MemberPointRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;


/**
 * 用户积分记录 Service 实现类
 *
 * @author QingX
 */
@Service
@Validated
public class MemberPointRecordServiceImpl implements MemberPointRecordService {

    @Resource
    private MemberPointRecordMapper recordMapper;

    @Override
    public PageResult<MemberPointRecordDO> getRecordPage(MemberPointRecordPageReqVO pageReqVO) {
        return recordMapper.selectPage(pageReqVO);
    }

}
