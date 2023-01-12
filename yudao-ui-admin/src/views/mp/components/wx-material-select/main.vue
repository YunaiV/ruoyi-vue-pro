<!--
  - Copyright (C) 2018-2019
  - All rights reserved, Designed By www.joolun.com
-->
<template>
  <!-- 类型：图片 -->
  <div v-if="objData.type === 'image'">
    <div class="waterfall" v-loading="loading">
      <div class="waterfall-item" v-for="item in list" :key="item.mediaId">
        <img class="material-img" :src="item.url">
        <p class="item-name">{{item.name}}</p>
        <el-row class="ope-row">
          <el-button size="mini" type="success" @click="selectMaterial(item)">选择
            <i class="el-icon-circle-check el-icon--right"></i>
          </el-button>
        </el-row>
      </div>
    </div>
    <!-- 分页组件 -->
    <div v-if="list.length <= 0 && !loading" class="el-table__empty-block">
      <span class="el-table__empty-text">暂无数据</span>
    </div>
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getMaterialPage"/>
  </div>
  <!-- 类型：语音 -->
  <div v-else-if="objData.type === 'voice'">
    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" align="center" prop="mediaId" />
      <el-table-column label="文件名" align="center" prop="name" />
      <el-table-column label="语音" align="center">
        <template v-slot="scope">
          <wx-voice-player :url="scope.row.url" />
        </template>
      </el-table-column>
      <el-table-column label="上传时间" align="center" prop="createTime" width="180">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" fixed="right" class-name="small-padding fixed-width">
        <template v-slot="scope">
          <el-button size="mini" type="text" icon="el-icon-circle-plus"
                     @click="selectMaterial(scope.row)">选择</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getMaterialPage"/>
  </div>
  <div v-else-if="objData.type === 'video'">
    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" align="center" prop="mediaId" />
      <el-table-column label="文件名" align="center" prop="name" />
      <el-table-column label="标题" align="center" prop="title" />
      <el-table-column label="介绍" align="center" prop="introduction" />
      <el-table-column label="视频" align="center">
        <template v-slot="scope">
          <wx-video-player :url="scope.row.url" />
        </template>
      </el-table-column>
      <el-table-column label="上传时间" align="center" prop="createTime" width="180">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" fixed="right" class-name="small-padding fixed-width">
        <template v-slot="scope">
          <el-button size="mini" type="text" icon="el-icon-circle-plus"
                     @click="selectMaterial(scope.row)">选择</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getMaterialPage"/>
  </div>
  <div v-else-if="objData.type == 'news'">
    <div class="waterfall" v-loading="loading">
      <div class="waterfall-item" v-for="item in list" :key="item.mediaId" v-if="item.content && item.content.articles">
        <WxNews :objData="item.content.articles"></WxNews>
        <el-row class="ope-row">
          <el-button size="mini" type="success" @click="selectMaterial(item)">选择<i class="el-icon-circle-check el-icon--right"></i></el-button>
        </el-row>
      </div>
    </div>
    <div v-if="list.length <=0 && !loading" class="el-table__empty-block">
      <span class="el-table__empty-text">暂无数据</span>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-pagination
        @size-change="sizeChange"
        :current-page.sync="page.currentPage"
        :page-sizes="[10, 20]"
        :page-size="page.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="page.total"
        class="pagination"
      >
      </el-pagination>
    </span>
  </div>
</template>

<script>

  // import { tableOptionVoice } from '@/const/crud/wxmp/wxmaterial_voice'
  // import { tableOptionVideo } from '@/const/crud/wxmp/wxmaterial_video'
  import WxNews from '@/views/mp/components/wx-news/main.vue';
  import WxVoicePlayer from '@/views/mp/components/wx-voice-play/main.vue';
  import WxVideoPlayer from '@/views/mp/components/wx-video-play/main.vue';
  import { getMaterialPage } from "@/api/mp/material";
  // import {getPage as getPageNews} from '@/api/wxmp/wxfreepublish'
  // import {getPage as getPageNewsDraft} from '@/api/wxmp/wxdraft'

  export default {
    name: "wxMaterialSelect",
    components: {
      WxNews,
      WxVoicePlayer,
      WxVideoPlayer
    },
    props: {
      objData: {
        type: Object,
        required: true
      },
      //图文类型：1、已发布图文；2、草稿箱图文
      newsType:{
        type: String,
        default: "1"
      },
    },
    data() {
      return {
        // 遮罩层
        loading: false,
        // 总条数
        total: 0,
        // 数据列表
        list: [],
        // 查询参数
        queryParams: {
          pageNo: 1,
          pageSize: 10,
        },
        // tableOptionVoice: tableOptionVoice,
        // tableOptionVideo: tableOptionVideo,
      }
    },
    created() {
      this.getPage(this.page)
    },
    methods:{
      selectMaterial(item){
        this.$emit('selectMaterial', item)
      },
      getPage(page, params) {
        this.loading = true
        if(this.objData.type == 'news'){ // 【图文】
          if(this.newsType == '1'){
            getPageNews(Object.assign({
              current: page.currentPage,
              size: page.pageSize,
              appId:this.appId,
            }, params)).then(response => {
              let tableData = response.data.items
              tableData.forEach(item => {
                item.mediaId = item.articleId
                item.content.articles = item.content.newsItem
              })
              this.list = tableData
              this.page.total = response.data.totalCount
              this.page.currentPage = page.currentPage
              this.page.pageSize = page.pageSize
              this.loading = false
            })
          }else if(this.newsType == '2'){
            getPageNewsDraft(Object.assign({
              current: page.currentPage,
              size: page.pageSize,
              appId:this.appId,
            }, params)).then(response => {
              let tableData = response.data.items
              tableData.forEach(item => {
                item.mediaId = item.mediaId
                item.content.articles = item.content.newsItem
              })
              this.list = tableData
              this.page.total = response.data.totalCount
              this.page.currentPage = page.currentPage
              this.page.pageSize = page.pageSize
              this.loading = false
            })
          }
        } else { // 【素材】
          this.getMaterialPage();
        }
      },
      getMaterialPage() {
        getMaterialPage({
          ...this.queryParams,
          type: this.objData.type
        }).then(response => {
          this.list = response.data.list
          this.total = response.data.total
        }).finally(() => {
          this.loading = false
        })
      },
      sizeChange(val) {
        this.page.currentPage = 1
        this.page.pageSize = val
        this.getPage(this.page)
      },
      currentChange(val) {
        this.page.currentPage = val
        this.getPage(this.page)
      },
      /**
       * 刷新回调
       */
      refreshChange(page) {
        this.getPage(this.page)
      }
    }
  };
</script>

<style lang="scss" scoped>
  /*瀑布流样式*/
  .waterfall {
    width: 100%;
    column-gap:10px;
    column-count: 5;
    margin: 0 auto;
  }
  .waterfall-item {
    padding: 10px;
    margin-bottom: 10px;
    break-inside: avoid;
    border: 1px solid #eaeaea;
  }
  .material-img {
    width: 100%;
  }
  p {
    line-height: 30px;
  }
  @media (min-width: 992px) and (max-width: 1300px) {
    .waterfall {
      column-count: 3;
    }
    p {
      color:red;
    }
  }
  @media (min-width: 768px) and (max-width: 991px) {
    .waterfall {
      column-count: 2;
    }
    p {
      color: orange;
    }
  }
  @media (max-width: 767px) {
    .waterfall {
      column-count: 1;
    }
  }
  /*瀑布流样式*/
</style>
