package cn.iocoder.yudao.module.iot.core.enums.modbus;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IoT Modbus 功能码枚举
 *
 * 对应《MODBUS Application Protocol Specification V1.1b3》5.1 节的公共功能码；
 * 不含保留、用户自定义功能码，也不含 43 的 MEI 子功能码（13、14）。
 * 注意：点位配置目前只使用 1~4 读功能码，其余功能码仅作定义。
 *
 * @author 芋道源码
 */
@Getter
@RequiredArgsConstructor
public enum IotModbusFunctionCodeEnum implements ArrayValuable<Integer> {

    READ_COILS(1, "读线圈"),
    READ_DISCRETE_INPUTS(2, "读离散输入"),
    READ_HOLDING_REGISTERS(3, "读保持寄存器"),
    READ_INPUT_REGISTERS(4, "读输入寄存器"),
    WRITE_SINGLE_COIL(5, "写单个线圈"),
    WRITE_SINGLE_REGISTER(6, "写单个寄存器"),
    READ_EXCEPTION_STATUS(7, "读异常状态"), // 仅串行链路
    DIAGNOSTICS(8, "诊断"), // 仅串行链路
    GET_COMM_EVENT_COUNTER(11, "获取通信事件计数器"), // 仅串行链路
    GET_COMM_EVENT_LOG(12, "获取通信事件记录"), // 仅串行链路
    WRITE_MULTIPLE_COILS(15, "写多个线圈"),
    WRITE_MULTIPLE_REGISTERS(16, "写多个寄存器"),
    REPORT_SERVER_ID(17, "报告从站 ID"), // 仅串行链路
    READ_FILE_RECORD(20, "读文件记录"),
    WRITE_FILE_RECORD(21, "写文件记录"),
    MASK_WRITE_REGISTER(22, "屏蔽写寄存器"),
    READ_WRITE_MULTIPLE_REGISTERS(23, "读/写多个寄存器"),
    READ_FIFO_QUEUE(24, "读 FIFO 队列"),
    ENCAPSULATED_INTERFACE_TRANSPORT(43, "封装接口传输");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(IotModbusFunctionCodeEnum::getCode)
            .toArray(Integer[]::new);

    /**
     * 功能码
     */
    private final Integer code;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
