<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="优惠劵名" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入优惠劵名" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable size="small">
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.COMMON_STATUS)"
                       :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="类型
     *
     * 1-优惠劵
     * 2-优惠码" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择类型
     *
     * 1-优惠劵
     * 2-优惠码" clearable size="small">
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.PRODUCT_SPU_STATUS)"
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
                   v-hasPermi="['promotion:coupon-template:create']">新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="优惠券名称" align="center" prop="name" />
      <el-table-column label="优惠券类型" align="center" prop="discountType">
        <template slot-scope="scope">
          <dict-tag :type="DICT_TYPE.PRODUCT_SPU_STATUS" :value="scope.row.type" />
        </template>
      </el-table-column>
      <el-table-column label="发行总量" align="center" prop="totalCount" />
      <el-table-column label="每人限领个数
     *
     * null - 则表示不限制" align="center" prop="takeLimitCount" />
      <el-table-column label="领取方式" align="center" prop="takeType" />
      <el-table-column label="是否设置满多少金额可用，单位：分" align="center" prop="usePrice" />
      <el-table-column label="商品范围" align="center" prop="productScope" />
      <el-table-column label="商品 SPU 编号的数组" align="center" prop="productSpuIds" />
      <el-table-column label="生效日期类型" align="center" prop="validityType" />
      <el-table-column label="固定日期-生效开始时间" align="center" prop="validStartTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.validStartTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="固定日期-生效结束时间" align="center" prop="validEndTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.validEndTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="领取日期-开始天数" align="center" prop="fixedStartTerm" />
      <el-table-column label="领取日期-结束天数" align="center" prop="fixedEndTerm" />
      <el-table-column label="折扣百分比" align="center" prop="discountPercent" />
      <el-table-column label="优惠金额，单位：分" align="center" prop="discountPrice" />
      <el-table-column label="折扣上限" />
      <el-table-column label="领取优惠券的数量" align="center" prop="takeNum" />
      <el-table-column label="使用优惠券的次数" align="center" prop="useCount" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="优惠券类型" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['promotion:coupon-template:update']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['promotion:coupon-template:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="500px" v-dialogDrag append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="优惠劵名" prop="name">
          <el-input v-model="form.name" placeholder="请输入优惠劵名" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in this.getDictDatas(DICT_TYPE.COMMON_STATUS)"
                      :key="dict.value" :label="parseInt(dict.value)">{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型">
            <el-option v-for="dict in this.getDictDatas(DICT_TYPE.PRODUCT_SPU_STATUS)"
                       :key="dict.value" :label="dict.label" :value="parseInt(dict.value)" />
          </el-select>
        </el-form-item>
        <el-form-item label="发行总量" prop="totalCount">
          <el-input v-model="form.totalCount" placeholder="请输入发行总量" />
        </el-form-item>
        <el-form-item label="每人限领个数" prop="takeLimitCount">
          <el-input v-model="form.takeLimitCount" placeholder="请输入每人限领个数" />
        </el-form-item>
        <el-form-item label="领取方式" prop="takeType">
          <el-select v-model="form.takeType" placeholder="请选择领取方式">
            <el-option label="请选择字典生成" value="" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否设置满多少金额可用，单位：分" prop="usePrice">
          <el-input v-model="form.usePrice" placeholder="请输入是否设置满多少金额可用，单位：分" />
        </el-form-item>
        <el-form-item label="商品范围" prop="productScope">
          <el-input v-model="form.productScope" placeholder="请输入商品范围" />
        </el-form-item>
        <el-form-item label="商品 SPU 编号的数组" prop="productSpuIds">
          <el-input v-model="form.productSpuIds" placeholder="请输入商品 SPU 编号的数组" />
        </el-form-item>
        <el-form-item label="生效日期类型" prop="validityType">
          <el-select v-model="form.validityType" placeholder="请选择生效日期类型">
            <el-option label="请选择字典生成" value="" />
          </el-select>
        </el-form-item>
        <el-form-item label="固定日期-生效开始时间" prop="validStartTime">
          <el-date-picker clearable v-model="form.validStartTime" type="date" value-format="timestamp" placeholder="选择固定日期-生效开始时间" />
        </el-form-item>
        <el-form-item label="固定日期-生效结束时间" prop="validEndTime">
          <el-date-picker clearable v-model="form.validEndTime" type="date" value-format="timestamp" placeholder="选择固定日期-生效结束时间" />
        </el-form-item>
        <el-form-item label="领取日期-开始天数" prop="fixedStartTerm">
          <el-input v-model="form.fixedStartTerm" placeholder="请输入领取日期-开始天数" />
        </el-form-item>
        <el-form-item label="领取日期-结束天数" prop="fixedEndTerm">
          <el-input v-model="form.fixedEndTerm" placeholder="请输入领取日期-结束天数" />
        </el-form-item>
        <el-form-item label="优惠类型" prop="discountType">
          <el-select v-model="form.discountType" placeholder="请选择优惠类型">
            <el-option label="请选择字典生成" value="" />
          </el-select>
        </el-form-item>
        <el-form-item label="折扣百分比" prop="discountPercent">
          <el-input v-model="form.discountPercent" placeholder="请输入折扣百分比" />
        </el-form-item>
        <el-form-item label="优惠金额，单位：分" prop="discountPrice">
          <el-input v-model="form.discountPrice" placeholder="请输入优惠金额，单位：分" />
        </el-form-item>
        <el-form-item label="折扣上限" prop="discountPriceLimit">
          <el-input v-model="form.discountPriceLimit" placeholder="请输入折扣上限，仅在 {@link #preferentialType} 等于 2 时生效" />
        </el-form-item>
        <el-form-item label="领取优惠券的数量" prop="takeNum">
          <el-input v-model="form.takeNum" placeholder="请输入领取优惠券的数量" />
        </el-form-item>
        <el-form-item label="使用优惠券的次数" prop="useCount">
          <el-input v-model="form.useCount" placeholder="请输入使用优惠券的次数" />
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
import { createCouponTemplate, updateCouponTemplate, deleteCouponTemplate, getCouponTemplate, getCouponTemplatePage, exportCouponTemplateExcel } from "@/api/promotion/couponTemplate";

export default {
  name: "CouponTemplate",
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
      // 优惠劵模板列表
      list: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        name: null,
        status: null,
        type: null,
        createTime: [],
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [{ required: true, message: "优惠劵名不能为空", trigger: "blur" }],
        status: [{ required: true, message: "状态不能为空", trigger: "blur" }],
        type: [{ required: true, message: "类型", trigger: "change" }],
        totalCount: [{ required: true, message: "发行总量不能为空", trigger: "blur" }],
        takeLimitCount: [{ required: true, message: "每人限领个数不能为空", trigger: "blur" }],
        takeType: [{ required: true, message: "领取方式不能为空", trigger: "change" }],
        usePrice: [{ required: true, message: "是否设置满多少金额可用，单位：分不能为空", trigger: "blur" }],
        productScope: [{ required: true, message: "商品范围不能为空", trigger: "blur" }],
        validityType: [{ required: true, message: "生效日期类型不能为空", trigger: "change" }],
        discountType: [{ required: true, message: "优惠类型不能为空", trigger: "change" }],
        takeNum: [{ required: true, message: "领取优惠券的数量不能为空", trigger: "blur" }],
        useCount: [{ required: true, message: "使用优惠券的次数不能为空", trigger: "blur" }],
      }
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
      getCouponTemplatePage(this.queryParams).then(response => {
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
    /** 表单重置 */
    reset() {
      this.form = {
        id: undefined,
        name: undefined,
        status: undefined,
        type: undefined,
        totalCount: undefined,
        takeLimitCount: undefined,
        takeType: undefined,
        usePrice: undefined,
        productScope: undefined,
        productSpuIds: undefined,
        validityType: undefined,
        validStartTime: undefined,
        validEndTime: undefined,
        fixedStartTerm: undefined,
        fixedEndTerm: undefined,
        discountType: undefined,
        discountPercent: undefined,
        discountPrice: undefined,
        discountPriceLimit: undefined,
        takeNum: undefined,
        useCount: undefined,
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
      this.title = "添加优惠劵模板";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id;
      getCouponTemplate(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改优惠劵模板";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) {
          return;
        }
        // 修改的提交
        if (this.form.id != null) {
          updateCouponTemplate(this.form).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
          return;
        }
        // 添加的提交
        createCouponTemplate(this.form).then(response => {
          this.$modal.msgSuccess("新增成功");
          this.open = false;
          this.getList();
        });
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id;
      this.$modal.confirm('是否确认删除优惠劵模板编号为"' + id + '"的数据项?').then(function() {
          return deleteCouponTemplate(id);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch(() => {});
    },
  }
};
</script>
