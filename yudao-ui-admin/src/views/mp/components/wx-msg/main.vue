<!--
  - Copyright (C) 2018-2019
  - All rights reserved, Designed By www.joolun.com
  芋道源码：
  ① 移除暂时用不到的 websocket
  ② 代码优化，补充注释，提升阅读性
-->
<template>
  <div class="msg-main">
    <div class="msg-div" :id="'msg-div' + nowStr">
      <!-- 加载更多 -->
      <div v-loading="tableLoading"></div>
      <div v-if="!tableLoading">
        <div class="el-table__empty-block" v-if="loadMore" @click="loadingMore"><span class="el-table__empty-text">点击加载更多</span></div>
        <div class="el-table__empty-block" v-if="!loadMore"><span class="el-table__empty-text">没有更多了</span></div>
      </div>
      <!-- 消息列表 -->
      <div class="execution" v-for="item in tableData" :key='item.id'>
        <div class="avue-comment" :class="item.sendFrom === 2 ? 'avue-comment--reverse' : ''">
          <div class="avatar-div">
            <img :src="item.sendFrom === 1 ? user.avatar : mp.avatar" class="avue-comment__avatar">
            <div class="avue-comment__author">{{item.sendFrom === 1 ? user.nickname : mp.nickname }}</div>
          </div>
          <div class="avue-comment__main">
            <div class="avue-comment__header">
              <div class="avue-comment__create_time">{{ parseTime(item.createTime) }}</div>
            </div>
            <div class="avue-comment__body" :style="item.sendFrom === 2 ? 'background: #6BED72;' : ''">
              <!-- 【事件】区域 -->
              <div v-if="item.type === 'event' && item.event === 'subscribe'">
                <el-tag type="success" size="mini">关注</el-tag>
              </div>
              <div v-else-if="item.type === 'event' && item.event === 'unsubscribe'">
                <el-tag type="danger" size="mini">取消关注</el-tag>
              </div>
              <div v-else-if="item.type === 'event' && item.event === 'CLICK'">
                <el-tag size="mini">点击菜单</el-tag>【{{ item.eventKey }}】
              </div>
              <div v-else-if="item.type === 'event' && item.event === 'VIEW'">
                <el-tag size="mini">点击菜单链接</el-tag>【{{ item.eventKey }}】
              </div>
              <div v-else-if="item.type === 'event' && item.event === 'scancode_waitmsg'"> <!-- TODO 芋艿：需要测试下 -->
                <el-tag size="mini">扫码结果</el-tag>【{{ item.eventKey }}】
              </div>
              <div v-else-if="item.type === 'event'">
                <el-tag type="danger" size="mini">未知事件类型</el-tag>
              </div>
              <!-- 【消息】区域 -->
              <div v-else-if="item.type === 'text'">{{ item.content }}</div>
              <div v-else-if="item.type === 'voice'">
                <wx-voice-player :url="item.mediaUrl" :content="item.recognition" />
              </div>
              <div v-else-if="item.type === 'image'">
                <a target="_blank" :href="item.mediaUrl">
                  <img :src="item.mediaUrl" style="width: 100px">
                </a>
              </div>
              <div v-else-if="item.type === 'video' || item.type === 'shortvideo'" style="text-align: center">
                <wx-video-player :url="item.mediaUrl" />
              </div>
              <div v-else-if="item.type === 'link'" class="avue-card__detail">
                <el-link type="success" :underline="false" target="_blank" :href="item.url">
                  <div class="avue-card__title"><i class="el-icon-link"></i>{{ item.title }}</div>
                </el-link>
                <div class="avue-card__info" style="height: unset">{{item.description}}</div>
              </div>
              <!-- TODO 芋艿：待完善 -->
              <div v-else-if="item.type === 'location'">
                <wx-location :label="item.label" :location-y="item.locationY" :location-x="item.locationX" />
              </div>
              <div v-else-if="item.type === 'news'" style="width: 300px"> <!-- TODO 芋艿：待测试；详情页也存在类似的情况 -->
                <wx-news :articles="item.articles" />
              </div>
              <div v-else-if="item.type === 'music'">
                <wx-music :title="item.title" :description="item.description" :thumb-media-url="item.thumbMediaUrl"
                  :music-url="item.musicUrl" :hq-music-url="item.hqMusicUrl" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="msg-send" v-loading="sendLoading">
      <wx-reply-select :objData="objData" />
      <el-button type="success" size="small" class="send-but" @click="sendMsg">发送(S)</el-button>
    </div>
  </div>
</template>

<script>
import {getMessagePage, sendMessage} from '@/api/mp/message'
  import WxReplySelect from '@/views/mp/components/wx-reply/main.vue'
  import WxVideoPlayer from '@/views/mp/components/wx-video-play/main.vue';
  import WxVoicePlayer from '@/views/mp/components/wx-voice-play/main.vue';
  import WxNews from '@/views/mp/components/wx-news/main.vue';
  import WxLocation from '@/views/mp/components/wx-location/main.vue';
  import WxMusic from '@/views/mp/components/wx-music/main.vue';
import {getUser} from "@/api/mp/user";

  export default {
    name: "wxMsg",
    components: {
      WxReplySelect,
      WxVideoPlayer,
      WxVoicePlayer,
      WxNews,
      WxLocation,
      WxMusic
    },
    props: {
      userId: {
        type: String,
        required: true
      },
    },
    data() {
      return {
        nowStr: new Date().getTime(), // 当前的时间戳，用于每次消息加载后，回到原位置；具体见 :id="'msg-div' + nowStr" 处
        sendLoading: false, // 发送消息是否加载中
        tableLoading: false, // 消息列表是否正在加载中
        loadMore: true, // 是否可以加载更多
        tableData: [], // 消息列表
        page: {
          pageNo: 1, // 当前页数
          pageSize: 14, // 每页显示多少条
        },
        user: { // 由于微信不再提供昵称，直接使用“用户”展示
          nickname: '用户',
          avatar: require("@/assets/images/profile.jpg"),
          accountId: 0, // 公众号账号编号
        },
        mp: {
          nickname: '公众号',
          avatar: require("@/assets/images/wechat.png"),
        },
        objData: { // 微信发送消息
          type: 'text',
        },
      }
    },
    created() {
      // 获得用户信息
      getUser(this.userId).then(response => {
        this.user.nickname = response.data.nickname | this.user.nickname;
        this.user.avatar = response.data.avatar | this.user.avatar;
        // 设置公众号账号编号
        this.objData.accountId = response.data.accountId;
      })

      // 加载消息
      this.refreshChange()
    },
    methods:{
      sendMsg(){
        if (!this.objData) {
          return;
        }
        if (this.objData.type === 'news') {
          this.objData.content.articles = [this.objData.content.articles[0]]
          this.$message({
            showClose: true,
            message: '图文消息条数限制在1条以内，已默认发送第一条',
            type: 'success'
          })
        }
        this.sendLoading = true
        sendMessage(Object.assign({
          userId: this.userId
        }, {
          ...this.objData,
          // content: this.objData.repContent,
          // TODO 芋艿：临时适配，保证可用
        })).then(response => {
          this.sendLoading = false
          // 添加到消息列表，并滚动
          this.tableData = [...this.tableData , ...[response.data] ]
          this.scrollToBottom()
          // 重置 objData 状态
          this.objData = {
            type: 'text',
            content: '',
          }
        }).catch(() => {
          this.sendLoading = false
        })
      },
      loadingMore() {
        this.page.pageNo++
        this.getPage(this.page)
      },
      getPage(page, params) {
        this.tableLoading = true
        getMessagePage(Object.assign({
          pageNo: page.pageNo,
          pageSize: page.pageSize,
          userId: this.userId
        }, params)).then(response => {
          // 计算当前的滚动高度
          const msgDiv = document.getElementById('msg-div' + this.nowStr);
          let scrollHeight = 0
          if(msgDiv){
            scrollHeight = msgDiv.scrollHeight
          }

          // 处理数据
          const data = response.data.list.reverse();
          this.tableData = [...data, ...this.tableData]
          this.tableLoading = false
          if (data.length < this.page.pageSize || data.length === 0){
            this.loadMore = false
          }
          this.page.pageNo = page.pageNo
          this.page.pageSize = page.pageSize

          // 滚动到原来的位置
          if(this.page.pageNo === 1) { // 定位到消息底部
            this.scrollToBottom()
          } else if (data.length !== 0) { // 定位滚动条
            this.$nextTick(() => {
              if (scrollHeight !== 0){
                msgDiv.scrollTop = document.getElementById('msg-div'+this.nowStr).scrollHeight - scrollHeight - 100
              }
            })
          }
        })
      },
      /**
       * 刷新回调
       */
      refreshChange() {
        this.getPage(this.page)
      },
      /** 定位到消息底部 */
      scrollToBottom: function () {
        this.$nextTick(() => {
          let div = document.getElementById('msg-div' + this.nowStr)
          div.scrollTop = div.scrollHeight
        })
      },
    }
  };
</script>
<style lang="scss" scoped>
/* 因为 joolun 实现依赖 avue 组件，该页面使用了 comment.scss、card.scc  */
@import './comment.scss';
@import './card.scss';

.msg-main {
  margin-top: -30px;
  padding: 10px;
}
.msg-div {
  height: 50vh;
  overflow: auto;
  background-color: #eaeaea;
}
.msg-send {
  padding: 10px;
}
.avatar-div {
  text-align: center;
  width: 80px;
}
.send-but {
  float: right;
  margin-top: 8px!important;
}
</style>

