
ta4j包的目录 [0.13-SNAPSHOT.zip](https://github.com/yangyangchu1992/quant4j/files/5718998/0.13-SNAPSHOT.zip)
查看附件

 
## 功能
 主要用于创建和管理自己的机器人,后台动态查看运行状态和收益信息。 
# 一: 简单策略 基于配置条件达到自己的条件计算总体权重触发买入卖出
# 二: 指标策略 组装指标（RSI MACD ......等等 可以自由组合） 基于指标的值 触发买入卖出
```
 - 买入 / 卖出

- 策略中心
  - 简单策略
  - 指标策略
  - 策略列表

- 指标策略
  - 策略自定义添加
  - 策略回测（不完善）
  - 新增策略组
  - 修改策略组

- 简单策略
  - 各因子组合
  - 修改组合

- 托管中心
  - 添加机器人
  - 修改机器人
  - 查看机器人信息
  - 查看机器人下的订单信息
  - 查看机器人的盈利信息

- 配置中心
  - 火币api设置
  - 邮件提醒设置 下单后将推送邮件


```

## 开发

```bash
#后端
 springboot 

- [sql脚本](https://github.com/tokenIsme/images/blob/master/quant.sql)
- [前端ui](https://github.com/tokenIsme/quant-admin)

# 进入前端项目目录
cd quant-admin

# 安装依赖
npm install

# 建议不要直接使用 cnpm 安装依赖，会有各种诡异的 bug。可以通过如下操作解决 npm 下载速度慢的问题
npm install --registry=https://registry.npm.taobao.org

# 启动服务
npm run dev   
浏览器访问 http://localhost:9527


#后端
 - 后台由一个admin 控制中心 client 机器人运行节点 和一个注册中心管理 节点信息组成
   - 启动机器人由admin分发信息给相应的机器人节点，redis作为消息中间件，在client和admin之间传递信息。
   - 项目由springboot构建 运行admin,client,register三个项目 动态扩展的是client节点。
   - 进入admin模块配置好配置文件,run AdminApplication
   - 进入client模块配置好配置文件,这里主要配置的是redis和节点的外网ip和端口,以便让admin获取到节点的信息。 执行run ClientApplication
   - 进入register模块配置好配置文件,run RegisterApplication
```



