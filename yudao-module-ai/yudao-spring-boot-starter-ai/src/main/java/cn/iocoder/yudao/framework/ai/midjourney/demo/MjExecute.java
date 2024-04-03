package cn.iocoder.yudao.framework.ai.midjourney.demo;

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
    boolean execute(MjCommandEnum mjCommand, String prompt);

}
