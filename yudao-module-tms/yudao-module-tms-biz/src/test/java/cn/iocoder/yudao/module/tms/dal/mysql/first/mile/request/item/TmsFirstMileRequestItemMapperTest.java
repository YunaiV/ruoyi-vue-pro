package cn.iocoder.yudao.module.tms.dal.mysql.first.mile.request.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.request.item.vo.TmsFirstMileRequestItemPageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.request.vo.TmsFirstMileRequestPageReqVO;
import cn.iocoder.yudao.module.tms.service.bo.TmsFirstMileRequestItemItemBO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
@Slf4j
class TmsFirstMileRequestItemMapperTest extends BaseDbUnitTest {
    @Resource
    private TmsFirstMileRequestItemMapper tmsFirstMileRequestItemMapper;

    @Test
    void selectPageBO() {
        TmsFirstMileRequestPageReqVO vo = new TmsFirstMileRequestPageReqVO();
        vo.setItem(new TmsFirstMileRequestItemPageReqVO());
        PageResult<TmsFirstMileRequestItemItemBO> result = tmsFirstMileRequestItemMapper.selectPageBO(vo);
        for (TmsFirstMileRequestItemItemBO tmsFirstMileRequestItemBO : result.getList()) {
            log.info("{}", tmsFirstMileRequestItemBO);
        }
    }
}