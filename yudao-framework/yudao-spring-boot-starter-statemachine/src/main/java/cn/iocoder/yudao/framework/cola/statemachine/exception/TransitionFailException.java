package cn.iocoder.yudao.framework.cola.statemachine.exception;

/**
 * @author 龙也
 * @date 2022/9/15 12:08 PM
 */
public class TransitionFailException extends RuntimeException {

    public TransitionFailException(String errMsg) {
        super(errMsg);
    }
}
