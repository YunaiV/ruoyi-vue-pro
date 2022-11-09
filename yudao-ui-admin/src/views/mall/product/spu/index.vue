<template>
  <div class="app-container">
    <!-- 搜索工作栏 -->
    <!-- TODO @Luowenfeng：参考界面；https://v5.niuteam.cn/shop/goods/lists.html
      商品名称、商品编码、商品分类、商品品牌
      商品销量、商品价格
     -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="商品名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入商品名称" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="分类id" prop="categoryId">
        <el-input v-model="queryParams.categoryId" placeholder="请输入分类id" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择上下架状态" clearable size="small">
          <el-option label="请选择字典生成" value="" />
          <el-option label="上架" value="0" />
          <el-option label="下架" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker v-model="dateRangeCreateTime" style="width: 240px" value-format="yyyy-MM-dd" type="daterange"
          range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
                   v-hasPermi="['product:spu:create']">新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <!--
      TODO @Luowenfeng：参考界面；
       https://v5.niuteam.cn/shop/goods/lists.html
       1. 字段：商品信息、价格、库存、销量、排序、创建时间、状态、操作；
       2. tab 分成全部、销售中、仓库中、预警中
       -->
      <el-table-column label="主键" align="center" prop="id" />
      <el-table-column label="商品名称" align="center" prop="name" />
      <el-table-column label="分类id" align="center" prop="categoryId" />
      <el-table-column label="商品主图地址" align="center" prop="picUrls">
        <template slot-scope="scope">
          <img
            v-if="scope.row.picUrls"
            :src="scope.row.picUrls[0]"
            alt="分类图片"
            class="img-height"
          />
        </template>
      </el-table-column>
      <el-table-column label="排序字段" align="center" prop="sort" />
      <el-table-column label="点击量" align="center" prop="clickCount" />
      <el-table-column label="价格区间" align="center" prop="price" />
      <el-table-column label="总库存" align="center" prop="totalStock" />
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['product:spu:update']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['product:spu:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <el-dialog :title="title" :visible.sync="open" width="900px" append-to-body destroy-on-close :close-on-click-modal="false" >
      <save @closeDialog="closeDialog" :type="dialogType" :obj="dialogObj" v-if="open" />
    </el-dialog>
  </div>
</template>

<script>
import {
  deleteSpu,
  getSpuPage,
} from "@/api/mall/product/spu";

import Editor from "@/components/Editor";
import ImageUpload from "@/components/ImageUpload";
import save  from "./save";

// 1. TODO @Luowenfeng：商品的添加、修改，走一个单独的页面，不走弹窗；https://v5.niuteam.cn/shop/goods/addgoods.html
// 2. TODO

export default {
  name: "Spu",
  components: {
    Editor,
    ImageUpload,
    save,
  },
  data() {
    return {
      tableLeftTitles: [],
      dbTagValues: [],
      allhistoryTags: [],
      unUseTags: [],
      propertyPageList: [],
      isShowTagInput: false,
      addTagInput: {
        name: "",
        propertyId: "",
        selectValues: [],
        selectValueIds: [],
        selectObect: [],
      },
      skuTags: [],
      categoryList: [],
      // 遮罩层
      loading: true,
      // 导出遮罩层
      exportLoading: false,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 商品spu列表
      list: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 弹出层类型
      dialogType: "add",
      // 弹出层参数
      dialogObj:{},
      dateRangeCreateTime: [],
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        name: null,
        sellPoint: null,
        description: null,
        categoryId: null,
        picUrls: null,
        sort: null,
        likeCount: null,
        price: null,
        quantity: null,
        status: null,
      },
      tagIndex: 0,
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true;
      // 处理查询参数
      let params = { ...this.queryParams };
      this.addBeginAndEndTime(params, this.dateRangeCreateTime, "createTime");
      // 执行查询
      getSpuPage(params).then((response) => {
        response.data.list.forEach(element => {
          element.price = this.divide(element.minPrice, 100)+"~"+this.divide(element.maxPrice, 100)
        });
        this.list = response.data.list;
        this.total = response.data.total;
        this.loading = false;
      });
    },

    /** 取消按钮 */
    cancel() {
      this.open = false;
      this.reset();
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNo = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRangeCreateTime = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.dialogType = "add";
      this.dialogObj={};
      this.open = true;
      this.title = "添加商品spu";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.dialogType = "upd";
      this.dialogObj.id = row.id;
      this.open = true;
      console.log("修改")
      this.title = "修改商品spu";
    },
    closeDialog(){
      console.log("关闭")
      this.dialogType = "add";
      this.dialogObj={};
      this.open = false;
      this.getList()
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id;
      this.$modal
        .confirm('是否确认删除商品spu编号为"' + id + '"的数据项?')
        .then(function () {
          return deleteSpu(id);
        })
        .then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        })
        .catch(() => {});
    }
  },
};
</script>
<style lang="scss">
.app-container {
  .el-tag + .el-tag {
    margin-left: 10px;
  }

  .button-new-tag {
    margin-left: 10px;
    height: 32px;
    line-height: 30px;
    padding-top: 0;
    padding-bottom: 0;
  }

  .input-new-tag {
    width: 90px;
    margin-left: 10px;
    vertical-align: bottom;
  }

  .img-height {
    height: 65px;
  }
}
</style>
