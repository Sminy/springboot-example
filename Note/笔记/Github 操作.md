## Github 操作

### 常用词含义

- watch：会持续收到该项目的动态
- fork：复制其个项目到自己的Github仓库中
- star，可以理解为点赞
- clone，将项目下载至本地
- follow，关注你感兴趣的作者，会收到他们的动态



### in 搜索

in关键词限制搜索范围：

- 公式 ：xxx(关键词) in:name或description或readme

  - xxx in:name 项目名包含xxx的
  - xxx in:description 项目描述包含xxx的
  - xxx in:readme 项目的readme文件中包含xxx的组合使用

- 组合使用

  - 搜索项目名或者readme中包含秒杀的项目
  - xxx in:name,readme

  

### star 和 fork 范围搜索

- 公式：
  - xxx关键字 stars 通配符 :> 或者 :>=
  - 区间范围数字： stars:数字1…数字2
- 案例
  - 查找stars数大于等于5000的springboot项目：springboot stars:>=5000
  - 查找forks数在1000~2000之间的springboot项目：springboot forks:1000…5000
- 组合使用
  - 查找star大于1000，fork数在500到1000的springboot项目：springboot stars:>1000 forks:500…1000



### awesome搜索

- 公式：awesome 关键字：awesome系列，一般用来收集学习、工具、书籍类相关的项目
- 搜索优秀的redis相关的项目，包括框架，教程等 awesome redis



### #L 定位行位置

- 一行：地址后面紧跟 #L10
  - https://github.com/abc/abc/pom.xml#L13
- 多行：地址后面紧跟 #Ln1 - #Ln2
  - https://github.com/moxi624/abc/abc/pom.xml#L13-L30



### t 项目内搜索

在项目仓库下按键盘T，进行项目内搜索



![img](https://img-blog.csdnimg.cn/img_convert/7bbdf17bc8e0d9f19a9b11d85cfe639e.png)

### 搜索区域活跃用户

- location：地区
- language：语言
- 例如：location:beijing language:java