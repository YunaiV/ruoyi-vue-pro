package cn.iocoder.yudao.module.hrm.service.recruit.config;

import cn.iocoder.yudao.module.hrm.service.recruit.candidate.HrmRecruitCandidateService;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.channel.HrmRecruitChannelDeleteReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.channel.HrmRecruitChannelPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.channel.HrmRecruitChannelSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.channel.HrmRecruitChannelStatusReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.config.HrmRecruitChannelDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.recruit.config.HrmRecruitChannelMapper;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * {@link HrmRecruitChannelServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmRecruitChannelServiceImpl.class)
public class HrmRecruitChannelServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmRecruitChannelServiceImpl recruitChannelService;

    @Resource
    private HrmRecruitChannelMapper recruitChannelMapper;

    @MockBean
    private HrmEmployeeService employeeService;
    @MockBean
    private HrmRecruitCandidateService recruitCandidateService;

    @Test
    public void testCreateRecruitChannel_success() {
        // 准备参数
        HrmRecruitChannelSaveReqVO reqVO = randomRecruitChannelSaveReqVO();

        // 调用
        Long channelId = recruitChannelService.createRecruitChannel(reqVO);

        // 断言
        assertNotNull(channelId);
        HrmRecruitChannelDO channel = recruitChannelMapper.selectById(channelId);
        assertPojoEquals(reqVO, channel, "id");
        assertFalse(channel.getSystemFlag());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), channel.getStatus());
    }

    @Test
    public void testUpdateRecruitChannel_success() {
        // mock 数据
        HrmRecruitChannelDO dbChannel = randomRecruitChannelDO(o -> o.setSystemFlag(false));
        recruitChannelMapper.insert(dbChannel);
        // 准备参数
        HrmRecruitChannelSaveReqVO reqVO = randomRecruitChannelSaveReqVO(o -> o.setId(dbChannel.getId()));

        // 调用
        recruitChannelService.updateRecruitChannel(reqVO);

        // 断言
        HrmRecruitChannelDO channel = recruitChannelMapper.selectById(dbChannel.getId());
        assertPojoEquals(reqVO, channel);
        assertFalse(channel.getSystemFlag());
    }

    @Test
    public void testUpdateRecruitChannel_systemNameUpdateForbidden() {
        // mock 数据
        HrmRecruitChannelDO dbChannel = randomRecruitChannelDO(o -> o
                .setName("人才招聘会").setSystemFlag(true));
        recruitChannelMapper.insert(dbChannel);
        // 准备参数
        HrmRecruitChannelSaveReqVO reqVO = randomRecruitChannelSaveReqVO(o -> o
                .setId(dbChannel.getId()).setName("招聘会"));

        // 调用，并断言异常
        assertServiceException(() -> recruitChannelService.updateRecruitChannel(reqVO),
                RECRUIT_CHANNEL_SYSTEM_NAME_UPDATE_FORBIDDEN);
    }

    @Test
    public void testUpdateRecruitChannel_notExists() {
        // 准备参数
        HrmRecruitChannelSaveReqVO reqVO = randomRecruitChannelSaveReqVO(o -> o.setId(randomLongId()));

        // 调用，并断言异常
        assertServiceException(() -> recruitChannelService.updateRecruitChannel(reqVO),
                RECRUIT_CHANNEL_NOT_EXISTS);
    }

    @Test
    public void testUpdateRecruitChannelStatus_success() {
        // mock 数据
        HrmRecruitChannelDO dbChannel = randomRecruitChannelDO(o -> o
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        recruitChannelMapper.insert(dbChannel);
        // 准备参数
        HrmRecruitChannelStatusReqVO reqVO = randomPojo(HrmRecruitChannelStatusReqVO.class, o -> o
                .setId(dbChannel.getId()).setStatus(CommonStatusEnum.DISABLE.getStatus()));

        // 调用
        recruitChannelService.updateRecruitChannelStatus(reqVO);

        // 断言
        assertEquals(reqVO.getStatus(), recruitChannelMapper.selectById(dbChannel.getId()).getStatus());
    }

    @Test
    public void testDeleteRecruitChannel_success() {
        // mock 数据
        HrmRecruitChannelDO dbChannel = randomRecruitChannelDO(o -> o.setSystemFlag(false));
        recruitChannelMapper.insert(dbChannel);
        HrmRecruitChannelDO transferChannel = randomRecruitChannelDO(o -> o
                .setSystemFlag(true).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        recruitChannelMapper.insert(transferChannel);
        // 准备参数
        HrmRecruitChannelDeleteReqVO reqVO = randomPojo(HrmRecruitChannelDeleteReqVO.class, o -> o
                .setId(dbChannel.getId()).setTransferChannelId(transferChannel.getId()));

        // 调用
        recruitChannelService.deleteRecruitChannel(reqVO);

        // 断言
        assertNull(recruitChannelMapper.selectById(dbChannel.getId()));
        assertNotNull(recruitChannelMapper.selectById(transferChannel.getId()));
        verify(employeeService).updateEmployeeChannelByChannelId(dbChannel.getId(), transferChannel.getId());
        verify(recruitCandidateService).updateRecruitCandidateChannelByChannelId(
                dbChannel.getId(), transferChannel.getId());
    }

    @Test
    public void testDeleteRecruitChannel_systemDeleteForbidden() {
        // mock 数据
        HrmRecruitChannelDO dbChannel = randomRecruitChannelDO(o -> o.setSystemFlag(true));
        recruitChannelMapper.insert(dbChannel);
        // 准备参数
        HrmRecruitChannelDeleteReqVO reqVO = randomPojo(HrmRecruitChannelDeleteReqVO.class, o -> o
                .setId(dbChannel.getId()).setTransferChannelId(randomLongId()));

        // 调用，并断言异常
        assertServiceException(() -> recruitChannelService.deleteRecruitChannel(reqVO),
                RECRUIT_CHANNEL_SYSTEM_DELETE_FORBIDDEN);
        verifyNoInteractions(employeeService, recruitCandidateService);
    }

    @Test
    public void testDeleteRecruitChannel_transferSelf() {
        // mock 数据
        HrmRecruitChannelDO dbChannel = randomRecruitChannelDO(o -> o.setSystemFlag(false));
        recruitChannelMapper.insert(dbChannel);
        // 准备参数
        HrmRecruitChannelDeleteReqVO reqVO = randomPojo(HrmRecruitChannelDeleteReqVO.class, o -> o
                .setId(dbChannel.getId()).setTransferChannelId(dbChannel.getId()));

        // 调用，并断言异常
        assertServiceException(() -> recruitChannelService.deleteRecruitChannel(reqVO),
                RECRUIT_CHANNEL_TRANSFER_SELF);
        verifyNoInteractions(employeeService, recruitCandidateService);
    }

    @Test
    public void testDeleteRecruitChannel_transferDisabled() {
        // mock 数据
        HrmRecruitChannelDO dbChannel = randomRecruitChannelDO(o -> o.setSystemFlag(false));
        recruitChannelMapper.insert(dbChannel);
        HrmRecruitChannelDO transferChannel = randomRecruitChannelDO(o -> o
                .setStatus(CommonStatusEnum.DISABLE.getStatus()));
        recruitChannelMapper.insert(transferChannel);
        // 准备参数
        HrmRecruitChannelDeleteReqVO reqVO = randomPojo(HrmRecruitChannelDeleteReqVO.class, o -> o
                .setId(dbChannel.getId()).setTransferChannelId(transferChannel.getId()));

        // 调用，并断言异常
        assertServiceException(() -> recruitChannelService.deleteRecruitChannel(reqVO),
                RECRUIT_CHANNEL_TRANSFER_DISABLED);
        verifyNoInteractions(employeeService, recruitCandidateService);
    }

    @Test
    public void testGetRecruitChannel() {
        // mock 数据
        HrmRecruitChannelDO dbChannel = randomRecruitChannelDO();
        recruitChannelMapper.insert(dbChannel);

        // 调用
        HrmRecruitChannelDO channel = recruitChannelService.getRecruitChannel(dbChannel.getId());

        // 断言
        assertPojoEquals(dbChannel, channel);
    }

    @Test
    public void testGetRecruitChannelList() {
        // mock 数据
        HrmRecruitChannelDO firstChannel = randomRecruitChannelDO();
        recruitChannelMapper.insert(firstChannel);
        HrmRecruitChannelDO secondChannel = randomRecruitChannelDO();
        recruitChannelMapper.insert(secondChannel);
        HrmRecruitChannelDO otherChannel = randomRecruitChannelDO();
        recruitChannelMapper.insert(otherChannel);
        // 准备参数
        List<Long> ids = asList(firstChannel.getId(), secondChannel.getId());

        // 调用
        List<HrmRecruitChannelDO> channels = recruitChannelService.getRecruitChannelList(ids);

        // 断言
        assertEquals(2, channels.size());
        assertEquals(new HashSet<>(ids), convertSet(channels, HrmRecruitChannelDO::getId));
    }

    @Test
    public void testGetRecruitChannelList_empty() {
        // 调用
        List<HrmRecruitChannelDO> channels = recruitChannelService.getRecruitChannelList(emptyList());

        // 断言
        assertTrue(channels.isEmpty());
    }

    @Test
    public void testGetRecruitChannelPage() {
        // mock 数据
        HrmRecruitChannelDO dbChannel = randomRecruitChannelDO(o -> o
                .setName("BOSS 直聘").setStatus(CommonStatusEnum.ENABLE.getStatus()));
        recruitChannelMapper.insert(dbChannel);
        // 测试 name 不匹配
        recruitChannelMapper.insert(cloneIgnoreId(dbChannel, o -> o.setName("猎聘网")));
        // 测试 status 不匹配
        recruitChannelMapper.insert(cloneIgnoreId(dbChannel,
                o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // 准备参数
        HrmRecruitChannelPageReqVO reqVO = new HrmRecruitChannelPageReqVO();
        reqVO.setName("BOSS");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        // 调用
        PageResult<HrmRecruitChannelDO> pageResult = recruitChannelService.getRecruitChannelPage(reqVO);

        // 断言
        assertEquals(1L, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbChannel, pageResult.getList().get(0));
    }

    @Test
    public void testGetRecruitChannelSimpleList() {
        // mock 数据
        HrmRecruitChannelDO secondChannel = randomRecruitChannelDO(o -> o
                .setSort(20).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        recruitChannelMapper.insert(secondChannel);
        HrmRecruitChannelDO firstChannel = randomRecruitChannelDO(o -> o
                .setSort(10).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        recruitChannelMapper.insert(firstChannel);
        // 测试 status 不匹配
        recruitChannelMapper.insert(randomRecruitChannelDO(o -> o
                .setSort(1).setStatus(CommonStatusEnum.DISABLE.getStatus())));

        // 调用
        List<HrmRecruitChannelDO> channels = recruitChannelService.getRecruitChannelSimpleList();

        // 断言
        assertEquals(asList(firstChannel.getId(), secondChannel.getId()),
                convertList(channels, HrmRecruitChannelDO::getId));
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static HrmRecruitChannelDO randomRecruitChannelDO(Consumer<HrmRecruitChannelDO>... consumers) {
        Consumer<HrmRecruitChannelDO> consumer = o -> o.setName(randomString()).setSort(randomInteger())
                .setStatus(randomCommonStatus()).setSystemFlag(false);
        return randomPojo(HrmRecruitChannelDO.class, ArrayUtils.append(consumer, consumers));
    }

    @SafeVarargs
    private static HrmRecruitChannelSaveReqVO randomRecruitChannelSaveReqVO(
            Consumer<HrmRecruitChannelSaveReqVO>... consumers) {
        Consumer<HrmRecruitChannelSaveReqVO> consumer = o -> o.setId(null)
                .setName(randomString()).setSort(randomInteger());
        return randomPojo(HrmRecruitChannelSaveReqVO.class, ArrayUtils.append(consumer, consumers));
    }

}
