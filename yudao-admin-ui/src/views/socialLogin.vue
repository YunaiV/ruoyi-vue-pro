<template>
  <div class="login">
    <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
      <h3 class="title">绑定账号</h3>
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
      <el-form-item style="width:100%;">
        <el-button :loading="loading" size="medium" type="primary" style="width:100%;" @click.native.prevent="handleLogin">
          <span v-if="!loading">提 交</span>
          <span v-else>提 交 中...</span>
        </el-button>
      </el-form-item>

    </el-form>
    <!--  底部  -->
    <div class="el-login-footer">
      <span>Copyright © 2020-2021 iocoder.cn All Rights Reserved.</span>
    </div>
  </div>
</template>

<script>
import { socialLogin } from "@/api/login";
import Cookies from "js-cookie";
import { encrypt, decrypt } from '@/utils/jsencrypt'

export default {
  name: "ThirdLogin",
  data() {
    return {
      loginForm: {
        username: "admin",
        password: "admin123",
        rememberMe: false, // TODO 芋艿：后面看情况，去掉这块
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
    getCookie() {
      const username = Cookies.get("username");
      const password = Cookies.get("password");
      const rememberMe = Cookies.get('rememberMe')
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
      };
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true;
          if (this.loginForm.rememberMe) {
            Cookies.set("username", this.loginForm.username, { expires: 30 });
            Cookies.set("password", encrypt(this.loginForm.password), { expires: 30 });
          } else {
            Cookies.remove("username");
            Cookies.remove("password");
          }
          this.$store.dispatch("SocialLogin2", {
            code: this.code,
            state: this.state,
            type: this.type,
            username: this.loginForm.username,
            password: this.loginForm.password
          }).then(() => {
            this.$router.push({ path: this.redirect || "/" }).catch(()=>{});
          }).catch(() => {
            this.loading = false;
          });
        }
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
  width: 400px;
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
</style>
