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
          <el-tabs class="form" style=" float:none;">
            <el-tab-pane label="三方授权" name="uname">
            </el-tab-pane>
          </el-tabs>
          <div>
            <el-form ref="loginForm" :model="loginForm" :rules="LoginRules" class="login-form">
              <el-form-item prop="tenantName" v-if="tenantEnable">
                <el-input v-model="loginForm.tenantName" type="text" auto-complete="off" placeholder='租户'>
                  <svg-icon slot="prefix" icon-class="tree" class="el-input__icon input-icon"/>
                </el-input>
              </el-form-item>
              <!-- 账号密码登录 -->
              <div v-if="loginForm.loginType === 'uname'">
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
                </el-form-item>
              </div>

              <!-- 下方的登录按钮 -->
              <el-form-item style="width:100%;">
                <el-button :loading="loading" size="medium" type="primary" style="width:60%;"
                           @click.native.prevent="handleLogin">
                  <span v-if="!loading">同意授权</span>
                  <span v-else>登 录 中...</span>
                </el-button>
                <el-button size="medium" style="width:37%">拒绝</el-button>
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
import {getTenantIdByName} from "@/api/system/tenant";
import Cookies from "js-cookie";
import {SystemUserSocialTypeEnum} from "@/utils/constants";
import {getTenantEnable} from "@/utils/ruoyi";
import {authorize} from "@/api/login";

export default {
  name: "Login",
  data() {
    return {
      tenantEnable: true,
      loginForm: {
        tenantName: "芋道源码",
      },
      LoginRules: {
        tenantName: [
          {required: true, trigger: "blur", message: "租户不能为空"},
          {
            validator: (rule, value, callback) => {
              // debugger
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
        ]
      },
      loading: false,
      redirect: undefined,
      // 枚举
      SysUserSocialTypeEnum: SystemUserSocialTypeEnum,
    };
  },
  created() {
    // 租户开关
    this.tenantEnable = getTenantEnable();
    // 重定向地址
    this.redirect = this.$route.query.redirect;
    this.getCookie();
  },
  methods: {
    getCookie() {
      const tenantName = Cookies.get('tenantName');
      this.loginForm = {
        tenantName: tenantName === undefined ? this.loginForm.tenantName : tenantName
      };
    },
    handleLogin() {
      if (true) {
        authorize()
        return;
      }
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true;
          // 发起登陆
          console.log("发起登录", this.loginForm);
          this.$store.dispatch(this.loginForm.loginType === "sms" ? "SmsLogin" : "Login", this.loginForm).then(() => {
            this.$router.push({path: this.redirect || "/"}).catch(() => {
            });
          }).catch(() => {
            this.loading = false;
            this.getCode();
          });
        }
      });
    }
  }
};
</script>
<style lang="scss" scoped>
@import "~@/assets/styles/login.scss";
.oauth-login {
  display: flex;
  align-items: cen;
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
