<template>
  <el-table :data="socialUsers" :show-header="false">
    <el-table-column label="社交平台" align="left" width="120">
      <template v-slot="scope">
        <img style="height:20px;vertical-align: middle;" :src="scope.row.img" /> {{ scope.row.title }}
      </template>
    </el-table-column>
    <el-table-column label="操作" align="left" >
      <template v-slot="scope">
        <div v-if="scope.row.openid">
          已绑定
          <el-button size="large" type="text" @click="unbind(scope.row)">(解绑)</el-button>
        </div>
        <div v-else>
          未绑定
          <el-button size="large" type="text" @click="bind(scope.row)">(绑定)</el-button>
        </div>
      </template>
    </el-table-column>
  </el-table>
</template>

<script>

import {SystemUserSocialTypeEnum} from "@/utils/constants";
import {socialAuthRedirect} from "@/api/login";
import {socialBind, socialUnbind} from "@/api/system/socialUser";

export default {
  props: {
    user: {
      type: Object
    },
    getUser: { // 刷新用户
      type: Function
    },
    setActiveTab: { // 设置激活的
      type: Function
    }
  },
  data() {
    return {
    };
  },
  computed: {
    socialUsers (){
      const socialUsers = [];
      for (const i in SystemUserSocialTypeEnum) {
        const socialUser = {...SystemUserSocialTypeEnum[i]};
        socialUsers.push(socialUser);
        if (this.user.socialUsers) {
          for (const j in this.user.socialUsers) {
            if (socialUser.type === this.user.socialUsers[j].type) {
              socialUser.openid = this.user.socialUsers[j].openid;
              break;
            }
          }
        }
      }
      return socialUsers;
    }
  },
  created() {
    // 社交绑定
    const type = this.$route.query.type;
    const code = this.$route.query.code;
    const state = this.$route.query.state;
    if (!code) {
      return;
    }
    socialBind(type, code, state).then(resp => {
      this.$modal.msgSuccess("绑定成功");
      this.$router.replace('/user/profile');
      // 调用父组件, 刷新
      this.getUser();
      this.setActiveTab('userSocial');
    });
  },
  methods: {
    bind(socialUser) {
      // 计算 redirectUri
      const redirectUri = location.origin + '/user/profile?type=' + socialUser.type;
      // 进行跳转
      socialAuthRedirect(socialUser.type, encodeURIComponent(redirectUri)).then((res) => {
        // console.log(res.url);
        window.location.href = res.data;
      });
    },
    unbind(socialUser) {
      socialUnbind(socialUser.type, socialUser.openid).then(resp => {
        this.$modal.msgSuccess("解绑成功");
        socialUser.openid = undefined;
      });
    },
    close() {
      this.$tab.closePage();
    }
  }
};
</script>
