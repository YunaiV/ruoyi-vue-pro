package cn.iocoder.yudao.framework.ai.midjourney.demo;

import cn.iocoder.yudao.framework.ai.midjourney.MidjourneyInteractionsEnum;

/**
 * mj 命令
 */
public interface MjExecute {

    /**
     * 执行命令
     *
     * @param mjCommand
     * @param prompt
     * @return
     */
    boolean execute(MidjourneyInteractionsEnum mjCommand, String prompt);

}
