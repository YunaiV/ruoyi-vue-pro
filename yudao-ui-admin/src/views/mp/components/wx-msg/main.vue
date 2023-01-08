<!--
  - Copyright (C) 2018-2019
  - All rights reserved, Designed By www.joolun.com
-->
<template>
  <div class="msg-main" v-loading="mainLoading">
    <div class="msg-div" :id="'msg-div'+nowStr">
      <div v-loading="tableLoading"></div>
      <div v-if="!tableLoading">
        <div class="el-table__empty-block" v-if="loadMore" @click="loadingMore"><span class="el-table__empty-text">点击加载更多</span></div>
        <div class="el-table__empty-block" v-if="!loadMore"><span class="el-table__empty-text">没有更多了</span></div>
      </div>
      <div class="execution" v-for="item in tableData" :key='item.id'>
        <div class="avue-comment" :class="item.type == '2' ? 'avue-comment--reverse' : ''">
          <div class="avatar-div">
            <img :src="item.type == '1' ? item.headimgUrl : item.appLogo" class="avue-comment__avatar">
            <div class="avue-comment__author">{{item.type == '1' ? item.nickName : item.appName}}</div>
          </div>
          <div class="avue-comment__main">
            <div class="avue-comment__header">
              <div class="avue-comment__create_time">{{item.createTime}}</div>
            </div>
            <div class="avue-comment__body" :style="item.type == '2' ? 'background: #6BED72;' : ''">
              <div v-if="item.repType == 'event' && item.repEvent == 'subscribe'"><el-tag type="success" size="mini">关注</el-tag></div>
              <div v-if="item.repType == 'event' && item.repEvent == 'unsubscribe'"><el-tag type="danger" size="mini">取消关注</el-tag></div>
              <div v-if="item.repType == 'event' && item.repEvent == 'CLICK'"><el-tag size="mini">点击菜单</el-tag>：【{{item.repName}}】</div>
              <div v-if="item.repType == 'event' && item.repEvent == 'VIEW'"><el-tag size="mini">点击菜单链接</el-tag>：【{{item.repUrl}}】</div>
              <div v-if="item.repType == 'event' && item.repEvent == 'scancode_waitmsg'"><el-tag size="mini">扫码结果：</el-tag>：【{{item.repContent}}】</div>
              <div v-if="item.repType == 'text'">{{item.repContent}}</div>
              <div v-if="item.repType == 'image'">
                <a target="_blank" :href="item.repUrl"><img :src="item.repUrl" style="width: 100px"></a>
              </div>
              <div v-if="item.repType == 'voice'">
                <WxVoicePlayer :objData="item"></WxVoicePlayer>
              </div>
              <div v-if="item.repType == 'video'" style="text-align: center">
                <WxVideoPlayer :objData="item"></WxVideoPlayer>
              </div>
              <div v-if="item.repType == 'shortvideo'" style="text-align: center">
                <WxVideoPlayer :objData="item"></WxVideoPlayer>
              </div>
              <div v-if="item.repType == 'location'">
                <el-link type="primary" target="_blank" :href="'https://map.qq.com/?type=marker&isopeninfowin=1&markertype=1&pointx='+item.repLocationY+'&pointy='+item.repLocationX+'&name='+item.repContent+'&ref=joolun'">
                  <img :src="'https://apis.map.qq.com/ws/staticmap/v2/?zoom=10&markers=color:blue|label:A|'+item.repLocationX+','+item.repLocationY+'&key='+qqMapKey+'&size=250*180'">
                  <p/><i class="el-icon-map-location"></i>{{item.repContent}}
                </el-link>
              </div>
              <div v-if="item.repType == 'link'" class="avue-card__detail">
                <el-link type="success" :underline="false" target="_blank" :href="item.repUrl">
                  <div class="avue-card__title"><i class="el-icon-link"></i>{{item.repName}}</div>
                </el-link>
                <div class="avue-card__info" style="height: unset">{{item.repDesc}}</div>
              </div>
              <div v-if="item.repType == 'news'" style="width: 300px">
                <WxNews :objData="item.content.articles"></WxNews>
              </div>
              <div v-if="item.repType == 'music'">
                <el-link type="success" :underline="false" target="_blank" :href="item.repUrl">
                  <div class="avue-card__body" style="padding:10px;background-color: #fff;border-radius: 5px">
                    <div class="avue-card__avatar"><img :src="item.repThumbUrl" alt=""></div>
                    <div class="avue-card__detail">
                      <div class="avue-card__title" style="margin-bottom:unset">{{item.repName}}</div>
                      <div class="avue-card__info" style="height: unset">{{item.repDesc}}</div>
                    </div>
                  </div>
                </el-link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
<!--    <div class="msg-send" v-loading="sendLoading">-->
<!--      <WxReplySelect :objData="objData"></WxReplySelect>-->
<!--      <el-button type="success" size="small" class="send-but" @click="sendMsg">发送(S)</el-button>-->
<!--    </div>-->
  </div>
</template>

<script>
  import { getPage, addObj, putObj } from '@/api/wxmp/wxmsg'
  import SockJS from 'sockjs-client';
  import Stomp from 'stompjs';
  import { getToken } from '@/utils/auth'
  import WxReplySelect from '@/components/wx-reply/main.vue'
  import WxNews from '@/components/wx-news/main.vue'
  import WxVideoPlayer from '@/components/wx-video-play/main.vue'
  import WxVoicePlayer from '@/components/wx-voice-play/main.vue'

  export default {
    name: "wxMsg",
    components: {
      WxReplySelect,
      WxNews,
      WxVideoPlayer,
      WxVoicePlayer
    },
    props: {
      wxUserId:{
        type:String
      }
    },
    data() {
      return {
        nowStr: new Date().getTime(),
        objData:{
          repType: 'text'
        },
        mainLoading:false,
        sendLoading:false,
        tableLoading:false,
        loadMore: true,
        tableData: [],
        page: {
          total: 0, // 总页数
          currentPage: 1, // 当前页数
          pageSize: 14, // 每页显示多少条
          ascs:[],//升序字段
          descs:'create_time'//降序字段
        },
        option: {
          props: {
            avatar: 'avatar',
            author: 'author',
            body: 'body'
          }
        }
      }
    },
    created() {
      this.refreshChange()
    },
    destroyed: function() {
      this.disconnect()
    },
    mounted(){
      //开源版接口没有实现websocket，固隐藏
      // this.initWebSocket()
    },
    beforeDestroy() {
      // 页面离开时断开连接,清除定时器
      this.disconnect();
      clearInterval(this.timer);
    },
    computed: {

    },
    methods:{
      initWebSocket() {
        this.connection();
        let self = this;
        //断开重连机制,尝试发送消息,捕获异常发生时重连
        this.timer = setInterval(() => {
          try {
            self.stompClient.send("test");
          } catch (err) {
            console.log("断线了: " + err);
            self.connection();
          }
        }, 5000);
      },
      connection() {
        let token = getToken()
        let headers = {
          'Authorization': 'Bearer ' + token
        }
        // 建立连接对象
        this.socket = new SockJS('/weixin/ws');//连接服务端提供的通信接口，连接以后才可以订阅广播消息和个人消息
        // 获取STOMP子协议的客户端对象
        this.stompClient = Stomp.over(this.socket);

        // 向服务器发起websocket连接
        this.stompClient.connect(headers, () => {
          this.stompClient.subscribe('/weixin/wx_msg'+this.wxUserId, (msg) => { // 订阅服务端提供的某个topic;
            let data = JSON.parse(msg.body)
            this.tableData = [...this.tableData , ...[data]]//刷新消息
            this.scrollToBottom()
            //标记为已读
            if(data.type == '1'){
              putObj({
                id: data.id,
                readFlag: '0'
              }).then(data => {})
            }
          });
        }, (err) => {

        });
      },
      disconnect() {
        if (this.stompClient != null) {
          this.stompClient.disconnect();
          console.log("Disconnected");
        }
      },
      sendMsg(){
        if(this.objData){
          if(this.objData.repType == 'news'){
            this.objData.content.articles = [this.objData.content.articles[0]]
            this.$message({
              showClose: true,
              message: '图文消息条数限制在1条以内，已默认发送第一条',
              type: 'success'
            })
          }
          this.sendLoading = true
          addObj(Object.assign({
            wxUserId: this.wxUserId
          },this.objData)).then(data => {
            this.sendLoading = false
            data = data.data
            this.tableData = [...this.tableData , ...[data] ]
            this.scrollToBottom()
            this.objData = {
              repType: 'text'
            }
          }).catch(() => {
            this.sendLoading = false
          })
        }
      },
      scrollToBottom: function () {
        this.$nextTick(() => {
          let div = document.getElementById('msg-div'+this.nowStr)
          div.scrollTop = div.scrollHeight
        })
      },
      loadingMore(){
        this.page.currentPage++
        this.getPage(this.page)
      },
      getPage(page, params) {
        this.tableLoading = true
        getPage(Object.assign({
          current: page.currentPage,
          size: page.pageSize,
          descs:page.descs,
          ascs: page.ascs,
          wxUserId: this.wxUserId
        }, params)).then(response => {
          let msgDiv = document.getElementById('msg-div'+this.nowStr)
          let scrollHeight = 0
          if(msgDiv){
            scrollHeight = msgDiv.scrollHeight
          }
          let data = response.data.records.reverse()
          this.tableData = [...data , ...this.tableData]
          this.page.total = response.data.total
          this.tableLoading = false
          if(data.length < this.page.pageSize || data.length == 0){
            this.loadMore = false
          }
          if(this.page.currentPage == 1){//定位到消息底部
            this.scrollToBottom()
          }else{
            if(data.length != 0){
              this.$nextTick(() => {//定位滚动条
                if(scrollHeight != 0){
                  msgDiv.scrollTop = document.getElementById('msg-div'+this.nowStr).scrollHeight - scrollHeight - 100
                }
              })
            }
          }
          this.page.currentPage = page.currentPage
          this.page.pageSize = page.pageSize
        })
      },
      /**
       * 刷新回调
       */
      refreshChange() {
        this.getPage(this.page)
      }
    }
  };
</script>

<style lang="scss" scoped>
  .msg-main{
    margin-top: -30px;
    padding: 10px;
  }
  .msg-div{
    height: 50vh;
    overflow: auto;
    background-color: #eaeaea;
  }
  .msg-send{
    padding: 10px;
  }
  .avue-comment__main{
    flex: unset!important;
    border-radius: 5px!important;
    margin: 0 8px!important;
  }
  .avue-comment__header{
    border-top-left-radius: 5px;
    border-top-right-radius: 5px;
  }
  .avue-comment__body {
    border-bottom-right-radius: 5px;
    border-bottom-left-radius: 5px;
  }
  .avatar-div{
    text-align: center;
    width: 80px;
  }
  .send-but{
    float: right;
    margin-top: 8px!important;
  }
</style>

