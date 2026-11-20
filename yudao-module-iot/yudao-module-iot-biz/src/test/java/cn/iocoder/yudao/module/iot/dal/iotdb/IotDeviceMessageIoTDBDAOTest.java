package cn.iocoder.yudao.module.iot.dal.iotdb;

import cn.hutool.core.util.ReflectUtil;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceMessageDO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * {@link IotDeviceMessageIoTDBDAO} 写入时间分配的单元测试
 *
 * @author 芋道源码
 */
public class IotDeviceMessageIoTDBDAOTest {

    private final IotDeviceMessageIoTDBDAO dao = new IotDeviceMessageIoTDBDAO();

    @Test
    public void testAllocateTs_sameMillisecond() {
        // 调用，并断言：同一设备的同一毫秒依次顺延（例如说，同一 pack 拆出的消息）
        assertEquals(1000L, dao.allocateTs(1L, 1000L));
        assertEquals(1001L, dao.allocateTs(1L, 1000L));
        assertEquals(1002L, dao.allocateTs(1L, 1000L));
        // 调用，并断言：落在已使用的时间上，同样顺延
        assertEquals(1003L, dao.allocateTs(1L, 1001L));
        // 调用，并断言：其它设备不受影响
        assertEquals(1000L, dao.allocateTs(2L, 1000L));
    }

    @Test
    public void testAllocateTs_largePack() {
        // 调用，并断言：同一 pack 拆出超过 1001 条相同 ts 的消息，依次顺延，不重复使用已写入的时间
        for (int i = 0; i < 1500; i++) {
            assertEquals(1000L + i, dao.allocateTs(1L, 1000L));
        }
        // 调用，并断言：之后同一基准 ts 的消息，接着顺延
        assertEquals(2500L, dao.allocateTs(1L, 1000L));
    }

    @Test
    public void testAllocateTs_mergeAdjacent() {
        // 调用：写入 1000、1002，再写入 1001（与前后合并为 [1000, 1002]）
        assertEquals(1000L, dao.allocateTs(1L, 1000L));
        assertEquals(1002L, dao.allocateTs(1L, 1002L));
        assertEquals(1001L, dao.allocateTs(1L, 1001L));

        // 断言：落在合并后的区间内，顺延到区间之后
        assertEquals(1003L, dao.allocateTs(1L, 1000L));
    }

    @Test
    public void testAllocateTs_interleaved() {
        // 调用，并断言：实时消息与补传的历史消息交错写入，各自使用原时间
        assertEquals(5000L, dao.allocateTs(1L, 5000L));
        assertEquals(1000L, dao.allocateTs(1L, 1000L));
        assertEquals(1001L, dao.allocateTs(1L, 1000L));
        // 调用，并断言：之后同一毫秒的实时消息，不覆盖之前的
        assertEquals(5001L, dao.allocateTs(1L, 5000L));
        // 调用，并断言：未使用的时间，不顺延
        assertEquals(3000L, dao.allocateTs(1L, 3000L));
    }

    @Test
    public void testInsert_sameMillisecond() {
        // 准备参数
        IotDeviceMessageIoTDBMapper mapper = mock(IotDeviceMessageIoTDBMapper.class);
        ReflectUtil.setFieldValue(dao, "deviceMessageMapper", mapper);

        // 调用：同一设备、同一毫秒写入 2 条
        dao.insert(buildMessage("m1"));
        dao.insert(buildMessage("m2"));

        // 断言：物理时间不同，不会整行覆盖；原始上报时间不变
        ArgumentCaptor<IotDeviceMessageIoTDBRow> captor = ArgumentCaptor.forClass(IotDeviceMessageIoTDBRow.class);
        verify(mapper, times(2)).insert(captor.capture());
        List<IotDeviceMessageIoTDBRow> rows = captor.getAllValues();
        assertEquals(Arrays.asList(1000L, 1001L), convertList(rows, IotDeviceMessageIoTDBRow::getTs));
        assertEquals(rows.get(0).getReportTime(), rows.get(1).getReportTime());
    }

    private static IotDeviceMessageDO buildMessage(String id) {
        return new IotDeviceMessageDO().setTs(1000L).setDeviceId(1L).setId(id)
                .setMethod("thing.property.post").setUpstream(true).setReply(false).setReportTime(1000L);
    }

}
