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
          <el-tabs class="form" style=" float:none;" value="uname">
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
                <el-button size="medium" style="width:36%">拒绝</el-button>
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
import {getTenantEnable} from "@/utils/ruoyi";
import {authorize, getAuthorize} from "@/api/login";
import {getTenantName, setTenantId} from "@/utils/auth";

export default {
  name: "Login",
  data() {
    return {
      tenantEnable: true,
      loginForm: {
        tenantName: "芋道源码",
      },
      params: { // URL 上的 client_id、scope 等参数
        responseType: undefined,
        clientId: undefined,
        redirectUri: undefined,
        state: undefined,
        scopes: [], // 优先从 query 参数获取；如果未传递，从后端获取
      },
      client: { // 客户端信息
        name: '',
        logo: '',
      },
      checkedScopes: [], // 已选中的 scope 数组
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
                  setTenantId(tenantId)
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
      //
    };
  },
  created() {
    // 租户开关
    this.tenantEnable = getTenantEnable();
    this.getCookie();

    // 解析参数
    // 例如说【自动授权不通过】：client_id=default&redirect_uri=https%3A%2F%2Fwww.iocoder.cn&response_type=code&scope=user.read%20user.write
    // 例如说【自动授权通过】：client_id=default&redirect_uri=https%3A%2F%2Fwww.iocoder.cn&response_type=code&scope=user.read
    this.params.responseType = this.$route.query.response_type
    this.params.clientId = this.$route.query.client_id
    this.params.redirectUri = this.$route.query.redirect_uri
    this.params.state = this.$route.query.state
    if (this.$route.query.scope) {
      this.params.scopes = this.$route.query.scope.split(' ')
    }

    // 如果有 scope 参数，先执行一次自动授权，看看是否之前都授权过了。
    if (this.params.scopes.length > 0) {
      this.doAuthorize(true, this.params.scopes, []).then(res => {
        const href = res.data
        if (!href) {
          console.log('自动授权未通过！')
          return;
        }
        location.href = href
      })
    }

    // 获取授权页的基本信息
    getAuthorize(this.params.clientId).then(res => {
      this.client = res.data.client
      // 解析 scope
      let scopes
      // 1.1 如果 params.scope 非空，则过滤下返回的 scopes
      if (this.params.scopes.length > 0) {
        scopes = []
        for (const scope of res.data.scopes) {
          if (this.params.scopes.indexOf(scope.key) >= 0) {
            scopes.push(scope)
          }
        }
      // 1.2 如果 params.scope 为空，则使用返回的 scopes 设置它
      } else {
        scopes = res.data.scopes
        for (const scope of scopes) {
          this.params.scopes.push(scope.key)
        }
      }
      // 生成已选中的 checkedScopes
      for (const scope of scopes) {
        if (scope.value) {
          this.checkedScopes.push(scope.key)
        }
      }
    })
  },
  methods: {
    getCookie() {
      const tenantName = getTenantName();
      this.loginForm = {
        tenantName: tenantName ? tenantName : this.loginForm.tenantName,
      };
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (!valid) {
          return
        }


      })
    },
    doAuthorize(autoApprove, checkedScopes, uncheckedScopes) {
      return authorize(this.params.responseType, this.params.clientId, this.params.redirectUri, this.params.state,
          autoApprove, checkedScopes, uncheckedScopes)
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
