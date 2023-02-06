<template>
  <div class="app-container">
    <doc-alert title="功能开启" url="https://doc.iocoder.cn/mall/build/" />

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="活动名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入活动名称" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="活动状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择活动状态" clearable size="small">
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.PROMOTION_ACTIVITY_STATUS)"
                       :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime">
        <el-date-picker v-model="queryParams.createTime" style="width: 240px" value-format="yyyy-MM-dd HH:mm:ss" type="daterange"
                        range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" :default-time="['00:00:00', '23:59:59']" />
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
                   v-hasPermi="['promotion:discount-activity:create']">新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="活动名称" align="center" prop="name" />
      <el-table-column label="活动时间" align="center" prop="startTime" width="240">
        <template v-slot="scope">
          <div>开始：{{ parseTime(scope.row.startTime) }}</div>
          <div>结束：{{ parseTime(scope.row.endTime) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template v-slot="scope">
          <dict-tag :type="DICT_TYPE.PROMOTION_ACTIVITY_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template v-slot="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-if="scope.row.status !== PromotionActivityStatusEnum.CLOSE.type"
                     v-hasPermi="['promotion:discount-activity:update']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleClose(scope.row)"
                     v-if="scope.row.status !== PromotionActivityStatusEnum.CLOSE.type &&
                            scope.row.status !== PromotionActivityStatusEnum.END.type"
                     v-hasPermi="['promotion:discount-activity:close']">关闭</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-if="scope.row.status === PromotionActivityStatusEnum.CLOSE.type"
                     v-hasPermi="['promotion:discount-activity:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="1000px" v-dialogDrag append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="活动名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input type="textarea" v-model="form.remark" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item label="活动时间" prop="startAndEndTime">
          <el-date-picker clearable v-model="form.startAndEndTime" type="datetimerange" :default-time="['00:00:00', '23:59:59']"
                          value-format="timestamp" placeholder="选择开始时间" style="width: 880px" />
        </el-form-item>
        <el-form-item label="商品选择">
          <el-select v-model="form.skuIds" placeholder="请选择活动商品" clearable size="small"
                     multiple filterable style="width: 880px" @change="changeFormSku">
            <el-option v-for="item in productSkus" :key="item.id" :label="item.spuName + ' ' + item.name" :value="item.id">
              <span style="float: left">{{ item.spuName }} &nbsp; {{ item.name}}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">￥{{ (item.price / 100.0).toFixed(2) }}</span>
            </el-option>
          </el-select>
          <el-table v-loading="loading" :data="form.products">
            <el-table-column label="商品名称" align="center" width="200">
              <template v-slot="scope">
                {{ scope.row.spuName }} &nbsp; {{ scope.row.name}}
              </template>
            </el-table-column>
            <el-table-column label="商品价格" align="center" prop="price">
              <template v-slot="scope">
                ￥{{ (scope.row.price / 100.0).toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column label="库存" align="center" prop="stock" />
            <el-table-column label="优惠类型" align="center" property="discountType">
              <template v-slot="scope">
                <el-select v-model="scope.row.discountType" placeholder="请选择优惠类型">
                  <el-option v-for="dict in getDictDatas(DICT_TYPE.PROMOTION_DISCOUNT_TYPE)"
                             :key="dict.value" :label="dict.label" :value="parseInt(dict.value)"/>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="优惠" align="center" prop="startTime" width="250">
              <template v-slot="scope">
                <el-form-item v-if="scope.row.discountType === PromotionDiscountTypeEnum.PRICE.type" prop="discountPrice">
                  减 <el-input-number v-model="scope.row.discountPrice" placeholder="请输入优惠金额"
                                      style="width: 190px" :precision="2" :min="0" :max="scope.row.price / 100.0 - 0.01" /> 元
                </el-form-item>
                <el-form-item v-if="scope.row.discountType === PromotionDiscountTypeEnum.PERCENT.type" prop="discountPercent">
                  打 <el-input-number v-model="scope.row.discountPercent" placeholder="请输入优惠折扣"
                                      style="width: 190px" :precision="1" :min="1" :max="9.9" /> 折
                </el-form-item>
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
              <template v-slot="scope">
                <el-button size="mini" type="text" icon="el-icon-delete" @click="removeFormSku(scope.row.skuId)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  createDiscountActivity,
  updateDiscountActivity,
  deleteDiscountActivity,
  getDiscountActivity,
  getDiscountActivityPage,
  closeDiscountActivity
} from "@/api/mall/promotion/discountActivity";
import {
  PromotionActivityStatusEnum, PromotionDiscountTypeEnum,
  PromotionProductScopeEnum
} from "@/utils/constants";
import { getSkuOptionList } from "@/api/mall/product/sku";
import { deepClone } from "@/utils";

export default {
  name: "DiscountActivity",
  components: {
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 导出遮罩层
      exportLoading: false,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 限时折扣活动列表
      list: [],
      // 弹出层名称
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        name: null,
        status: null,
        createTime: [],
      },
      // 表单参数
      form: {
        skuIds: [], // 选中的 SKU
        products: [], // 商品信息
      },
      // 表单校验
      rules: {
        name: [{ required: true, message: "活动名称不能为空", trigger: "blur" }],
        startAndEndTime: [{ required: true, message: "活动时间不能为空", trigger: "blur" }],
        skuIds: [{ required: true, message: "选择商品不能为空", trigger: "blur" }],
      },
      // 商品 SKU 列表
      productSkus: [],
      // 如下的变量，主要为了 v-if 判断可以使用到
      PromotionProductScopeEnum: PromotionProductScopeEnum,
      PromotionActivityStatusEnum: PromotionActivityStatusEnum,
      PromotionDiscountTypeEnum: PromotionDiscountTypeEnum,
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true;
      // 执行查询
      getDiscountActivityPage(this.queryParams).then(response => {
        this.list = response.data.list;
        this.total = response.data.total;
        this.loading = false;
      });
      // 获得 SKU 商品列表
      getSkuOptionList().then(response => {
        this.productSkus = response.data;
      });
    },
    /** 取消按钮 */
    cancel() {
      this.open = false;
      this.reset();
    },
    /** 表单重置 */
    reset() {
      this.form = {
        id: undefined,
        name: undefined,
        startAndEndTime: undefined,
        startTime: undefined,
        endTime: undefined,
        remark: undefined,
        skuIds: [],
        products: [],
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNo = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加限时折扣活动";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id;
      getDiscountActivity(id).then(response => {
        this.form = response.data;
        // 修改数据
        this.form.startAndEndTime = [response.data.startTime, response.data.endTime];
        this.form.skuIds = response.data.products.map(item => item.skuId);
        this.form.products.forEach(product => {
          // 获得对应的 SKU 信息
          const sku = this.productSkus.find(item => item.id === product.skuId);
          if (!sku) {
            return;
          }
          // 设置商品信息
          product.name = sku.name;
          product.spuName = sku.spuName;
          product.price = sku.price;
          product.stock = sku.stock;
          product.discountPrice = product.discountPrice !== undefined ? product.discountPrice / 100.0 : undefined;
          product.discountPercent = product.discountPercent !== undefined ? product.discountPercent / 10.0 : undefined;
        });
        // 打开弹窗
        this.open = true;
        this.title = "修改限时折扣活动";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) {
          return;
        }
        // 处理数据
        const data = deepClone(this.form); // 必须深拷贝，不然后面的 products 操作会有影响
        data.startTime = this.form.startAndEndTime[0];
        data.endTime = this.form.startAndEndTime[1];
        data.products.forEach(product => {
          product.discountPrice = product.discountPrice !== undefined ? product.discountPrice * 100 : undefined;
          product.discountPercent = product.discountPercent !== undefined ? product.discountPercent * 10 : undefined;
        });
        if (!valid) {
          return;
        }

        // 修改的提交
        if (this.form.id != null) {
          updateDiscountActivity(data).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
          return;
        }
        // 添加的提交
        createDiscountActivity(data).then(response => {
          this.$modal.msgSuccess("新增成功");
          this.open = false;
          this.getList();
        });
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id;
      this.$modal.confirm('是否确认删除限时折扣活动编号为"' + id + '"的数据项?').then(function() {
          return deleteDiscountActivity(id);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch(() => {});
    },
    /** 关闭按钮操作 */
    handleClose(row) {
      const id = row.id;
      this.$modal.confirm('是否确认关闭限时折扣活动编号为"' + id + '"的数据项?').then(function() {
        return closeDiscountActivity(id);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("关闭成功");
      }).catch(() => {});
    },
    /** 当 Form 的 SKU 发生变化时 */
    changeFormSku(skuIds) {
      // 处理【新增】
      skuIds.forEach(skuId => {
        // 获得对应的 SKU 信息
        const sku = this.productSkus.find(item => item.id === skuId);
        if (!sku) {
          return;
        }
        // 判断已存在，直接跳过
        const product = this.form.products.find(item => item.skuId === skuId);
        if (product) {
          return;
        }
        this.form.products.push({
          skuId: sku.id,
          name: sku.name,
          price: sku.price,
          stock: sku.stock,
          spuId: sku.spuId,
          spuName: sku.spuName,
          discountType: PromotionDiscountTypeEnum.PRICE.type,
        });
      });
      // 处理【移除】
      this.form.products.map((product, index) => {
        if (!skuIds.includes(product.skuId)) {
          this.form.products.splice(index, 1);
        }
      });
    },
    /** 移除 Form 的 SKU */
    removeFormSku(skuId) {
      this.form.skuIds.map((id, index) => {
        if (skuId === id) {
          this.form.skuIds.splice(index, 1);
        }
      });
      this.changeFormSku(this.form.skuIds);
    },
  }
}
</script>
