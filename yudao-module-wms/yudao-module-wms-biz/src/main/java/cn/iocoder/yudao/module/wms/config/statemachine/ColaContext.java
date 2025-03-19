package cn.iocoder.yudao.module.wms.config.statemachine;

import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalReqVO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: LeeFJ
 * @date: 2025/2/28 15:02
 * @description:
 */

public class ColaContext<T> {


    public static <X> ColaContext<X> from(X requestDO, WmsApprovalReqVO approvalReqVO) {
        ColaContext<X> context = new ColaContext<>();
        context.data=requestDO;
        context.approvalReqVO=approvalReqVO;
        context.success=true;
        return context;
    }
    /**
     * 是否被处理
     **/
    private Boolean handled = false;
    private Boolean success = true;
    private T data;
    private WmsApprovalReqVO approvalReqVO;
    private List<String> errors = new ArrayList<>();


    public Boolean success() {
        return success;
    }

    public Boolean failure() {
        return !success;
    }

    public Boolean handled() {
        return handled;
    }

    public void handled(Boolean handled) {
        this.handled=handled;
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
