package cn.iocoder.yudao.framework.cola.statemachine;

import cn.iocoder.yudao.framework.cola.statemachine.builder.FailCallback;
import cn.iocoder.yudao.framework.cola.statemachine.builder.NumbFailCallback;
import cn.iocoder.yudao.framework.cola.statemachine.builder.StateMachineBuilderImpl;
import cn.iocoder.yudao.framework.cola.statemachine.builder.TransitionHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
class StateMachineBuilderImplTest2   {

    @Component
    public class BaseHandler implements TransitionHandler<String, Long, Integer> {

        @Override
        public void perform(String from, String to, Long event, Integer context) {
            log.info("do something");
        }

        @Override
        public boolean when(Integer context) {
            return false;
        }
    }

    @Component
    public class TestFallback implements FailCallback<String, Long, Integer> {

        @Override
        public void onFail(String sourceState, String targetState, Long event, Integer context) {

        }
    }

    @Test
    void test() {


        var builder = new StateMachineBuilderImpl<String, Long, Integer>();
        var handler = new BaseHandler();

        builder.externalTransition()
            .from("3")
            .to("7")
            .on(4L)
            .handle(handler)
        ;

        builder.setFailCallback(new NumbFailCallback<>());

        builder.setFailCallback((source, target,event, context) -> {
            if(target==null) {
                System.out.println("event match fail");
            } else {
                System.out.println("condition fail");
            }
        });


        var sm = builder.build("someSM");
        sm.setGetter(Objects::toString);

        log.info(
            sm.fireEvent(4L, 3)
        );
    }



}