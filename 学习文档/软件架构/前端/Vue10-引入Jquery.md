webpack.base.conf.js：

```js
module.exports = {
  //...
  resolve: {
    extensions: ['.js', '.vue', '.json'],
    alias: {
      '@': resolve('src'),
      'jquery': resolve('static/jquery.min.js')
    }
  },
  //...
}
```

引入：

```js
import * as $ from "jquery";
```

