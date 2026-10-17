目的
- 将当前“单体多模块”的芋道项目，按需生成一个“单模块”版本，方便初学者或小团队使用与二次开发。

本方案做了什么
- 新增 PowerShell 脚本 script\\tools\\gen-single-module.ps1，用于在仓库内生成一个单模块输出目录（默认 single-module-output）。
- 脚本会复制核心源码与资源，并生成一个基础 pom.xml 和启动类，尽量做到开箱可编译、可运行；但仍可能需要你按需调整依赖与配置。

如何使用
1) 打开 PowerShell，切换到仓库根目录。
2) 运行：

   .\\script\\tools\\gen-single-module.ps1 -OutputDir single-module-output

3) 进入输出目录构建运行：

   cd .\\single-module-output
   mvn -T 1C -U -DskipTests package
   java -jar .\\target\\yudao-single-<version>.jar

注意
- 该脚本采用“拍平合并”的方式：会将 server、framework 以及常见业务模块的源码与资源集中到单一模块。根据你的需求可以在脚本中增删需要参与合并的模块目录。
- 由于原工程包含很多可选组件（RocketMQ、RabbitMQ、Quartz、IoT 等），生成的 pom.xml 仅包含常用依赖。如你使用到其他组件，请在输出 pom.xml 中自行添加。
- 配置文件合并采取“后覆盖前”的简单策略，可能需要你手动比对与调优。
- 生成物不会修改原始代码，可删除输出目录并重新生成。
