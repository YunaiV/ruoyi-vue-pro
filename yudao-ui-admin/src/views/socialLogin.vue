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
              <el-form-item prop="code" v-if="captchaEnable">
                <el-input v-model="loginForm.code" auto-complete="off" placeholder="验证码" style="width: 63%"
                          @keyup.enter.native="handleLogin">
                  <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon"/>
                </el-input>
                <div class="login-code">
                  <img :src="codeUrl" @click="getCode" class="login-code-img"/>
                </div>
              </el-form-item>
              <el-checkbox v-model="loginForm.rememberMe" style="margin:0 0 25px 0;">记住密码</el-checkbox>
              <!-- 下方的登录按钮 -->
              <el-form-item style="width:100%;">
                <el-button :loading="loading" size="medium" type="primary" style="width:100%;"
                           @click.native.prevent="handleLogin">
                  <span v-if="!loading">登 录</span>
                  <span v-else>登 录 中...</span>
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>
    </div>
    <!-- footer -->
    <div class="footer">
      Copyright © 2020-2022 iocoder.cn All Rights Reserved.
    </div>
  </div>
</template>

<script>
import Cookies from "js-cookie";
import { encrypt, decrypt } from '@/utils/jsencrypt'
import {
  getPassword, getRememberMe,
  getUsername,
  removePassword,
  removeUsername,
  setPassword,
  setRememberMe,
  setUsername
} from "@/utils/auth";
import {getCodeImg} from "@/api/login";

export default {
  name: "ThirdLogin",
  data() {
    return {
      codeUrl: "",
      captchaEnable: true,
      loginForm: {
        loginType: "uname",
        username: "admin",
        password: "admin123",
        rememberMe: false,
        code: "",
        uuid: "",
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
  // watch: {
  //   $route: {
  //     handler: function(route) {
  //       this.redirect = route.query && route.query.redirect;
  //     },
  //     immediate: true
  //   }
  // },
  created() {
    this.getCookie();
    // 重定向地址
    this.redirect = this.$route.query.redirect;
    this.getCode();
    // 社交登录相关
    this.type = this.$route.query.type;
    this.code = this.$route.query.code;
    this.state = this.$route.query.state;
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
    handleLogin() {
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
            code: this.loginForm.code,
            uuid: this.loginForm.uuid,
          }).then(() => {
            this.$router.push({ path: this.redirect || "/" }).catch(()=>{});
          }).catch(() => {
            this.loading = false;
            this.getCode()
          });
        }
      });
    }
  }
};
</script>

<style rel="stylesheet/scss" lang="scss">
@import "~@/assets/styles/login.scss";
</style>
