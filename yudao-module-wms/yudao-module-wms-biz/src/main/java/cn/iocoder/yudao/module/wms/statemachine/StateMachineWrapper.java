package cn.iocoder.yudao.module.wms.statemachine;

import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalReqVO;
import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.Visitor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author: LeeFJ
 * @date: 2025/2/28 14:16
 * @description: 状态机包装类
 */
public class StateMachineWrapper<S, E, D> implements StateMachine<S, E, ColaContext<D>> {

    /**
     * 状态机
     **/
    private StateMachine<S, E, ColaContext<D>> stateMachine;

    /**
     * 状态属性值提取器
     **/
    private Function<D,S> getter;


    public StateMachineWrapper(StateMachine<S, E, ColaContext<D>> stateMachine, Function<D,S> getter) {
        this.stateMachine = stateMachine;
        this.getter = getter;
    }

    @Override
    public boolean verify(S sourceStateId, E event) {
        return stateMachine.verify(sourceStateId, event);
    }

    @Override
    public S fireEvent(S sourceState, E event, ColaContext<D> ctx) {
        return stateMachine.fireEvent(sourceState,event,ctx);
    }

    @Override
    public List<S> fireParallelEvent(S sourceState, E event, ColaContext<D> ctx) {
        return stateMachine.fireParallelEvent(sourceState,event,ctx);
    }


    public S fireEvent(E event, ColaContext<D> ctx) {
        return stateMachine.fireEvent(getter.apply(ctx.data()),event,ctx);
    }


    @Override
    public String getMachineId() {
        return stateMachine.getMachineId();
    }

    @Override
    public void showStateMachine() {
        stateMachine.showStateMachine();
    }

    @Override
    public String generatePlantUML() {
        return stateMachine.generatePlantUML();
    }

    @Override
    public String accept(Visitor visitor) {
        return stateMachine.accept(visitor);
    }


    @Setter
    @Getter
    private S initStatus;

    @Setter
    private List<S> statusCanEdit;

    @Setter
    private List<S> statusCanDelete;


    @Setter
    private Map<S, List<S>> conditionMap;

    /**
     * 当前状态是否可编辑
     */
    public boolean canEdit(S status) {
        return statusCanEdit.contains(status);
    }

    /**
     * 当前状态是否可删除
     */
    public boolean canDelete(S status) {
        return statusCanDelete.contains(status);
    }

    /**
     * 获得起始状态
     */
    public List<S> getFroms(S to) {
        return conditionMap.get(to);
    }

    /**
     * 创建上下文
     */
    public ColaContext<D> createContext(WmsApprovalReqVO approvalReqVO,D data) {
        return ColaContext.from(data,approvalReqVO,this);
    }
}
