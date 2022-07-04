<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="商品名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入商品名称" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="卖点" prop="sellPoint">
        <el-input v-model="queryParams.sellPoint" placeholder="请输入卖点" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="分类id" prop="categoryId">
        <el-input v-model="queryParams.categoryId" placeholder="请输入分类id" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="价格(分)" prop="price">
        <el-input v-model="queryParams.price" placeholder="请输入价格 单位使用：分" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="库存数量" prop="quantity">
        <el-input v-model="queryParams.quantity" placeholder="请输入库存数量" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择上下架状态" clearable size="small">
          <el-option label="请选择字典生成" value=""/>
          <el-option label="上架" value="0"/>
          <el-option label="下架" value="1"/>
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker v-model="dateRangeCreateTime" style="width: 240px" value-format="yyyy-MM-dd"
                        type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"/>
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
                   v-hasPermi="['product:spu:create']">新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
                   :loading="exportLoading"
                   v-hasPermi="['product:spu:export']">导出
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="主键" align="center" prop="id"/>
      <el-table-column label="商品名称" align="center" prop="name"/>
<!--      <el-table-column label="卖点" align="center" prop="sellPoint"/>-->
<!--      <el-table-column label="描述" align="center" prop="description"/>-->
      <el-table-column label="分类id" align="center" prop="categoryId"/>
      <el-table-column label="商品主图地址" align="center" prop="picUrls">
        <template slot-scope="scope">
          <img v-if="scope.row.picUrls" :src="scope.row.picUrls[0]" alt="分类图片" class="img-height"/>
        </template>
      </el-table-column>
      <el-table-column label="排序字段" align="center" prop="sort"/>
      <el-table-column label="点赞初始人数" align="center" prop="likeCount"/>
      <el-table-column label="价格 (分)" align="center" prop="price"/>
      <el-table-column label="库存数量" align="center" prop="quantity"/>
<!--      <el-table-column label="状态" align="center" prop="status"/>-->
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['product:spu:update']">修改
          </el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['product:spu:delete']">删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="900px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称"/>
        </el-form-item>
        <el-form-item label="卖点" prop="sellPoint">
          <el-input v-model="form.sellPoint" placeholder="请输入卖点"/>
        </el-form-item>
        <el-form-item label="描述">
          <editor v-model="form.description" :min-height="192"/>
        </el-form-item>
        <el-form-item label="分类id" prop="categoryIds">
          <el-cascader
            v-model="form.categoryIds"
            placeholder="请输入分类id"
            style="width: 100%"
            :options="categoryList"
            :props="propName"
            clearable></el-cascader>
        </el-form-item>
        <el-form-item label="商品主图地址" prop="picUrls">
          <ImageUpload v-model="form.picUrls" :limit="10"/>
        </el-form-item>
        <el-form-item label="商品规格">
          <el-button size="mini" @click="shopTagInput()">添加规格</el-button>
          <div v-for="(tag, tagIndex) in skuTags" :key="tagIndex">
            <span>{{tag.name}}</span>
            <el-button style="margin-left: 10px" class="button-new-tag" type="text" icon="el-icon-delete"
                       @click="removeTag(tagIndex)">删除
            </el-button>
            <br/>
            <el-tag
              v-for="(tagItem, tagItemIndex) in tag.selectValues"
              :key="tagItem"
              style="margin-right: 10px"
              :disable-transitions="false">
              {{tagItem}}
            </el-tag>
            <!--            <el-input-->
            <!--              class="input-new-tag"-->
            <!--              v-if="tagItemInputs[tagIndex] && tagItemInputs[tagIndex].visible"-->
            <!--              v-model="tagItemInputs[tagIndex].value"-->
            <!--              :ref="`saveTagInput${tagIndex}`"-->
            <!--              size="small"-->
            <!--              @keyup.enter.native="handleInputConfirm(tagIndex)"-->
            <!--              @blur="handleInputConfirm(tagIndex)">-->
            <!--            </el-input>-->
          </div>
        </el-form-item>
        <el-form-item label="规格名" v-show="isShowTagInput">
          <el-col :span="8">
            <el-select v-model="addTagInput.name" filterable allow-create default-first-option placeholder="请选择"
                       @change="handleTagClick">
              <el-option
                v-for="item in unUseTags"
                :key="item.id"
                :label="item.name"
                :value="item.name">
              </el-option>
            </el-select>
          </el-col>
        </el-form-item>
        <el-form-item label="规格值" v-show="isShowTagInput">
          <el-col :span="8">
            <el-select v-model="addTagInput.selectValues" multiple filterable allow-create default-first-option
                       placeholder="请选择">
              <el-option
                v-for="item in dbTagValues"
                :key="item.id"
                :label="item.name"
                :value="item.name">
              </el-option>
            </el-select>
          </el-col>
        </el-form-item>
        <el-form-item>
          <el-button size="mini" type="primary" @click="addTag()" v-show="isShowTagInput">确定</el-button>
          <el-button size="mini" @click="hideTagInput()" v-show="isShowTagInput">取消</el-button>
        </el-form-item>
        <el-form-item v-if="form.skus.length>0">
          <el-table
            :data="form.skus"
            border
            style="width: 100%; margin-top: 20px"
            :span-method="tableSpanMethod">
            <el-table-column v-for="(leftTitle, index) in skuTags" :key="index" :label="leftTitle.name">
              <template slot-scope="scope">
                {{scope.row.propertyChildNames[index]}}
              </template>
            </el-table-column>
            <el-table-column v-if="skuTags.length"
                             prop="picUrl"
                             label="sku图片"
                             width="180">
              <template slot-scope="scope">
                <ImageUpload v-model="scope.row.picUrl" :limit="1">
                </ImageUpload>
              </template>
            </el-table-column>
            <el-table-column
              prop="prodName"
              label="条形码"
              width="250" v-if="skuTags.length">
              <template slot-scope="scope">
                <el-input v-model="scope.row.barCode" type="textarea" :disabled="scope.row.status==1"></el-input>
              </template>
            </el-table-column>
            <el-table-column
              prop="price"
              label="销售价">
              <template slot-scope="scope">
                <el-input-number
                  size="small"
                  v-model="scope.row.price"
                  controls-position="right"
                  :precision="2"
                  :max="1000000000"
                  :min="0.01"
                  :disabled="scope.row.status==1">
                </el-input-number>
              </template>
            </el-table-column>
            <el-table-column
              prop="oriPrice"
              label="成本价">
              <template slot-scope="scope">
                <el-input-number
                  size="small"
                  v-model="scope.row.costPrice"
                  controls-position="right"
                  :precision="2"
                  :max="1000000000"
                  :min="0.01"
                  :disabled="scope.row.status==1">
                </el-input-number>
              </template>
            </el-table-column>
            <el-table-column
              prop="oriPrice"
              label="原价">
              <template slot-scope="scope">
                <el-input-number
                  size="small"
                  v-model="scope.row.originalPrice"
                  controls-position="right"
                  :precision="2"
                  :max="1000000000"
                  :min="0.01"
                  :disabled="scope.row.status==1">
                </el-input-number>
              </template>
            </el-table-column>
            <el-table-column
              label="操作">
              <template slot-scope="scope">
                <el-button type="text" size="small" @click="changeSkuStatus(`${scope.$index}`)" v-if="scope.row.status===0">
                  正常
                </el-button>
                <el-button type="text" size="small" @click="changeSkuStatus(`${scope.$index}`)" v-else>已禁用</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
        <el-form-item label="排序字段" prop="sort">
          <el-input v-model="form.sort" placeholder="请输入排序字段"/>
        </el-form-item>
        <el-form-item label="点赞初始人数" prop="likeCount">
          <el-input v-model="form.likeCount" placeholder="请输入点赞初始人数"/>
        </el-form-item>
        <el-form-item label="价格 单位使用：分" prop="price">
          <el-input v-model="form.price" placeholder="请输入价格 单位使用：分"/>
        </el-form-item>
        <el-form-item label="库存数量" prop="quantity">
          <el-input v-model="form.quantity" placeholder="请输入库存数量"/>
        </el-form-item>
        <el-form-item label="上下架状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">上架</el-radio>
            <el-radio label="1">下架</el-radio>
          </el-radio-group>
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
    import {createSpu, updateSpu, deleteSpu, getSpu, getSpuPage, exportSpuExcel} from "@/api/mall/product/spu";
    import {
        createCategory,
        deleteCategory,
        exportCategoryExcel,
        getCategory,
        listCategory,
        updateCategory
    } from "@/api/mall/product/category";
    import {
        createProperty,
        updateProperty,
        deleteProperty,
        getProperty,
        getPropertyPage,
        exportPropertyExcel
    } from "@/api/mall/product/property";

    import Editor from '@/components/Editor';
    import ImageUpload from '@/components/ImageUpload';

    export default {
        name: "Spu",
        components: {
            Editor, ImageUpload
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
                    name: '',
                    propertyId: '',
                    selectValues: [],
                    selectValueIds: [],
                },
                skuTags: [],
                propName: {
                    checkStrictly: true,
                    label: 'name',
                    value: 'id'
                },
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
                // 表单参数
                form: {
                    id: undefined,
                    name: undefined,
                    sellPoint: undefined,
                    description: undefined,
                    categoryId: undefined,
                    categoryIds: [],
                    picUrls: undefined,
                    sort: undefined,
                    likeCount: undefined,
                    price: undefined,
                    quantity: undefined,
                    status: undefined,
                    isShowTagInput: undefined,
                    skus:[],
                },
                // 表单校验
                rules: {
                    sellPoint: [{required: true, message: "卖点不能为空", trigger: "blur"}],
                    description: [{required: true, message: "描述不能为空", trigger: "blur"}],
                    categoryIds: [{required: true, message: "分类id不能为空", trigger: "blur"}],
                    picUrls: [{required: true, message: "商品主图地址", trigger: "blur"}],
                    sort: [{required: true, message: "排序字段不能为空", trigger: "blur"}],
                },
                tagIndex:0,
            };
        },
        created() {

            this.getList();

        },
        methods: {
            getTableSpecData() {
                return this.value
            },
            tableSpanMethod({row, column, rowIndex, columnIndex}) {

            },
            changeSkuStatus(tagIndex) {
                if(this.form.skus[tagIndex].status == 0){
                    this.form.skus[tagIndex].status = 1 ;
                }else {
                    this.form.skus[tagIndex].status = 0 ;
                }

            },
            skuAddProdName() {
                if (this.initing) {
                    return
                }
                let skuList = []
                for (let i = 0; i < this.value.length; i++) {
                    const sku = Object.assign({}, this.value[i])
                    if (!sku.properties) {
                        return
                    }
                    sku.skuName = ''
                    let properties = sku.properties.split(';')
                    for (const propertiesKey in properties) {
                        sku.skuName += properties[propertiesKey].split(':')[1] + ' '
                    }
                    sku.prodName = this.prodName + ' ' + sku.skuName
                    skuList.push(sku)
                }
                this.$emit('input', skuList)
            },
            handleTagClose(tagIndex, tagItemIndex) {

            },
            //确定添加sku规格
            addTag() {

                let skus = this.unUseTags.map(function (item, index) {
                    return item.name
                });
                console.log("skus=="+JSON.stringify(skus))
                let index = skus.indexOf(this.addTagInput.name);
                console.log("index=="+index)
                console.log("skus[index].id=="+this.unUseTags[index].id)
                console.log("this.unUseTags[index].propertyValueList=="+JSON.stringify(this.unUseTags[index].propertyValueList))
                this.addTagInput.propertyId = this.unUseTags[index].id;
                for (let i = 0; i < this.addTagInput.selectValues.length; i++) {
                    for (let j = 0; j < this.unUseTags[index].propertyValueList.length; j++) {
                        if (this.addTagInput.selectValues[i] === this.unUseTags[index].propertyValueList[j].name) {
                            this.addTagInput.selectValueIds.push(this.unUseTags[index].propertyValueList[j].id)
                        }
                    }
                }
                let addTagInput = JSON.parse(JSON.stringify(this.addTagInput))
                console.log("addTagInput=="+JSON.stringify(addTagInput))
                this.skuTags.push(addTagInput);
                this.unUseTags.splice(index, 1);
                this.isShowTagInput = false;
                this.getTable();
            },
            getTable(){
                this.form.skus=[];
                let skuTags = JSON.parse(JSON.stringify(this.skuTags));
                let sku1s = [];
                let skuIds = [];
                let propertyIds = [];
                let propertyNames = [];
                for (let i = 0; i < skuTags.length; i++) {
                    sku1s.push(skuTags[i].selectValues);
                   skuIds.push(skuTags[i].selectValueIds);
                   propertyIds.push(skuTags[i].propertyId);
                   propertyNames.push(skuTags[i].name);
                }
                let skuAll = sku1s.reduce((x,y) =>{
                    let arr = [];
                    x.forEach(m => y.forEach(y => arr.push(m.concat([y]))))
                    return arr;
                },[[]])
                console.log(skuAll);

                let skuIdAll = skuIds.reduce((x,y) =>{
                    let arr = [];
                    x.forEach(m => y.forEach(y => arr.push(m.concat([y]))))
                    return arr;
                },[[]])
                console.log(skuIdAll);
                for (let i = 0; i < skuAll.length; i++) {
                    let han = {
                        propertyNames:propertyNames,
                        propertyIds:propertyIds,
                        propertyChildNames:skuAll[i],
                        propertyChildIds:skuIdAll[i],
                        properties: [],
                        picUrl: '',
                        costPrice: '',
                        originalPrice: '',
                        spuId: '',
                        prodName: '',
                        price: '',
                        barCode: '',
                        status: '0',
                    }
                    this.form.skus.push(han);
                }
                this.form.skus.forEach(x=>{
                    x.properties=[];
                    for (let i = 0; i <x.propertyIds.length ; i++) {
                        x.properties.push({
                            propertyId:x.propertyIds[i],
                            valueId:x.propertyChildIds[i]
                        })
                    }
                })

                console.log("this.skus=="+JSON.stringify(this.form.skus))
            },
            hideTagInput() {
                this.isShowTagInput = false;
                this.addTagInput = {
                    name: '',
                    propertyId: '',
                    selectValues: [],
                    selectValueIds: [],
                };
            },
            shopTagInput() {
                if (this.unUseTags.length <= 0) {
                    return this.$message.error("规格已经添加完毕")
                }
                this.isShowTagInput = true;
                this.addTagInput = {
                    name: '',
                    propertyId: '',
                    selectValues: [],
                    selectValueIds: [],
                };
            },
            //删除已选的规格
            removeTag(row) {
                let skus = this.allhistoryTags.map(function (item, index) {
                    return item.name
                })
                let index = skus.indexOf(this.skuTags[row].name);
                this.unUseTags.push(this.allhistoryTags[index]);
                this.skuTags.splice(row, 1);
                this.getTable();
            },
            handleTagClick(row) {
                for (let i = 0; i < this.propertyPageList.length; i++) {
                    if (row == this.propertyPageList[i].name) {
                        this.dbTagValues = this.propertyPageList[i].propertyValueList
                    }
                }
            },
            /** 查询规格 */
            getPropertyPageList() {
                // 执行查询
                getPropertyPage().then(response => {
                    this.propertyPageList = response.data.list;

                    this.unUseTags = this.propertyPageList.map(function (item, index) {
                        return item
                    })
                    this.allhistoryTags = JSON.parse(JSON.stringify(this.unUseTags));
                    console.log(this.propertyPageList)
                });
            },
            /** 查询分类 */
            getListCategory() {
                // 执行查询
                listCategory().then(response => {
                    this.categoryList = this.handleTree(response.data, "id", "parentId");

                });
            },
            /** 查询列表 */
            getList() {
                this.loading = true;
                // 处理查询参数
                let params = {...this.queryParams};
                this.addBeginAndEndTime(params, this.dateRangeCreateTime, 'createTime');
                // 执行查询
                getSpuPage(params).then(response => {
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
                    sellPoint: undefined,
                    description: undefined,
                    categoryId: undefined,
                    categoryIds: [],
                    picUrls: undefined,
                    sort: undefined,
                    likeCount: undefined,
                    price: undefined,
                    quantity: undefined,
                    status: undefined,
                    isShowTagInput: undefined,
                    skus:[],
                };
                this.skuTags=[];
                this.resetForm("form");
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
                this.reset();
                this.open = true;
                this.title = "添加商品spu";
                this.getListCategory();
                this.getPropertyPageList();
            },
            /** 修改按钮操作 */
            handleUpdate(row) {
                this.reset();
                const id = row.id;
                getSpu(id).then(response => {
                  console.log(">>>>>> response.data:" + JSON.stringify(response.data))
                    let dataSpu = response.data;
                    this.form = {
                        id: dataSpu.id,
                        name: dataSpu.name,
                        sellPoint: dataSpu.sellPoint,
                        description: dataSpu.sellPoint,
                        categoryId: dataSpu.sellPoint,
                        categoryIds: dataSpu.categoryIds,
                        picUrls: dataSpu.picUrls,
                        sort: dataSpu.sort,
                        likeCount: dataSpu.likeCount,
                        price: dataSpu.price,
                        quantity: dataSpu.quantity,
                        status: dataSpu.status,
                        isShowTagInput:undefined,
                        skus:dataSpu.skus
                        // skus:dataSpu.productSkuRespVOS,
                    };
                    this.open = true;
                    this.title = "修改商品spu";
                });
            },

            /** 提交按钮 */
            submitForm() {
                console.log(this.form.picUrls.split(','));

                this.$refs["form"].validate(valid => {
                    if (!valid) {
                        return;
                    }
                    this.form.picUrls = this.form.picUrls.split(',');
                    this.form.categoryId = this.form.categoryIds[(this.form.categoryIds.length - 1)];
                    this.form.status = Number(this.form.status);
                    // 修改的提交
                    if (this.form.id != null) {
                        updateSpu(this.form).then(response => {
                            this.$modal.msgSuccess("修改成功");
                            this.open = false;
                            this.getList();
                        });
                        return;
                    }
                    // 添加的提交
                    createSpu(this.form).then(response => {
                        this.$modal.msgSuccess("新增成功");
                        this.open = false;
                        this.getList();
                    });
                });
            },
            /** 删除按钮操作 */
            handleDelete(row) {
                const id = row.id;
                this.$modal.confirm('是否确认删除商品spu编号为"' + id + '"的数据项?').then(function () {
                    return deleteSpu(id);
                }).then(() => {
                    this.getList();
                    this.$modal.msgSuccess("删除成功");
                }).catch(() => {
                });
            },
            /** 导出按钮操作 */
            handleExport() {
                // 处理查询参数
                let params = {...this.queryParams};
                params.pageNo = undefined;
                params.pageSize = undefined;
                this.addBeginAndEndTime(params, this.dateRangeCreateTime, 'createTime');
                // 执行导出
                this.$modal.confirm('是否确认导出所有商品spu数据项?').then(() => {
                    this.exportLoading = true;
                    return exportSpuExcel(params);
                }).then(response => {
                    this.$download.excel(response, '商品spu.xls');
                    this.exportLoading = false;
                }).catch(() => {
                });
            }
        }
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
