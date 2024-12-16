package cn.iocoder.yudao.module.iot.controller.admin.plugininfo;

import jakarta.annotation.Resource;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.security.PermitAll;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 插件 Controller 测试用例
 */
@RestController
@RequestMapping("/iot/plugins")
public class PluginController {

    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private SpringPluginManager springPluginManager;
    @Resource
    private Greetings greetings;

    @Value("${pf4j.pluginsDir}")
    private String pluginsDir;

    /**
     * 上传插件 JAR 文件并加载插件
     *
     * @param file 上传的 JAR 文件
     * @return 上传结果
     */
    @PermitAll
    @PostMapping("/upload")
    public ResponseEntity<String> uploadPlugin(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("上传的文件为空");
        }

        // 确保插件目录存在
        Path pluginsPath = Paths.get(pluginsDir);
        try {
            if (!Files.exists(pluginsPath)) {
                Files.createDirectories(pluginsPath);
            }

            // 保存上传的 JAR 文件到插件目录
            String filename = file.getOriginalFilename();
            if (filename == null || (!filename.endsWith(".jar") && !filename.endsWith(".zip"))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("上传的文件不是 JAR 或 ZIP 文件");
            }

            Path jarPath = pluginsPath.resolve(filename);

            Files.copy(file.getInputStream(), jarPath, StandardCopyOption.REPLACE_EXISTING);

            // 加载插件
            String pluginId = springPluginManager.loadPlugin(jarPath.toAbsolutePath());

            // 启动插件
            springPluginManager.startPlugin(pluginId);

            return ResponseEntity.ok("插件上传并加载成功");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("上传插件时发生错误: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("加载插件时发生错误: " + e.getMessage());
        }
    }

    /**
     * 卸载指定插件
     *
     * @param pluginId 插件 ID
     * @return 卸载结果
     */
    @PermitAll
    @DeleteMapping("/unload/{pluginId}")
    public ResponseEntity<String> unloadPlugin(@PathVariable String pluginId) {
        if (springPluginManager.getPlugins().stream().noneMatch(plugin -> plugin.getDescriptor().getPluginId().equals(pluginId))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("插件未加载: " + pluginId);
        }

        springPluginManager.stopPlugin(pluginId);
        springPluginManager.unloadPlugin(pluginId);

        // 删除插件 JAR 文件（可选）
//            PluginWrapper plugin = pluginManager.getPlugin(pluginId);
//            PluginDescriptor descriptor = plugin.getDescriptor();
//            Path jarPath = Paths.get(pluginsDir).resolve(descriptor.getPluginId() + ".jar");
//            Files.deleteIfExists(jarPath);

        return ResponseEntity.ok("插件卸载成功: " + pluginId);
    }

    /**
     * 列出所有已加载的插件
     *
     * @return 插件列表
     */
    @PermitAll
    @GetMapping("/list")
    public ResponseEntity<List<String>> listPlugins() {
        List<String> plugins = springPluginManager.getPlugins().stream()
                .map(plugin -> plugin.getDescriptor().getPluginId())
                .collect(Collectors.toList());
        return ResponseEntity.ok(plugins);
    }

    /**
     * 打印问候语
     *
     * @return 问候语数量
     */
    @PermitAll
    @GetMapping("/printGreetings")
    public ResponseEntity<Integer> printGreetings() {
        Integer count = greetings.printGreetings();
        return ResponseEntity.ok(count);
    }
}
