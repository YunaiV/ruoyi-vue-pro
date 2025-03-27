package cn.iocoder.yudao.module.wms.statemachine;

import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalReqVO;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: LeeFJ
 * @date: 2025/2/28 15:02
 * @description:
 */

public class ColaContext<T> {


    static <X> ColaContext<X> from(X requestDO, WmsApprovalReqVO approvalReqVO,StateMachineWrapper stateMachineWrapper) {
        ColaContext<X> context = new ColaContext<>();
        context.data=requestDO;
        context.approvalReqVO=approvalReqVO;
        context.success=true;
        context.stateMachineWrapper=stateMachineWrapper;
        return context;
    }
    /**
     * 是否被处理
     **/
    private Boolean satisfied = false;
    private Boolean performed = false;
    private Boolean success = true;
    private T data;
    private WmsApprovalReqVO approvalReqVO;
    private List<String> errors = new ArrayList<>();

    @Getter
    private StateMachineWrapper stateMachineWrapper;


    public Boolean success() {
        return success;
    }

    public Boolean failure() {
        return !success;
    }

    public Boolean isSatisfied() {
        return satisfied;
    }


    /**
     * 是否匹配条件，包括状态条件与其它数据具备的条件
     **/
    public void isSatisfied(Boolean satisfied) {
        this.satisfied =satisfied;
    }

    public Boolean isPerformed() {
        return performed;
    }


    public void isPerformed(Boolean performed) {
        this.performed = performed;
    }


    public T data() {
        return data;
    }

    public WmsApprovalReqVO approvalReqVO() {
        return approvalReqVO;
    }

    public void addError(String message) {
        errors.add(message);
        success=false;
    }

    private String getDefaultError() {
        if(this.errors.isEmpty()) {
            return null;
        } else {
            return this.errors.get(0);
        }
    }


}
