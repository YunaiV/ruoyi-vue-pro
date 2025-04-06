package cn.iocoder.yudao.module.iot.script.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 产品脚本示例类，提供各种类型的产品脚本示例代码
 */
@Slf4j
@Component
public class ProductScriptSamples {

    /**
     * 获取属性解析脚本示例
     *
     * @return 属性解析脚本示例代码
     */
    public String getPropertyParserSample() {
        return "/**\n" +
                " * 属性上报数据解析脚本示例\n" +
                " * @param input 设备上报的原始数据\n" +
                " * @param productKey 产品标识\n" +
                " * @param method 方法类型，固定为 property\n" +
                " * @param properties 当前设备的属性数据\n" +
                " * @return 解析后的属性数据\n" +
                " */\n" +
                "function parseProperty(input, productKey) {\n" +
                "    // 记录日志\n" +
                "    console.log('开始解析属性数据: ' + input);\n" +
                "    \n" +
                "    try {\n" +
                "        // 假设上报的是 JSON 字符串\n" +
                "        var data = JSON.parse(input);\n" +
                "        \n" +
                "        // 构建属性数据结构\n" +
                "        var result = {\n" +
                "            // 属性上报的时间戳，毫秒级\n" +
                "            timestamp: data.timestamp || Date.now(),\n" +
                "            // 属性数据\n" +
                "            params: {}\n" +
                "        };\n" +
                "        \n" +
                "        // 处理属性值\n" +
                "        if (data.temperature) {\n" +
                "            result.params.temperature = parseFloat(data.temperature);\n" +
                "        }\n" +
                "        \n" +
                "        if (data.humidity) {\n" +
                "            result.params.humidity = parseFloat(data.humidity);\n" +
                "        }\n" +
                "        \n" +
                "        console.log('属性解析结果: ' + JSON.stringify(result));\n" +
                "        return result;\n" +
                "    } catch (error) {\n" +
                "        console.error('解析属性数据失败: ' + error.message);\n" +
                "        throw new Error('解析失败: ' + error.message);\n" +
                "    }\n" +
                "};\n" +
                "\n" +
                "// 执行解析\n" +
                "parseProperty(input, productKey);";
    }

    /**
     * 获取事件解析脚本示例
     *
     * @return 事件解析脚本示例代码
     */
    public String getEventParserSample() {
        return "/**\n" +
                " * 事件数据解析脚本示例\n" +
                " * @param input 设备上报的原始数据\n" +
                " * @param productKey 产品标识\n" +
                " * @param method 方法类型，固定为 event\n" +
                " * @param identifier 事件标识符\n" +
                " * @return 解析后的事件数据\n" +
                " */\n" +
                "function parseEvent(input, productKey, identifier) {\n" +
                "    // 记录日志\n" +
                "    console.log('开始解析事件数据: ' + input);\n" +
                "    console.log('事件标识符: ' + identifier);\n" +
                "    \n" +
                "    try {\n" +
                "        // 假设上报的是 JSON 字符串\n" +
                "        var data = JSON.parse(input);\n" +
                "        \n" +
                "        // 构建事件数据结构\n" +
                "        var result = {\n" +
                "            // 事件标识符\n" +
                "            identifier: identifier || 'alert',\n" +
                "            // 事件上报的时间戳，毫秒级\n" +
                "            timestamp: data.timestamp || Date.now(),\n" +
                "            // 事件参数\n" +
                "            params: {}\n" +
                "        };\n" +
                "        \n" +
                "        // 根据不同事件类型处理参数\n" +
                "        if (result.identifier === 'alert') {\n" +
                "            result.params.level = data.level || 'info';\n" +
                "            result.params.message = data.message || '';\n" +
                "        } else if (result.identifier === 'error') {\n" +
                "            result.params.code = data.code || 0;\n" +
                "            result.params.message = data.message || '';\n" +
                "        }\n" +
                "        \n" +
                "        console.log('事件解析结果: ' + JSON.stringify(result));\n" +
                "        return result;\n" +
                "    } catch (error) {\n" +
                "        console.error('解析事件数据失败: ' + error.message);\n" +
                "        throw new Error('解析失败: ' + error.message);\n" +
                "    }\n" +
                "};\n" +
                "\n" +
                "// 执行解析\n" +
                "parseEvent(input, productKey, identifier);";
    }

    /**
     * 获取命令编码脚本示例
     *
     * @return 命令编码脚本示例代码
     */
    public String getCommandEncoderSample() {
        return "/**\n" +
                " * 命令数据编码脚本示例\n" +
                " * @param input 平台下发的命令数据\n" +
                " * @param productKey 产品标识\n" +
                " * @param method 方法类型，固定为 command\n" +
                " * @param cmdParams 命令参数\n" +
                " * @return 编码后的命令数据\n" +
                " */\n" +
                "function encodeCommand(input, productKey, cmdParams) {\n" +
                "    // 记录日志\n" +
                "    console.log('开始编码命令数据: ' + input);\n" +
                "    console.log('命令参数: ' + JSON.stringify(cmdParams));\n" +
                "    \n" +
                "    try {\n" +
                "        // 输入可能是 JSON 字符串或对象\n" +
                "        var data = typeof input === 'string' ? JSON.parse(input) : input;\n" +
                "        \n" +
                "        // 获取命令名称和值\n" +
                "        var cmdName = cmdParams.cmdName || '';\n" +
                "        var cmdValue = cmdParams.cmdValue;\n" +
                "        \n" +
                "        // 构建设备可识别的命令格式\n" +
                "        var result = {\n" +
                "            cmd: cmdName,\n" +
                "            value: cmdValue,\n" +
                "            timestamp: Date.now()\n" +
                "        };\n" +
                "        \n" +
                "        // 根据不同命令类型构建参数\n" +
                "        if (cmdName === 'setValue') {\n" +
                "            // 无需额外处理\n" +
                "        } else if (cmdName === 'control') {\n" +
                "            result.mode = data.mode || 'auto';\n" +
                "            result.action = data.action || 'start';\n" +
                "        }\n" +
                "        \n" +
                "        // 转换为设备能识别的格式（此处以 JSON 字符串为例）\n" +
                "        var encodedResult = JSON.stringify(result);\n" +
                "        \n" +
                "        console.log('命令编码结果: ' + encodedResult);\n" +
                "        return encodedResult;\n" +
                "    } catch (error) {\n" +
                "        console.error('编码命令数据失败: ' + error.message);\n" +
                "        throw new Error('编码失败: ' + error.message);\n" +
                "    }\n" +
                "};\n" +
                "\n" +
                "// 执行编码\n" +
                "encodeCommand(input, productKey, cmdParams);";
    }
}