package cn.iocoder.yudao.module.iot.plugin;


import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/iot/plugin-demo")
@Extension
public class IoTHttpPluginController implements ExtensionPoint {

    @GetMapping("/greet")
    public String greet() {
        return "Hello from MyPlugin!";
    }

    @PostMapping("/message")
    public void receiveMessage(@RequestBody String message) {
        System.out.println("Received message: " + message);
    }
}
