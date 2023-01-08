<!--
  - Copyright (C) 2018-2019
  - All rights reserved, Designed By www.joolun.com
-->
<template>
  <div class="msg-main" v-loading="mainLoading">
    <div class="msg-div" :id="'msg-div'+nowStr">
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
            <img :src="item.sendFrom === 1 ? item.headimgUrl : item.appLogo" class="avue-comment__avatar">
<!--            <div class="avue-comment__author">{{item.sendFrom === 1 ? item.nickName : item.appName}}</div>-->
            <div class="avue-comment__author">{{item.sendFrom === 1 ? '用户' : '公众号' }}</div>
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
              <div v-if="item.type === 'link'" class="avue-card__detail">
                <el-link type="success" :underline="false" target="_blank" :href="item.url">
                  <div class="avue-card__title"><i class="el-icon-link"></i>{{ item.title }}</div>
                </el-link>
                <div class="avue-card__info" style="height: unset">{{item.description}}</div>
              </div>
<!--              <div v-if="item.repType == 'location'">-->
<!--                <el-link type="primary" target="_blank" :href="'https://map.qq.com/?type=marker&isopeninfowin=1&markertype=1&pointx='+item.repLocationY+'&pointy='+item.repLocationX+'&name='+item.repContent+'&ref=joolun'">-->
<!--                  <img :src="'https://apis.map.qq.com/ws/staticmap/v2/?zoom=10&markers=color:blue|label:A|'+item.repLocationX+','+item.repLocationY+'&key='+qqMapKey+'&size=250*180'">-->
<!--                  <p/><i class="el-icon-map-location"></i>{{item.repContent}}-->
<!--                </el-link>-->
<!--              </div>-->

<!--              <div v-if="item.repType == 'news'" style="width: 300px">-->
<!--                <WxNews :objData="item.content.articles"></WxNews>-->
<!--              </div>-->

<!--              <div v-if="item.repType == 'music'">-->
<!--                <el-link type="success" :underline="false" target="_blank" :href="item.repUrl">-->
<!--                  <div class="avue-card__body" style="padding:10px;background-color: #fff;border-radius: 5px">-->
<!--                    <div class="avue-card__avatar"><img :src="item.repThumbUrl" alt=""></div>-->
<!--                    <div class="avue-card__detail">-->
<!--                      <div class="avue-card__title" style="margin-bottom:unset">{{item.repName}}</div>-->
<!--                      <div class="avue-card__info" style="height: unset">{{item.repDesc}}</div>-->
<!--                    </div>-->
<!--                  </div>-->
<!--                </el-link>-->
<!--              </div>-->
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
  import { getMessagePage } from '@/api/mp/message'
  // import WxReplySelect from '@/components/wx-reply/main.vue'
  // import WxNews from '@/components/wx-news/main.vue'
  import WxVideoPlayer from '@/views/mp/components/wx-video-play/main.vue';
  import WxVoicePlayer from '@/views/mp/components/wx-voice-play/main.vue';

  export default {
    name: "wxMsg",
    components: {
      // WxReplySelect,
      // WxNews,
      WxVideoPlayer,
      WxVoicePlayer
    },
    props: {
      wxUserId: {
        type: String
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
        tableData: [], // 消息列表
        page: {
          total: 0, // 总页数
          pageNo: 1, // 当前页数
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
    methods:{
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
        this.page.pageNo++
        this.getPage(this.page)
      },
      getPage(page, params) {
        this.tableLoading = true
        getMessagePage(Object.assign({
          pageNo: page.pageNo,
          pageSize: page.pageSize,
          descs:page.descs,
          ascs: page.ascs,
          wxUserId: this.wxUserId
        }, params)).then(response => {
          let msgDiv = document.getElementById('msg-div'+this.nowStr)
          let scrollHeight = 0
          if(msgDiv){
            scrollHeight = msgDiv.scrollHeight
          }
          let data = response.data.list.reverse()
          this.tableData = [...data , ...this.tableData]
          this.page.total = response.data.total
          this.tableLoading = false
          if(data.length < this.page.pageSize || data.length === 0){
            this.loadMore = false
          }
          if(this.page.pageNo == 1){//定位到消息底部
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
          this.page.pageNo = page.pageNo
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
/* 来自 https://github.com/nmxiaowei/avue/blob/master/styles/src/element-ui/comment.scss  */
/* 因为 joolun 实现依赖 avue 组件，该页面使用了 comment.scss  */
.avue-comment{
  margin-bottom: 30px;
  display: flex;
  align-items: flex-start;
  &--reverse{
    flex-direction:row-reverse;
    .avue-comment__main{
      &:before,&:after{
        left: auto;
        right: -8px;
        border-width: 8px 0 8px 8px;
      }
      &:before{
        border-left-color: #dedede;
      }
      &:after{
        border-left-color: #f8f8f8;
        margin-right: 1px;
        margin-left: auto;
      }
    }
  }
  &__avatar{
    width: 48px;
    height: 48px;
    border-radius: 50%;
    border: 1px solid transparent;
    box-sizing: border-box;
    vertical-align: middle;
  }
  &__header{
    padding: 5px 15px;
    background: #f8f8f8;
    border-bottom: 1px solid #eee;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
  &__author{
    font-weight: 700;
    font-size: 14px;
    color: #999;
  }
  &__main{
    flex:1;
    margin: 0 20px;
    position: relative;
    border: 1px solid #dedede;
    border-radius: 2px;
    &:before,&:after{
      position: absolute;
      top: 10px;
      left: -8px;
      right: 100%;
      width: 0;
      height: 0;
      display: block;
      content: " ";
      border-color: transparent;
      border-style: solid solid outset;
      border-width: 8px 8px 8px 0;
      pointer-events: none;
    }
    &:before {
      border-right-color: #dedede;
      z-index: 1;
    }
    &:after{
      border-right-color: #f8f8f8;
      margin-left: 1px;
      z-index: 2;
    }
  }
  &__body{
    padding: 15px;
    overflow: hidden;
    background: #fff;
    font-family: Segoe UI,Lucida Grande,Helvetica,Arial,Microsoft YaHei,FreeSans,Arimo,Droid Sans,wenquanyi micro hei,Hiragino Sans GB,Hiragino Sans GB W3,FontAwesome,sans-serif;color: #333;
    font-size: 14px;
  }
  blockquote{
    margin:0;
    font-family: Georgia,Times New Roman,Times,Kai,Kaiti SC,KaiTi,BiauKai,FontAwesome,serif;
    padding: 1px 0 1px 15px;
    border-left: 4px solid #ddd;
  }
}
</style>
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

