package cn.iocoder.yudao.module.wms.config.statemachine;

import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.Visitor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.function.Function;

/**
 * @author: LeeFJ
 * @date: 2025/2/28 14:16
 * @description:
 */
public class StateMachineWrapper<S, E,D> implements StateMachine<S, E, ColaContext<D>> {

    private StateMachine<S, E, ColaContext<D>> stateMachine;
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

    public boolean canEdit(S status) {
        return statusCanEdit.contains(status);
    }
    public boolean canDelete(S status) {
        return statusCanDelete.contains(status);
    }


}
