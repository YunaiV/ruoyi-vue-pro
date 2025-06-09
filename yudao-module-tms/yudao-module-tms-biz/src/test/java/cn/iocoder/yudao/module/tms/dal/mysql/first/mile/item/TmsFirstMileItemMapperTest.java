package cn.iocoder.yudao.module.tms.dal.mysql.first.mile.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.item.vo.TmsFirstMileItemPageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.req.TmsFirstMilePageReqVO;
import cn.iocoder.yudao.module.tms.service.bo.TmsFirstMileItemBO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
@Slf4j
class TmsFirstMileItemMapperTest extends BaseDbUnitTest {

    @Resource
    private TmsFirstMileItemMapper tmsFirstMileItemMapper;

    @Test
    void selectPageBO() {
        TmsFirstMilePageReqVO vo = new TmsFirstMilePageReqVO();
        vo.setItemPageReqVO(new TmsFirstMileItemPageReqVO());
        PageResult<TmsFirstMileItemBO> pageBO = tmsFirstMileItemMapper.selectPageBO(vo);
        log.info("pageBO: {}", pageBO);
    }
}