package cn.iocoder.yudao.module.wms.config.statemachine;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: LeeFJ
 * @date: 2025/2/28 15:02
 * @description:
 */

public class ColaContext<T> {

    public static <X> ColaContext<X> from(X requestDO) {
        ColaContext<X> context = new ColaContext<>();
        context.data=requestDO;
        context.success=true;
        return context;
    }

    private Boolean success = true;
    private T data;
    private List<String> errors = new ArrayList<>();


    public Boolean success() {
        return success;
    }

    public Boolean failure() {
        return !success;
    }

    public T data() {
        return data;
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
