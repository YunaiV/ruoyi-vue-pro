<!--
  - Copyright (C) 2018-2019
  - All rights reserved, Designed By www.joolun.com
  芋道源码：
  ① 移除 avue 组件，使用 ElementUI 原生组件
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
                @pagination="getPage"/>
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
  <div v-else-if="objData.type === 'news'">
    <div class="waterfall" v-loading="loading">
      <div class="waterfall-item" v-for="item in list" :key="item.mediaId" v-if="item.content && item.content.newsItem">
        <wx-news :articles="item.content.newsItem" />
        <el-row class="ope-row">
          <el-button size="mini" type="success" @click="selectMaterial(item)">
            选择<i class="el-icon-circle-check el-icon--right"></i>
          </el-button>
        </el-row>
      </div>
    </div>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getMaterialPage"/>
  </div>
</template>

<script>
import WxNews from '@/views/mp/components/wx-news/main.vue';
import WxVoicePlayer from '@/views/mp/components/wx-voice-play/main.vue';
import WxVideoPlayer from '@/views/mp/components/wx-video-play/main.vue';
import { getMaterialPage } from "@/api/mp/material";
import { getFreePublishPage } from "@/api/mp/freePublish";
import { getDraftPage } from "@/api/mp/draft";

export default {
  name: "wxMaterialSelect",
  components: {
    WxNews,
    WxVoicePlayer,
    WxVideoPlayer
  },
  props: {
    objData: {
      type: Object, // type - 类型；accountId - 公众号账号编号
      required: true
    },
    newsType:{ // 图文类型：1、已发布图文；2、草稿箱图文
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
        accountId: this.objData.accountId,
      },
    }
  },
  created() {
    this.getPage()
  },
  methods:{
    selectMaterial(item) {
      this.$emit('selectMaterial', item)
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNo = 1
      this.getPage()
    },
    getPage() {
      this.loading = true
      if (this.objData.type === 'news' && this.newsType === '1') { // 【图文】+ 【已发布】
        this.getFreePublishPage();
      } else if (this.objData.type === 'news' && this.newsType === '2') { // 【图文】+ 【草稿】
        this.getDraftPage();
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
    getFreePublishPage() {
      getFreePublishPage(this.queryParams).then(response => {
        // 将 thumbUrl 转成 picUrl，保证 wx-news 组件可以预览封面
        response.data.list.forEach(item => {
          const newsItem = item.content.newsItem;
          newsItem.forEach(article => {
            article.picUrl = article.thumbUrl;
          })
        })
        this.list = response.data.list
        this.total = response.data.total
      }).finally(() => {
        this.loading = false
      })
    },
    getDraftPage() {
      getDraftPage((this.queryParams)).then(response => {
        // 将 thumbUrl 转成 picUrl，保证 wx-news 组件可以预览封面
        response.data.list.forEach(item => {
          const newsItem = item.content.newsItem;
          newsItem.forEach(article => {
            article.picUrl = article.thumbUrl;
          })
        })
        this.list = response.data.list
        this.total = response.data.total
      }).finally(() => {
        this.loading = false
      })
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
