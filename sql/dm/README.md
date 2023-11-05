# 达梦数据库适配

## 达梦测试环境

可以使用Docker或Windows服务器版本。

### Docker

参考文档 https://eco.dameng.com/document/dm/zh-cn/start/dm-install-docker.html

下载 Docker 镜像 (版本: 20230808)：https://download.dameng.com/eco/dm8/dm8_20230808_rev197096_x86_rh6_64_single.tar

```shell
docker load -i dm8_20230808_rev197096_x86_rh6_64_single.tar

docker run -d -p 5236:5236 \
    --restart=unless-stopped \
    --name dm8_test \
    --privileged=true \
    -e PAGE_SIZE=16 \
    -e LD_LIBRARY_PATH=/opt/dmdb~~~~ms/bin \
    -e EXTENT_SIZE=32 \
    -e BLANK_PAD_MODE=1 \
    -e LOG_SIZE=1024 \
    -e UNICODE_FLAG=1 \
    -e LENGTH_IN_CHAR=1 \
    -e INSTANCE_NAME=dm8_test \
    -v $PWD/dm8_test:/opt/dmdbms/data \
    dm8_single:dm8_20230808_rev197096_x86_rh6_64
```

备注：可以尝试使用大小写不敏感配置`-e CASE_SENSITIVE=N`，需要停止并删除容器后，删除`dm8_test`目录，重新`docker run`。

### Windows

Windows 版本(20230928)：https://eco.dameng.com/
https://download.dameng.com/eco/adapter/DM8/202310/dm8_20230928_x86_win_64.zip

安装参考文档：https://eco.dameng.com/document/dm/zh-cn/start/install-dm-windows-prepare.html

傻瓜式安装、数据库实例化，一路下去就好，不用修改任何参数。

## 更新达梦 jdbc 版本

`yudao-dependencies/pom.xml`

```xml

<dm8.jdbc.version>8.1.3.62</dm8.jdbc.version>
```

非常重要，老版本的 jdbc 大概率有问题，会报错`Invalid Column`

## 数据库管理软件

DM 管理工具：https://eco.dameng.com/document/dm/zh-cn/start/tool-dm-manager.html

## 数据库导入

为了方便起见，建议使用 SYSDBA/SYSDBA001 建立用户 RUOYI_VUE_PRO/123456
这样就会建立一个名为 RUOYI_VUE_PRO 的 schema。

在不指定默认 schema 的情况下，会使用同名的 schema 作为默认。

执行达梦的 `sql/dm/ruoyi-vue-pro-dm8.sql` 文件。

## 数据库链接信息

```yaml
# application-local.yaml
url: jdbc:dm://localhost:5236
username: RUOYI_VUE_PRO
password: 123456
```

## DAO 改造

由于`domain`是达梦的关键字，因此需要修改`cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO`的相关字段

- 方法 1：直接修改为`domain_`，对应修改数据库字段
- 方法 2：添加注解`@TableField("\"DOMAIN\"")`

备注：可以通过?schema=RUOYI_VUE_PRO 来制定默认 schema 名称，这样就不用建一个 RUOYI_VUE_PRO 这么憋屈的用户名

## 关于大小写敏感

参考文档: 详解 DM 数据库字符串大小写敏感 https://eco.dameng.com/community/article/df11811a02de8e923c2e57ef6597bc62

## TODO

工作流仍未适配

有价值的参考文章：https://blog.csdn.net/TangBoBoa/article/details/130392495
