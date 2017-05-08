cordova-plugin-leanpush
========================

基于 LeanCloud 推送和统计的 Cordova 插件

## 安装


### 从源代码安装

```shell
cordova plugin add https://github.com/wujun4code/cordova-plugin-leanpush.git  --variable LEAN_APP_ID=<你的 App Id> --variable LEAN_APP_KEY=<你的 App Key> --save
```


在 `gulpfile.js` 里面添加如下代码：

```js
var leancloudInstaller = require('./plugins/cordova-plugin-leancloud/lpush-installer');

gulp.task('lpush-install', function(done){
    leancloudInstaller(__dirname, done);
});
```

然后安装如下 2 个组件

```shell
npm install --save-dev xml2js thunks && npm install
```

最后执行以下 gulp 任务：

```shell
gulp lpush-install
```

完成

## 使用

### 初始化

在 "deviceReady" 方法中初始化 LeanCloud(比如 $ionicPlatform.ready)

```js
window.LeanPush.init();
```

### 推送相关文档
Leancloud Push 开发指南](https://leancloud.cn/docs/ios_push_guide.html).

#### 初始化接口
```js
window.LeanPush.subscribe(channel, success, error)  // 订阅频道 channel :string 
window.LeanPush.unsubscribe(channel, success, error) //退订频道 channel :string
window.LeanPush.clearSubscription(success, error) //退订所有频道 
```

#### 注册 Installation 
一个 Installation 对象对应着一台设备，iOS 设备第一次启动 app 的时候会弹出提示，是否允许当前 app 使用推送，当用户点击 「允许」 之后，LeanCloud SDK 就会注册一个 iOS 设备推送的 DeviceToken 并且将它存储在 `_Installation` 表里。

```js
window.LeanPush.getInstallation(function(data){
      data = {        
          'deviceType':'android' or 'ios',
          'installationId': 'android installation id' or 'ios deviceToken'// `installationId` 是保存之后从服务端返回的当前设备对应的 installation 表里面的 `objectId`
          'deviceToken':    'ios deviceToken' or 'android installation id'
     }
}, function(error) {
}); 
```

#### 接受推送消息

```js
window.LeanPush.onNotificationReceived(function(data){
   data = {
       "alert":             "消息内容",
       "category":          "通知分类名称",
       "badge":             "未读消息数目",
       "sound":             "声音文件名",
       "content-available": "如果你在使用 Newsstand，设置为 1 来开始一次后台下载",
       "prevAppState": 'background' or 'foreground' or 'closed'
       // push到来的时候上一个App状态:
       // android只有 'background' 和 'closed', 因为android所有push都要点击
       // ios都有，因为ios如果app在前台，系统推送的alert不会出现
       // 用户没有任何操作，app就自动执行notification的函数不好, 可以加个判断
   };
}); 
```

假设服务端推送的格式如下：

```json
{
  "alert":             "消息内容",
  "category":          "通知分类名称",
  "badge":             "未读消息数目",
  "sound":             "声音文件名",
  "content-available": "如果你在使用 Newsstand，设置为 1 来开始一次后台下载"
}
```

客户端接收的格式如下：

```json
{
  "alert":             "消息内容",
  "category":          "通知分类名称",
  "badge":             "未读消息数目",
  "sound":             "声音文件名",
  "content-available": "如果你在使用 Newsstand，设置为 1 来开始一次后台下载"
  "prevAppState": 'background' or 'foreground' or 'closed'
}
```

注：针对 iOS 特殊的接收格式在接收之后做了解包处理，保证和 Android 接收的格式是一样的。因此在  `window.LeanPush.onNotificationReceived(callback)` 可以统一处理格式，无需再判断 deviceType 是 iOS 而做特殊处理。
    

```
$rootScope.$on('leancloud:notificationReceived', callback) // 如果你用了angular， 一个notification会在scope上broadcast这个event
// callback:
// function(event, notice){
//    // event is from angular, notice is same above 
// }
```

感谢 [Derek Hsu](https://github.com/Hybrid-Force) 



### 数据统计与分析 API

可以参考 [https://github.com/Hybrid-Force/cordova-plugin-leancloud](https://github.com/Hybrid-Force/cordova-plugin-leancloud).



- 关于统计部分的使用可以参考：[https://github.com/BenBBear/cordova-plugin-leanpush/blob/master/www/LeanAnalytics.js](https://github.com/BenBBear/cordova-plugin-leanpush/blob/master/www/LeanAnalytics.js) I

- 文档地址 [Leancloud documentation about leanAnalytics](https://leancloud.cn/docs/ios_statistics.html)

---

## 截屏效果图

### Android
![](./img/android.gif)

### IOS

See the [Attention Below](#attention), the webview can't `alert` when `onResume`

#### One

- notice from close
- notice while foreground

![](./img/ios.gif)

#### Two

- notice from background

##### mobile

![](./img/ios-back-phone.gif)


## 行为

The `onNotificationReceived callback`  and the `$rootScope.$emit('leancloud:notificationReceived')` will fires when

### IOS

- app in the foreground, notice comes (won't show the system notification alert)
- app in the background, tap the notification to resume it
- app closed, tap the notification to open it

### Android


- app in the foreground, tap the notification to see it
- app in the background, tap the notification to resume it
- app closed, tap the notification to open it



## 注意

### Android Quirk

请确保一定要先执行 gulp 任务： lpush_installer.js


#### 卸载

执行如下脚本：

```shell
cordova plugin rm cordova-plugin-leanpush
ionic platform rm android && ionic platform rm ios
ionic platform add android && ionic platform add ios
```

### 通知处理

以下两种方法都可以获取通知：

- `onNotificationReceived`

- `$rootScope.$emit('leancloud:notificationReceived')`


You can choose one of them, but may not both.


###  Android 编译常见问题

- **Error: duplicate files during packaging of APK**

在 `platforms/android/build.gradle` 找到 `android` 标签，然后添加如下内容：

```groovy
 packagingOptions {
       exclude 'META-INF/LICENSE.txt'
 	   exclude 'META-INF/NOTICE.txt'
}
```

使其内容如下：

```groovy
android{
   packagingOptions {
       exclude 'META-INF/LICENSE.txt'
 	   exclude 'META-INF/NOTICE.txt'
   }
    //其他 gradle 的设置
}
```

## LICENSE

The MIT License (MIT)

Copyright (c) 2015 Xinyu Zhang, Derek Hsu,Wu Jun
