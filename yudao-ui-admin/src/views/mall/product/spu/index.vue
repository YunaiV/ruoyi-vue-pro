<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="商品名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入商品名称" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="商品编码" prop="code">
        <el-input v-model="queryParams.code" placeholder="请输入商品编码" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="商品分类" prop="categoryIds">
        <el-cascader v-model="queryParams.categoryIds" placeholder="请输入商品分类"
                     :options="categoryList" :props="propName" clearable ref="category"/>
      </el-form-item>
      <el-form-item label="商品品牌" prop="brandId">
        <el-select v-model="queryParams.brandId" placeholder="请输入商品品牌" clearable @keyup.enter.native="handleQuery">
          <el-option v-for="item in brandList" :key="item.id" :label="item.name" :value="item.id"/>
        </el-select>
      </el-form-item>
      <!-- TODO 待实现：商品类型 -->
      <!-- TODO 待实现：商品标签 -->
      <!-- TODO 待实现：营销活动 -->
      <!-- TODO 前端优化：商品销量、商品价格，排的整齐一点 -->
      <el-form-item label="商品销量">
        <el-col :span="7" style="padding-left:0">
          <el-form-item prop="salesCountMin">
            <el-input v-model="queryParams.salesCountMin" placeholder="最低销量" clearable @keyup.enter.native="handleQuery"/>
          </el-form-item>
        </el-col>
        <el-col :span="1">-</el-col>
        <el-col :span="7" style="padding-left:0">
          <el-form-item prop="salesCountMax">
            <el-input v-model="queryParams.salesCountMax" placeholder="最高销量" clearable @keyup.enter.native="handleQuery"/>
          </el-form-item>
        </el-col>
      </el-form-item>
      <el-form-item label="商品价格" prop="code">
        <el-col :span="7" style="padding-left:0">
          <el-form-item prop="marketPriceMin">
            <el-input v-model="queryParams.marketPriceMin" placeholder="最低价格" clearable @keyup.enter.native="handleQuery"/>
          </el-form-item>
        </el-col>
        <el-col :span="1">-</el-col>
        <el-col :span="7" style="padding-left:0">
          <el-form-item prop="marketPriceMax">
            <el-input v-model="queryParams.marketPriceMax" placeholder="最高价格" clearable @keyup.enter.native="handleQuery"/>
          </el-form-item>
        </el-col>
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
                   v-hasPermi="['product:spu:create']">添加商品</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"/>
    </el-row>

    <el-tabs v-model="activeTabs" type="card" @tab-click="handleClick">
      <!-- 全部 -->
      <el-tab-pane label="全部" name="all">
        <!-- 列表 -->
        <el-table v-loading="loading" :data="list">
          <el-table-column label="商品信息" align="center" width="260">
            <template slot-scope="scope">
              <div class="product-info">
                <img v-if="scope.row.picUrls" :src="scope.row.picUrls[0]" alt="分类图片" class="img-height" />
                <div class="message">{{ scope.row.name }}</div>
              </div>
            </template>
            <!-- TODO 前端优化：可以有个 + 号，点击后，展示每个 sku -->
          </el-table-column>
          <el-table-column label="价格" align="center" prop="marketPrice" :formatter="formatPrice"/>
          <el-table-column label="库存" align="center" prop="totalStock"/>
          <el-table-column label="销量" align="center" prop="salesCount"/>
          <el-table-column label="排序" align="center" prop="sort"/>
          <el-table-column label="创建时间" align="center" prop="createTime" width="180">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" align="center" prop="status">
            <template slot-scope="scope">
              <dict-tag :type="DICT_TYPE.PRODUCT_SPU_STATUS" :value="scope.row.status"/>
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
      </el-tab-pane>

      <!-- 销售中 -->
      <el-tab-pane label="销售中" name="on">
        <!-- 列表 -->
        <el-table v-loading="loading" :data="list">
          <el-table-column label="商品信息" align="center" width="260">
            <template slot-scope="scope">
              <div class="product-info">
                <img v-if="scope.row.picUrls" :src="scope.row.picUrls[0]" alt="分类图片" class="img-height"/>
                <div class="message">{{ scope.row.name }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="价格" align="center" prop="marketPrice" :formatter="formatPrice"/>
          <el-table-column label="库存" align="center" prop="totalStock"/>
          <el-table-column label="销量" align="center" prop="salesCount"/>
          <el-table-column label="排序" align="center" prop="sort"/>
          <el-table-column label="创建时间" align="center" prop="createTime" width="180">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" align="center" prop="status">
            <template slot-scope="scope">
              <dict-tag :type="DICT_TYPE.PRODUCT_SPU_STATUS" :value="scope.row.status"/>
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
      </el-tab-pane>

      <!-- 仓库中 -->
      <el-tab-pane label="仓库中" name="off">
        <!-- 列表 -->
        <el-table v-loading="loading" :data="list">
          <el-table-column label="商品信息" align="center" width="260">
            <template slot-scope="scope">
              <div class="product-info">
                <img v-if="scope.row.picUrls" :src="scope.row.picUrls[0]" alt="分类图片" class="img-height"/>
                <div class="message">{{ scope.row.name }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="价格" align="center" prop="marketPrice" :formatter="formatPrice"/>
          <el-table-column label="库存" align="center" prop="totalStock"/>
          <el-table-column label="销量" align="center" prop="salesCount"/>
          <el-table-column label="排序" align="center" prop="sort"/>
          <el-table-column label="创建时间" align="center" prop="createTime" width="180">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" align="center" prop="status">
            <template slot-scope="scope">
              <dict-tag :type="DICT_TYPE.PRODUCT_SPU_STATUS" :value="scope.row.status"/>
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
      </el-tab-pane>

      <!-- 预警中 -->
      <el-tab-pane label="预警中" name="remind">
        <!-- 列表 -->
        <el-table v-loading="loading" :data="list">
          <el-table-column label="商品信息" align="center" width="260">
            <template slot-scope="scope">
              <div class="product-info">
                <img v-if="scope.row.picUrls" :src="scope.row.picUrls[0]" alt="分类图片" class="img-height"/>
                <div class="message">{{ scope.row.name }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="价格" align="center" prop="marketPrice" :formatter="formatPrice"/>
          <el-table-column label="库存" align="center" prop="totalStock"/>
          <el-table-column label="销量" align="center" prop="salesCount"/>
          <el-table-column label="排序" align="center" prop="sort"/>
          <el-table-column label="创建时间" align="center" prop="createTime" width="180">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" align="center" prop="status">
            <template slot-scope="scope">
              <dict-tag :type="DICT_TYPE.PRODUCT_SPU_STATUS" :value="scope.row.status"/>
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
      </el-tab-pane>
    </el-tabs>

    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>
  </div>
</template>

<script>
import {deleteSpu, getSpuPage,} from "@/api/mall/product/spu";
import {getProductCategoryList} from "@/api/mall/product/category";
import {getBrandList} from "@/api/mall/product/brand";
import {ProductSpuStatusEnum} from "@/utils/constants";

export default {
  name: "Spu",
  data() {
    return {
      activeTabs: "all",
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
        salesCountMin: null,
        salesCountMax: null,
        marketPriceMin: null,
        marketPriceMax: null,
      },
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
      let params = {...this.queryParams};
      params.marketPriceMin = this.queryParams.marketPriceMin === null ? null : params.marketPriceMin * 100;
      params.marketPriceMax = this.queryParams.marketPriceMax === null ? null : params.marketPriceMax * 100;
      params.categoryId = this.queryParams.categoryIds[this.queryParams.categoryIds.length - 1];
      this.addBeginAndEndTime(params, this.dateRangeCreateTime, "createTime");
      // 执行查询
      getSpuPage(params).then((response) => {
        this.list = response.data.list;
        this.total = response.data.total;
        this.loading = false;
      });
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
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.$router.push({ name: 'SpuAdd'})
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.$router.push({ name: 'SpuEdit', params: { spuId: row.id }})
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id;
      this.$modal
        .confirm('是否确认删除商品spu编号为"' + id + '"的数据项?')
        .then(function () {
          return deleteSpu(id);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch(() => {
        });
    },
    formatPrice(row, column, cellValue) {
      return '￥' + this.divide(cellValue, 100);
    },
    // 选中 tab
    handleClick(val) {
      if (val.name === "all") {
        this.queryParams.status = undefined;
        this.queryParams.alarmStock = undefined;
      } else if (val.name === "on") {
        this.queryParams.status = ProductSpuStatusEnum.ENABLE.status;
        this.queryParams.alarmStock = undefined;
      } else if (val.name === "off") {
        this.queryParams.status = ProductSpuStatusEnum.DISABLE.status;
        this.queryParams.alarmStock = undefined;
      } else if (val.name === "remind") {
        this.queryParams.status = undefined;
        this.queryParams.alarmStock = true;
      }
      this.getList();
    }
  },
};
</script>
<style lang="scss">
.app-container {
  .el-tag + .el-tag {
    margin-left: 10px;
  }

  .product-info {
    display: flex;

    .img-height {
      height: 50px;
      width: 50px;
    }

    .message {
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
