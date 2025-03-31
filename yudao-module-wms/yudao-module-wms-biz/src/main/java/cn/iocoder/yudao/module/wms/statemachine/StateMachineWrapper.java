package cn.iocoder.yudao.module.wms.statemachine;

import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalReqVO;
import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.Visitor;

import java.util.List;
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

    private List<ColaTransition<S, E, ColaContext<D>>> actions;

//    /**
//     * 初始状态
//     **/
//    @Setter
//    @Getter
//    private S initStatus;

//    /**
//     * 可编辑状态
//     **/
//    @Setter
//    private List<S> statusCanEdit;

//    /**
//     * 状态可删除状态
//     **/
//    @Setter
//    private List<S> statusCanDelete;

    /**
     * 状态属性值提取器
     **/
    private Function<D,S> getter;


    /**
     * 构造函数
     **/
    public StateMachineWrapper(StateMachine<S, E, ColaContext<D>> stateMachine, List<ColaTransition<S, E, ColaContext<D>>> actions, Function<D,S> getter) {
        this.stateMachine = stateMachine;
        this.actions = actions;
        this.getter = getter;
    }


    /**
     * 校验事件起始状态与事件
     * @return    是否触发
     **/
    @Override
    public boolean verify(S from, E event) {
        return stateMachine.verify(from, event);
    }

    /**
     * 触发事件
     **/
    @Override
    public S fireEvent(S from, E event, ColaContext<D> ctx) {
        return stateMachine.fireEvent(from,event,ctx);
    }

    /**
     * 触发并行事件
     **/
    @Override
    public List<S> fireParallelEvent(S from, E event, ColaContext<D> ctx) {
        return stateMachine.fireParallelEvent(from,event,ctx);
    }


    /**
     * 触发事件，起始状态从 利用 getter 方法从上下文的数据对象获取
     **/
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





//    /**
//     * 当前状态是否可编辑
//     */
//    public boolean canEdit(S status) {
//        return statusCanEdit.contains(status);
//    }

//    /**
//     * 当前状态是否可删除
//     */
//    public boolean canDelete(S status) {
//        return statusCanDelete.contains(status);
//    }

    /**
     * 获得目标状态
     * @param from   起始状态
     * @param event  事件
     */
    public S getTo(S from,E event) {

        for (ColaTransition<S, E, ColaContext<D>> action : actions) {
            if(action.getEvent()!=event) {
                continue;
            }
            for (S f : action.getFrom()) {
                if(from==f) {
                    return action.getTo();
                }
            }
        }

        return null;
    }

    /**
     * 创建上下文
     */
    public ColaContext<D> createContext(WmsApprovalReqVO approvalReqVO,D data) {
        return ColaContext.from(data,approvalReqVO,this);
    }
}
