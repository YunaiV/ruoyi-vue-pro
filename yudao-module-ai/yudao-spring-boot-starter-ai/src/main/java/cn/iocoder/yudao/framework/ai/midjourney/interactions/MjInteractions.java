package cn.iocoder.yudao.framework.ai.midjourney.interactions;

import cn.iocoder.yudao.framework.ai.midjourney.MidjourneyInteractionsEnum;

import java.util.List;

/**
 * mj Interactions
 *
 * author: fansili
 * time: 2024/4/3 17:22
 */
public interface MjInteractions {

    List<MidjourneyInteractionsEnum> supperInteractions();

    Boolean execute(String prompt);
}
