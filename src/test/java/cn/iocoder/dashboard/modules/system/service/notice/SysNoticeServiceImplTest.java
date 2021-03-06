package cn.iocoder.dashboard.modules.system.service.notice;

import cn.iocoder.dashboard.BaseSpringBootUnitTest;
import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.modules.system.controller.notice.vo.SysNoticeCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.notice.vo.SysNoticeUpdateReqVO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.notice.SysNoticeDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.notice.SysNoticeMapper;
import cn.iocoder.dashboard.modules.system.enums.notice.SysNoticeTypeEnum;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;

import java.util.function.Consumer;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.dashboard.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.dashboard.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


class SysNoticeServiceImplTest extends BaseSpringBootUnitTest {

    @Resource
    private SysNoticeService sysNoticeService;

    @Resource
    private SysNoticeMapper sysNoticeMapper;

    @Test
    void testPageNotices_success() {
        // todo: 待更新
    }

    @Test
    void testGetNotice_success() {
        // 插入前置数据
        int noticeId = sysNoticeMapper.insert(randomSysNoticeDO());

        // 查询
        assertNotNull(sysNoticeService.getNotice(new Long(noticeId)));
    }

    @Test
    void testCreateNotice_success() {
        // 准备参数
        SysNoticeCreateReqVO reqVO = randomSysNoticeCreateReqVO();

        // 校验插入是否成功
        Long noticeId = sysNoticeService.createNotice(reqVO);
        assertNotNull(noticeId);

        // 校验插入属性是否正确
        SysNoticeDO notice = sysNoticeMapper.selectById(noticeId);
        assertPojoEquals(reqVO, notice);
    }

    @Test
    void testUpdateNotice_success() {
        // 插入前置数据
        int noticeId = sysNoticeMapper.insert(randomSysNoticeDO());

        // 准备更新参数
        SysNoticeUpdateReqVO reqVO = randomSysNoticeUpdateReqVO(o -> o.setId(new Long(noticeId)));

        // 更新
        sysNoticeService.updateNotice(reqVO);

        // 检验是否更新成功
        SysNoticeDO notice = sysNoticeMapper.selectById(noticeId);
        assertPojoEquals(reqVO, notice);
    }

    @Test
    void testDeleteNotice_success() {
        // 插入前置数据
        int noticeId = sysNoticeMapper.insert(randomSysNoticeDO());

        // 删除
        sysNoticeService.deleteNotice(new Long(noticeId));

        // 检查是否删除成功
        assertNull(sysNoticeMapper.selectById(noticeId));
    }

    @SafeVarargs
    private static SysNoticeDO randomSysNoticeDO(Consumer<SysNoticeDO>... consumers) {
        SysNoticeDO notice = randomPojo(SysNoticeDO.class, consumers);
        notice.setType(randomEle(SysNoticeTypeEnum.values()).getType());
        notice.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return notice;
    }

    @SafeVarargs
    private static SysNoticeUpdateReqVO randomSysNoticeUpdateReqVO(Consumer<SysNoticeUpdateReqVO>... consumers) {
        SysNoticeUpdateReqVO reqVO = randomPojo(SysNoticeUpdateReqVO.class, consumers);
        reqVO.setType(randomEle(SysNoticeTypeEnum.values()).getType());
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return reqVO;
    }

    private static SysNoticeCreateReqVO randomSysNoticeCreateReqVO() {
        SysNoticeCreateReqVO reqVO = randomPojo(SysNoticeCreateReqVO.class);
        reqVO.setType(randomEle(SysNoticeTypeEnum.values()).getType());
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return reqVO;
    }


}