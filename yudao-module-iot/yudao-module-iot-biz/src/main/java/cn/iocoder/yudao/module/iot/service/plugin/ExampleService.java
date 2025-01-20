//package cn.iocoder.yudao.module.iot.service.plugin;
//
//import cn.iocoder.yudao.module.iot.mqttrpc.server.RpcServer;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.PostConstruct;
//
//@Service
//@RequiredArgsConstructor
//public class ExampleService {
//
//    private final RpcServer rpcServer;
//
//    @PostConstruct
//    public void registerMethods() {
//        rpcServer.registerMethod("add", params -> {
//            if (params.length != 2) {
//                throw new IllegalArgumentException("add方法需要两个参数");
//            }
//            int a = ((Number) params[0]).intValue();
//            int b = ((Number) params[1]).intValue();
//            return add(a, b);
//        });
//
//        rpcServer.registerMethod("concat", params -> {
//            if (params.length != 2) {
//                throw new IllegalArgumentException("concat方法需要两个参数");
//            }
//            String str1 = params[0].toString();
//            String str2 = params[1].toString();
//            return concat(str1, str2);
//        });
//    }
//
//    private int add(int a, int b) {
//        return a + b;
//    }
//
//    private String concat(String a, String b) {
//        return a + b;
//    }
//}