# 企业微信小程序开发过程

# 一.前置操作

## 1. 绑定微信小程序

- 访问`https://work.weixin.qq.com/wework_admin/frame#apps`
- 最下方`自建`中新建应用

## 2. 点击新建的小程序，然后点击应用主页的设置

## 3. 点击关联小程序

# 二. 功能开发

## 1. 参数获取

- `corpid`为`https://work.weixin.qq.com/wework_admin/frame#profile`最下方的企业ID
- `corpsecret`为新建应用的secret
- `appid`为微信小程序的APPID 或者 H5应用的AgentID

## 2. 获取企业微信token
* 发起鉴权的服务器的IP需要在`企业可信IP`中配置

