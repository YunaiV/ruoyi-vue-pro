<template>
  <div class="login">
    <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
      <h3 class="title">芋道后台管理系统</h3>
      <el-form-item prop="tenantName" v-if="tenantEnable">
        <el-input v-model="loginForm.tenantName" type="text" auto-complete="off" placeholder='租户'>
          <svg-icon slot="prefix" icon-class="tree" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>
      <el-form-item prop="username">
        <el-input v-model="loginForm.username" type="text" auto-complete="off" placeholder="账号">
          <svg-icon slot="prefix" icon-class="user" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input v-model="loginForm.password" type="password" auto-complete="off" placeholder="密码" @keyup.enter.native="handleLogin">
          <svg-icon slot="prefix" icon-class="password" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>
      <el-form-item prop="code" v-if="captchaEnable">
        <el-input v-model="loginForm.code" auto-complete="off" placeholder="验证码" style="width: 63%" @keyup.enter.native="handleLogin">
          <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon" />
        </el-input>
        <div class="login-code">
          <img :src="codeUrl" @click="getCode" class="login-code-img"/>
        </div>
      </el-form-item>
      <el-checkbox v-model="loginForm.rememberMe" style="margin:0px 0px 25px 0px;">记住密码</el-checkbox>
      <el-form-item style="width:100%;">
        <el-button :loading="loading" size="medium" type="primary" style="width:100%;" @click.native.prevent="handleLogin">
          <span v-if="!loading">登 录</span>
          <span v-else>登 录 中...</span>
        </el-button>
      </el-form-item>

      <el-form-item style="width:100%;">
          <div class="oauth-login" style="display:flex">
            <div class="oauth-login-item" v-for="item in SysUserSocialTypeEnum" :key="item.type" @click="doSocialLogin(item)">
              <img :src="item.img" height="25px" width="25px" alt="登录" >
              <span>{{item.title}}</span>
            </div>
        </div>
      </el-form-item>
    </el-form>
    <!--  底部  -->
    <div class="el-login-footer">
      <span>Copyright © 2020-2021 iocoder.cn All Rights Reserved.</span>
    </div>
  </div>
</template>

<script>
import { getCodeImg,socialAuthRedirect } from "@/api/login";
import { getTenantIdByName } from "@/api/system/tenant";
import Cookies from "js-cookie";
import { encrypt, decrypt } from '@/utils/jsencrypt'
import {SystemUserSocialTypeEnum} from "@/utils/constants";
import { getTenantEnable } from "@/utils/ruoyi";

export default {
  name: "Login",
  data() {
    return {
      codeUrl: "",
      captchaEnable: true,
      tenantEnable: true,
      loginForm: {
        username: "admin",
        password: "admin123",
        rememberMe: false,
        code: "",
        uuid: "",
        tenantName: "芋道源码",
      },
      loginRules: {
        username: [
          { required: true, trigger: "blur", message: "用户名不能为空" }
        ],
        password: [
          { required: true, trigger: "blur", message: "密码不能为空" }
        ],
        code: [{ required: true, trigger: "change", message: "验证码不能为空" }],
        tenantName: [
          { required: true, trigger: "blur", message: "租户不能为空" },
          {
            validator: (rule, value, callback) => {
              getTenantIdByName(value).then(res => {
                const tenantId = res.data;
                if (tenantId && tenantId >= 0) {
                  // 设置租户
                  Cookies.set("tenantId", tenantId);
                  callback();
                } else {
                  callback('租户不存在');
                }
              });
            },
            trigger: 'blur'
          }
        ],
      },
      loading: false,
      redirect: undefined,
      // 枚举
      SysUserSocialTypeEnum: SystemUserSocialTypeEnum,
    };
  },
  // watch: {
  //   $route: {
  //     handler: function(route) {
  //       this.redirect = route.query && route.query.redirect;
  //     },
  //     immediate: true
  //   }
  // },
  created() {
    // 租户开关
    this.tenantEnable = getTenantEnable();
    // 重定向地址
    this.redirect = this.$route.query.redirect;
    this.getCode();
    this.getCookie();
  },
  methods: {
    getCode() {
      // 只有开启的状态，才加载验证码。默认开启
      if (!this.captchaEnable) {
        return;
      }
      // 请求远程，获得验证码
      getCodeImg().then(res => {
        res = res.data;
        this.captchaEnable = res.enable;
        if (this.captchaEnable) {
          this.codeUrl = "data:image/gif;base64," + res.img;
          this.loginForm.uuid = res.uuid;
        }
      });
    },
    getCookie() {
      const username = Cookies.get("username");
      const password = Cookies.get("password");
      const rememberMe = Cookies.get('rememberMe')
      const tenantName = Cookies.get('tenantName');
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe),
        tenantName: tenantName === undefined ? this.loginForm.tenantName : tenantName,
      };
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true;
          // 设置 Cookie
          if (this.loginForm.rememberMe) {
            Cookies.set("username", this.loginForm.username, { expires: 30 });
            Cookies.set("password", encrypt(this.loginForm.password), { expires: 30 });
            Cookies.set('rememberMe', this.loginForm.rememberMe, { expires: 30 });
            Cookies.set('tenantName', this.loginForm.tenantName, { expires: 30 });
          } else {
            Cookies.remove("username");
            Cookies.remove("password");
            Cookies.remove('rememberMe');
            Cookies.remove('tenantName');
          }
          // 发起登陆
          this.$store.dispatch("Login", this.loginForm).then(() => {
            this.$router.push({ path: this.redirect || "/" }).catch(()=>{});
          }).catch(() => {
            this.loading = false;
            this.getCode();
          });
        }
      });
    },
    doSocialLogin(socialTypeEnum) {
      // console.log("开始Oauth登录...%o", socialTypeEnum.code);
      // 设置登录中
      this.loading = true;
      // 计算 redirectUri
      const redirectUri = location.origin + '/social-login?type=' + socialTypeEnum.type + '&redirect=' + (this.redirect || "/"); // 重定向不能丢
      // const redirectUri = 'http://127.0.0.1:48080/api/gitee/callback';
      // const redirectUri = 'http://127.0.0.1:48080/api/dingtalk/callback';
      // 进行跳转
      socialAuthRedirect(socialTypeEnum.type, encodeURIComponent(redirectUri)).then((res) => {
        // console.log(res.url);
        window.location.href = res.data;
      });
    }
  }
};
</script>

<style rel="stylesheet/scss" lang="scss">
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-image: url("http://static.yudao.iocoder.cn/login-background.jpg");
  background-size: cover;
}
.title {
  margin: 0px auto 30px auto;
  text-align: center;
  color: #707070;
}

.login-form {
  border-radius: 6px;
  background: #ffffff;
  width: 500px;
  padding: 25px 25px 5px 25px;
  .el-input {
    height: 38px;
    input {
      height: 38px;
    }
  }
  .input-icon {
    height: 39px;
    width: 14px;
    margin-left: 2px;
  }
}
.login-tip {
  font-size: 13px;
  text-align: center;
  color: #bfbfbf;
}
.login-code {
  width: 33%;
  height: 38px;
  float: right;
  img {
    cursor: pointer;
    vertical-align: middle;
  }
}
.el-login-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #fff;
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
}
.login-code-img {
  height: 38px;
}
.oauth-login {
  display: flex;
  cursor:pointer;
}
.oauth-login-item {
  display: flex;
  align-items: center;
  margin-right: 10px;
}
.oauth-login-item img {
  height: 25px;
  width: 25px;
}
.oauth-login-item span:hover {
  text-decoration: underline red;
  color: red;
}
</style>
