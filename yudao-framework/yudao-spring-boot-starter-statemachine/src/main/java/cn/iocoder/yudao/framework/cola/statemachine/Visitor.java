package cn.iocoder.yudao.framework.cola.statemachine;

/**
 * Visitor
 *
 * @author Frank Zhang
 * @date 2020-02-08 8:41 PM
 */
public interface Visitor {

    char LF = '\n';

    /**
     * @param visitable the element to be visited.
     * @return
     */
    String visitOnEntry(StateMachine<?, ?, ?> visitable);

    /**
     * @param visitable the element to be visited.
     * @return
     */
    String visitOnExit(StateMachine<?, ?, ?> visitable);

    /**
     * @param visitable the element to be visited.
     * @return
     */
    String visitOnEntry(State<?, ?, ?> visitable);

    /**
     * @param visitable the element to be visited.
     * @return
     */
    String visitOnExit(State<?, ?, ?> visitable);
}
