<template>
  <div class="container">
    <div class="logo"></div>
    <!-- 登录区域 -->
    <div class="content">
      <!-- 配图 -->
      <div class="pic"></div>
      <!-- 表单 -->
      <div class="field">
        <!-- [移动端]标题 -->
        <h2 class="mobile-title">
          <h3 class="title">芋道后台管理系统</h3>
        </h2>

        <!-- 表单 -->
        <div class="form-cont">
          <el-tabs class="form" v-model="loginForm.loginType " style=" float:none;">
            <el-tab-pane label="绑定账号" name="uname">
            </el-tab-pane>
          </el-tabs>
          <div>
            <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
              <!-- 账号密码登录 -->
              <el-form-item prop="username">
                <el-input v-model="loginForm.username" type="text" auto-complete="off" placeholder="账号">
                  <svg-icon slot="prefix" icon-class="user" class="el-input__icon input-icon"/>
                </el-input>
              </el-form-item>
              <el-form-item prop="password">
                <el-input v-model="loginForm.password" type="password" auto-complete="off" placeholder="密码"
                          @keyup.enter.native="handleLogin">
                  <svg-icon slot="prefix" icon-class="password" class="el-input__icon input-icon"/>
                </el-input>
              </el-form-item>
              <el-checkbox v-model="loginForm.rememberMe" style="margin:0 0 25px 0;">记住密码</el-checkbox>
              <!-- 下方的登录按钮 -->
              <el-form-item style="width:100%;">
                <el-button :loading="loading" size="medium" type="primary" style="width:100%;"
                           @click.native.prevent="getCode">
                  <span v-if="!loading">登 录</span>
                  <span v-else>登 录 中...</span>
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>
    </div>

    <!-- 图形验证码 -->
    <Verify ref="verify" :captcha-type="'blockPuzzle'" :img-size="{width:'400px',height:'200px'}"
            @success="handleLogin" />

    <!-- footer -->
    <div class="footer">
      Copyright © 2020-2022 iocoder.cn All Rights Reserved.
    </div>
  </div>
</template>

<script>
import {
  getPassword, getRememberMe,
  getUsername,
  removePassword,
  removeUsername,
  setPassword,
  setRememberMe,
  setUsername
} from "@/utils/auth";

import Verify from '@/components/Verifition/Verify';
import {getCaptchaEnable} from "@/utils/ruoyi";

export default {
  name: "ThirdLogin",
  components: {
    Verify
  },
  data() {
    return {
      codeUrl: "",
      captchaEnable: true,
      loginForm: {
        loginType: "uname",
        username: "admin",
        password: "admin123",
        rememberMe: false,
        captchaVerification: "",
      },
      loginRules: {
        username: [
          { required: true, trigger: "blur", message: "用户名不能为空" }
        ],
        password: [
          { required: true, trigger: "blur", message: "密码不能为空" }
        ],
      },
      loading: false,
      redirect: undefined,
      // 社交登录相关
      type: undefined,
      code: undefined,
      state: undefined,
    };
  },
  created() {
    this.getCookie();
    // 验证码开关
    this.captchaEnable = getCaptchaEnable();
    // 重定向地址
    this.redirect = this.getUrlValue('redirect');
    // 社交登录相关
    this.type = this.getUrlValue('type');
    this.code = this.$route.query.code;
    this.state = this.$route.query.state;

    // 尝试登录一下
    this.loading = true;
    this.$store.dispatch("SocialLogin", {
      code: this.code,
      state: this.state,
      type: this.type
    }).then(() => {
      this.$router.push({ path: this.redirect || "/" }).catch(()=>{});
    }).catch(() => {
      this.loading = false;
    });
  },
  methods: {
    getCode() {
      // 情况一，未开启：则直接登录
      if (!this.captchaEnable) {
        this.handleLogin({})
        return;
      }

      // 情况二，已开启：则展示验证码；只有完成验证码的情况，才进行登录
      // 弹出验证码
      this.$refs.verify.show()
    },
    getCookie() {
      const username = getUsername();
      const password = getPassword();
      const rememberMe = getRememberMe();
      this.loginForm = {
        username: username ? username : this.loginForm.username,
        password: password ? password : this.loginForm.password,
        rememberMe: rememberMe ? getRememberMe() : false,
        loginType: this.loginForm.loginType,
      };
    },
    handleLogin(captchaParams) {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true;
          if (this.loginForm.rememberMe) {
            setUsername(this.loginForm.username)
            setPassword(this.loginForm.password)
            setRememberMe(this.loginForm.rememberMe)
          } else {
            removeUsername()
            removePassword()
          }
          this.$store.dispatch("Login", {
            socialCode: this.code,
            socialState: this.state,
            socialType: this.type,
            // 账号密码登录
            username: this.loginForm.username,
            password: this.loginForm.password,
            captchaVerification: captchaParams.captchaVerification
          }).then(() => {
            this.$router.push({ path: this.redirect || "/" }).catch(()=>{});
          }).catch(() => {
            this.loading = false;
            this.getCode()
          });
        }
      });
    },
    getUrlValue(key) {
      const url = new URL(decodeURIComponent(location.href));
      return url.searchParams.get(key);
    }
  }
};
</script>

<style rel="stylesheet/scss" lang="scss">
@import "~@/assets/styles/login.scss";
</style>
