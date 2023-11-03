<template>
  <div class="app-container">
    <el-form label-width="120px">
      <el-row type="flex" :gutter="0">
        <el-col :sm="12">
          <el-form-item label="WebSocket地址" size="small">
            <el-input v-model="url" type="text"/>
          </el-form-item>
        </el-col>
        <el-col :offset="1">
          <el-form-item label="" label-width="0px" size="small">
            <el-button @click="connect" type="primary" :disabled="ws&&ws.readyState===1">
              {{ ws && ws.readyState === 1 ? "已连接" : "连接" }}
            </el-button>
            <el-button @click="exit" type="danger">断开</el-button>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="发送内容" size="small">
        <el-input type="textarea" v-model="message" :rows="5"/>
      </el-form-item>
      <el-form-item label="" size="small">
        <el-button type="success" @click="send">发送消息</el-button>
      </el-form-item>
      <el-form-item label="接收内容" size="small">
        <el-input type="textarea" v-model="content" :rows="12" disabled/>
      </el-form-item>
      <el-form-item label="" size="small">
        <el-button type="info" @click="content=''">清空消息</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import store from "@/store";
import {getNowDateTime} from "@/utils/ruoyi";

export default {
  data() {
    return {
      url: process.env.VUE_APP_BASE_API + "/websocket/message",
      message: "",
      content: "",
      ws: null,
    };
  },
  created() {
    this.url = this.url.replace("http", "ws")
  },
  methods: {
    connect() {
      if (!'WebSocket' in window) {
        this.$modal.msgError("您的浏览器不支持WebSocket");
        return;
      }
      const userId = store.getters.userId;
      this.ws = new WebSocket(this.url + "?userId=" + userId);
      const self = this;
      this.ws.onopen = function (event) {
        self.content = self.content + "\n**********************连接开始**********************\n";
      };
      this.ws.onmessage = function (event) {
        self.content = self.content + "接收时间：" + getNowDateTime() + "\n" + event.data + "\n";
      };
      this.ws.onclose = function (event) {
        self.content = self.content + "**********************连接关闭**********************\n";
      };
      this.ws.error = function (event) {
        self.content = self.content + "**********************连接异常**********************\n";
      };
    },
    exit() {
      if (this.ws) {
        this.ws.close();
        this.ws = null;
      }
    },
    send() {
      if (!this.ws || this.ws.readyState !== 1) {
        this.$modal.msgError("未连接到服务器");
        return;
      }
      if (!this.message) {
        this.$modal.msgError("请输入发送内容");
        return;
      }
      this.ws.send(this.message);
    }
  },
};
</script>
