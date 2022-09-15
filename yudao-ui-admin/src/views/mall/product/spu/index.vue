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

      <el-form-item label="商品编码" prop="code">
        <el-input v-model="queryParams.code" placeholder="请输入商品名称" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>

      <el-form-item label="商品分类" prop="categoryId">
        <el-cascader v-model="queryParams.categoryIds" placeholder="请输入商品分类"
                         :options="categoryList" :props="propName" clearable ref="category" />
      </el-form-item>

      <el-form-item label="商品品牌" prop="brandId">
        <el-select v-model="queryParams.brandId" placeholder="请输入商品品牌" clearable @keyup.enter.native="handleQuery">
          <el-option v-for="item in brandList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
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
      <el-table-column label="商品信息" align="center" width="260">

        <template slot-scope="scope" >
          <div class="product-info">
            <img
              v-if="scope.row.picUrls"
              :src="scope.row.picUrls[0]"
              alt="分类图片"
              class="img-height"
            />
            <div class="message">{{scope.row.name}}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="价格" align="center" prop="marketPrice" :formatter="unitConversion"/>
      <el-table-column label="库存" align="center" prop="totalStock" />
      <el-table-column label="销量" align="center" prop="salesCount" />
      <el-table-column label="排序" align="center" prop="sort" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status"/>
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
      <save @closeDialog="closeDialog" :obj="dialogObj" v-if="open" />
    </el-dialog>
  </div>
</template>

<script>
import {
  deleteSpu,
  getSpuPage,
} from "@/api/mall/product/spu";
import {getProductCategoryList} from "@/api/mall/product/category";
import {getBrandList} from "@/api/mall/product/brand";
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
      propName: {
        checkStrictly: true,
        label: "name",
        value: "id",
      },
      brandList: [],
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
      // 弹出层参数
      dialogObj:{},
      dateRangeCreateTime: [],
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        name: null,
        code: null,
        categoryIds: [],
        categoryId: null,
        brandId: null,
        status: null,
      },
      tagIndex: 0,
    };
  },
  created() {
    this.getList();
    this.getListCategory()
    this.getListBrand()
  },
  methods: {
    /** 查询分类列表 */
    getListCategory() {
      // 执行查询
      getProductCategoryList().then((response) => {
        this.categoryList = this.handleTree(response.data, "id", "parentId");
      });
    },
    /** 查询品牌列表 */
    getListBrand() {
      // 执行查询
      getBrandList().then((response) => {
        this.brandList = response.data;
      });
    },
    /** 查询列表 */
    getList() {
      this.loading = true;
      // 处理查询参数
      let params = { ...this.queryParams };
      params.categoryId =  this.queryParams.categoryIds[ this.queryParams.categoryIds.length - 1];
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
      this.$refs.category.$refs.panel.checkedValue = [];//也可以是指定的值，默认返回值是数组，也可以返回单个值
  　　this.$refs.category.$refs.panel.activePath = [];
  　　this.$refs.category.$refs.panel.syncActivePath();
      this.queryParams.categoryIds = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.dialogObj={};
      this.open = true;
      this.title = "添加商品spu";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.dialogObj.id = row.id;
      this.open = true;
      this.title = "修改商品spu";
    },
    closeDialog(){
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
    },
    unitConversion(row, column, cellValue){
      return this.divide(cellValue, 100);
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

  .product-info{
    display: flex;
    .img-height {
      height: 50px;
      width: 50px;
    }
    .message{
      margin-left: 10px;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      word-break: break-all;
      -webkit-box-orient: vertical;
      white-space: normal;
      overflow: hidden;
      height: 50px;
      line-height: 25px;
    }
  }

}
</style>
