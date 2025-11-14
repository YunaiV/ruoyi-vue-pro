export default {
  props: {
    localdata: {
      type: [Array, Object],
      default () {
        return []
      }
    },
    spaceInfo: {
      type: Object,
      default () {
        return {}
      }
    },
    collection: {
      type: String,
      default: ''
    },
    action: {
      type: String,
      default: ''
    },
    field: {
      type: String,
      default: ''
    },
    orderby: {
      type: String,
      default: ''
    },
    where: {
      type: [String, Object],
      default: ''
    },
    pageData: {
      type: String,
      default: 'add'
    },
    pageCurrent: {
      type: Number,
      default: 1
    },
    pageSize: {
      type: Number,
      default: 20
    },
    getcount: {
      type: [Boolean, String],
      default: false
    },
    getone: {
      type: [Boolean, String],
      default: false
    },
    gettree: {
      type: [Boolean, String],
      default: false
    },
    manual: {
      type: Boolean,
      default: false
    },
    value: {
      type: [Array, String, Number],
      default () {
        return []
      }
    },
    modelValue: {
      type: [Array, String, Number],
      default () {
        return []
      }
    },
    preload: {
      type: Boolean,
      default: false
    },
    stepSearh: {
      type: Boolean,
      default: true
    },
    selfField: {
      type: String,
      default: ''
    },
    parentField: {
      type: String,
      default: ''
    },
    multiple: {
      type: Boolean,
      default: false
    },
    map: {
      type: Object,
      default() {
        return {
          text: "text",
          value: "value"
        }
      }
    }
  },
  data() {
    return {
      loading: false,
      errorMessage: '',
      loadMore: {
        contentdown: '',
        contentrefresh: '',
        contentnomore: ''
      },
      dataList: [],
      selected: [],
      selectedIndex: 0,
      page: {
        current: this.pageCurrent,
        size: this.pageSize,
        count: 0
      }
    }
  },
  computed: {
    isLocaldata() {
      return !this.collection.length
    },
    postField() {
      let fields = [this.field];
      if (this.parentField) {
        fields.push(`${this.parentField} as parent_value`);
      }
      return fields.join(',');
    },
    dataValue() {
      let isModelValue = Array.isArray(this.modelValue) ? (this.modelValue.length > 0) : (this.modelValue !== null || this.modelValue !== undefined)
      return isModelValue ? this.modelValue : this.value
    },
    hasValue() {
      if (typeof this.dataValue === 'number') {
        return true
      }
      return (this.dataValue != null) && (this.dataValue.length > 0)
    }
  },
  created() {
    this.$watch(() => {
      var al = [];
      ['pageCurrent',
        'pageSize',
        'spaceInfo',
        'value',
        'modelValue',
        'localdata',
        'collection',
        'action',
        'field',
        'orderby',
        'where',
        'getont',
        'getcount',
        'gettree'
      ].forEach(key => {
        al.push(this[key])
      });
      return al
    }, (newValue, oldValue) => {
      let needReset = false
      for (let i = 2; i < newValue.length; i++) {
        if (newValue[i] != oldValue[i]) {
          needReset = true
          break
        }
      }
      if (newValue[0] != oldValue[0]) {
        this.page.current = this.pageCurrent
      }
      this.page.size = this.pageSize

      this.onPropsChange()
    })
    this._treeData = []
  },
  methods: {
    onPropsChange() {
      this._treeData = []
    },
    getCommand(options = {}) {
      /* eslint-disable no-undef */
      let db = uniCloud.database(this.spaceInfo)

      const action = options.action || this.action
      if (action) {
        db = db.action(action)
      }

      const collection = options.collection || this.collection
      db = db.collection(collection)

      const where = options.where || this.where
      if (!(!where || !Object.keys(where).length)) {
        db = db.where(where)
      }

      const field = options.field || this.field
      if (field) {
        db = db.field(field)
      }

      const orderby = options.orderby || this.orderby
      if (orderby) {
        db = db.orderBy(orderby)
      }

      const current = options.pageCurrent !== undefined ? options.pageCurrent : this.page.current
      const size = options.pageSize !== undefined ? options.pageSize : this.page.size
      const getCount = options.getcount !== undefined ? options.getcount : this.getcount
      const getTree = options.gettree !== undefined ? options.gettree : this.gettree

      const getOptions = {
        getCount,
        getTree
      }
      if (options.getTreePath) {
        getOptions.getTreePath = options.getTreePath
      }

      db = db.skip(size * (current - 1)).limit(size).get(getOptions)

      return db
    },
		getNodeData(callback) {
		  if (this.loading) {
		    return
		  }
		  this.loading = true
		  this.getCommand({
		    field: this.postField,
				where: this._pathWhere()
		  }).then((res) => {
		    this.loading = false
		    this.selected = res.result.data
		    callback && callback()
		  }).catch((err) => {
		    this.loading = false
		    this.errorMessage = err
		  })
		},
    getTreePath(callback) {
      if (this.loading) {
        return
      }
      this.loading = true

      this.getCommand({
        field: this.postField,
        getTreePath: {
          startWith: `${this.selfField}=='${this.dataValue}'`
        }
      }).then((res) => {
        this.loading = false
        let treePath = []
        this._extractTreePath(res.result.data, treePath)
        this.selected = treePath
        callback && callback()
      }).catch((err) => {
        this.loading = false
        this.errorMessage = err
      })
    },
    loadData() {
      if (this.isLocaldata) {
        this._processLocalData()
        return
      }

      if (this.dataValue != null) {
        this._loadNodeData((data) => {
          this._treeData = data
          this._updateBindData()
          this._updateSelected()
        })
        return
      }

      if (this.stepSearh) {
        this._loadNodeData((data) => {
          this._treeData = data
          this._updateBindData()
        })
      } else {
        this._loadAllData((data) => {
          this._treeData = []
          this._extractTree(data, this._treeData, null)
          this._updateBindData()
        })
      }
    },
    _loadAllData(callback) {
      if (this.loading) {
        return
      }
      this.loading = true

      this.getCommand({
        field: this.postField,
        gettree: true,
        startwith: `${this.selfField}=='${this.dataValue}'`
      }).then((res) => {
        this.loading = false
        callback(res.result.data)
        this.onDataChange()
      }).catch((err) => {
        this.loading = false
        this.errorMessage = err
      })
    },
    _loadNodeData(callback, pw) {
      if (this.loading) {
        return
      }
      this.loading = true

      this.getCommand({
        field: this.postField,
        where: pw || this._postWhere(),
        pageSize: 500
      }).then((res) => {
        this.loading = false
        callback(res.result.data)
        this.onDataChange()
      }).catch((err) => {
        this.loading = false
        this.errorMessage = err
      })
    },
    _pathWhere() {
      let result = []
      let where_field = this._getParentNameByField();
      if (where_field) {
        result.push(`${where_field} == '${this.dataValue}'`)
      }

      if (this.where) {
        return `(${this.where}) && (${result.join(' || ')})`
      }

      return result.join(' || ')
    },
    _postWhere() {
      let result = []
      let selected = this.selected
      let parentField = this.parentField
      if (parentField) {
        result.push(`${parentField} == null || ${parentField} == ""`)
      }
      if (selected.length) {
        for (var i = 0; i < selected.length - 1; i++) {
          result.push(`${parentField} == '${selected[i].value}'`)
        }
      }

      let where = []
      if (this.where) {
        where.push(`(${this.where})`)
      }
      if (result.length) {
        where.push(`(${result.join(' || ')})`)
      }

      return where.join(' && ')
    },
    _nodeWhere() {
      let result = []
      let selected = this.selected
      if (selected.length) {
        result.push(`${this.parentField} == '${selected[selected.length - 1].value}'`)
      }

      if (this.where) {
        return `(${this.where}) && (${result.join(' || ')})`
      }

      return result.join(' || ')
    },
    _getParentNameByField() {
      const fields = this.field.split(',');
      let where_field = null;
      for (let i = 0; i < fields.length; i++) {
        const items = fields[i].split('as');
        if (items.length < 2) {
          continue;
        }
        if (items[1].trim() === 'value') {
          where_field = items[0].trim();
          break;
        }
      }
      return where_field
    },
    _isTreeView() {
      return (this.parentField && this.selfField)
    },
    _updateSelected() {
      var dl = this.dataList
      var sl = this.selected
      let textField = this.map.text
      let valueField = this.map.value
      for (var i = 0; i < sl.length; i++) {
        var value = sl[i].value
        var dl2 = dl[i]
        for (var j = 0; j < dl2.length; j++) {
          var item2 = dl2[j]
          if (item2[valueField] === value) {
            sl[i].text = item2[textField]
            break
          }
        }
      }
    },
    _updateBindData(node) {
      const {
        dataList,
        hasNodes
      } = this._filterData(this._treeData, this.selected)

      let isleaf = this._stepSearh === false && !hasNodes

      if (node) {
        node.isleaf = isleaf
      }

      this.dataList = dataList
      this.selectedIndex = dataList.length - 1

      if (!isleaf && this.selected.length < dataList.length) {
        this.selected.push({
          value: null,
          text: "请选择"
        })
      }

      return {
        isleaf,
        hasNodes
      }
    },
    _filterData(data, paths) {
      let dataList = []
      let hasNodes = true

      dataList.push(data.filter((item) => {
        return (item.parent_value === null || item.parent_value === undefined || item.parent_value === '')
      }))
      for (let i = 0; i < paths.length; i++) {
        var value = paths[i].value
        var nodes = data.filter((item) => {
          return item.parent_value === value
        })

        if (nodes.length) {
          dataList.push(nodes)
        } else {
          hasNodes = false
        }
      }

      return {
        dataList,
        hasNodes
      }
    },
    _extractTree(nodes, result, parent_value) {
      let list = result || []
      let valueField = this.map.value
      for (let i = 0; i < nodes.length; i++) {
        let node = nodes[i]

        let child = {}
        for (let key in node) {
          if (key !== 'children') {
            child[key] = node[key]
          }
        }
        if (parent_value !== null && parent_value !== undefined && parent_value !== '') {
          child.parent_value = parent_value
        }
        result.push(child)

        let children = node.children
        if (children) {
          this._extractTree(children, result, node[valueField])
        }
      }
    },
    _extractTreePath(nodes, result) {
      let list = result || []
      for (let i = 0; i < nodes.length; i++) {
        let node = nodes[i]

        let child = {}
        for (let key in node) {
          if (key !== 'children') {
            child[key] = node[key]
          }
        }
        result.push(child)

        let children = node.children
        if (children) {
          this._extractTreePath(children, result)
        }
      }
    },
    _findNodePath(key, nodes, path = []) {
      let textField = this.map.text
      let valueField = this.map.value
      for (let i = 0; i < nodes.length; i++) {
        let node = nodes[i]
        let children = node.children
        let text = node[textField]
        let value = node[valueField]

        path.push({
          value,
          text
        })

        if (value === key) {
          return path
        }

        if (children) {
          const p = this._findNodePath(key, children, path)
          if (p.length) {
            return p
          }
        }

        path.pop()
      }
      return []
    },
    _processLocalData() {
      this._treeData = []
      this._extractTree(this.localdata, this._treeData)

      var inputValue = this.dataValue
      if (inputValue === undefined) {
        return
      }

      if (Array.isArray(inputValue)) {
        inputValue = inputValue[inputValue.length - 1]
        if (typeof inputValue === 'object' && inputValue[this.map.value]) {
          inputValue = inputValue[this.map.value]
        }
      }

      this.selected = this._findNodePath(inputValue, this.localdata)
    }
  }
}
