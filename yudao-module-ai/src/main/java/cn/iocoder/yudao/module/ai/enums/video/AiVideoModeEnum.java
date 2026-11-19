package cn.iocoder.yudao.module.ai.enums.video;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * AI 视频生成方式的枚举：只有「视频生成」类型使用
 *
 * 每种方式只使用对应角色的输入文件（{@link #getInputRoles()}），其它角色提交时忽略
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiVideoModeEnum implements ArrayValuable<String> {

    TEXT("text", "文生视频", ImmutableList.of()),
    IMAGE("image", "图生视频", ImmutableList.of(AiVideoInputRoleEnum.FIRST_FRAME)),
    FIRST_LAST("first_last", "首尾帧", ImmutableList.of(AiVideoInputRoleEnum.FIRST_FRAME, AiVideoInputRoleEnum.LAST_FRAME)),
    REFERENCE("reference", "参考生视频", ImmutableList.of(AiVideoInputRoleEnum.REFERENCE_IMAGE,
            AiVideoInputRoleEnum.REFERENCE_VIDEO, AiVideoInputRoleEnum.REFERENCE_AUDIO));

    public static final String[] ARRAYS = Arrays.stream(values()).map(AiVideoModeEnum::getMode).toArray(String[]::new);

    /**
     * 方式
     */
    private final String mode;
    /**
     * 方式名
     */
    private final String name;
    /**
     * 使用的输入角色
     */
    private final List<AiVideoInputRoleEnum> inputRoles;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    public static AiVideoModeEnum of(String mode) {
        return Arrays.stream(values()).filter(item -> item.getMode().equals(mode)).findFirst().orElse(null);
    }

}
