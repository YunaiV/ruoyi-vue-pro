
package cn.iocoder.yudao.module.iot.controller;

import cn.iocoder.yudao.module.iot.mqttrpc.client.RpcClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;

// TODO 芋艿：后续 review 下
/**
 * 插件实例 RPC 接口
 *
 * @author 芋道源码
 */
@RestController
@RequestMapping("/rpc")
@RequiredArgsConstructor
public class RpcController {

    @Resource
    private RpcClient rpcClient;

    @PostMapping("/add")
    public CompletableFuture<Object> add(@RequestParam int a, @RequestParam int b) throws Exception {
        return rpcClient.call("add", new Object[]{a, b}, 10);
    }

    @PostMapping("/concat")
    public CompletableFuture<Object> concat(@RequestParam String str1, @RequestParam String str2) throws Exception {
        return rpcClient.call("concat", new Object[]{str1, str2}, 10);
    }

}