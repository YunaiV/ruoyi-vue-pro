# 移除代码

## 移除百度统计代码

在对应应用的 `index.html` 文件中，找到如下代码，删除即可：

```html
<!-- apps/web-antd -->
<script>
  var _hmt = _hmt || [];
  (function () {
    var hm = document.createElement('script');
    hm.src = 'https://hm.baidu.com/hm.js?d20a01273820422b6aa2ee41b6c9414d';
    var s = document.getElementsByTagName('script')[0];
    s.parentNode.insertBefore(hm, s);
  })();
</script>
```
