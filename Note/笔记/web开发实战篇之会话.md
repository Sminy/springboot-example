# web 开发实战篇之会话

## 0. 开场介绍

大家好，我是xxx，属于平台开发组成员之一，主要从事后端Java开发，目前主要负责电子合同及电子收据开发工作。简单的介绍一下我的特点吧，引用贾平凹一句话“虽然灵魂有趣，有点倔强，也慢热，不善表达” ，接下来我要给大家分享的是《web 开发实战篇之会话》，欢迎大家多提宝贵意见，共同交流学习~



##  1.Http 协议

### 1.1 http 历史

1989年，爵士成功开发出世界上第一个Web服务器和第一个Web客户机，并为它命名为万维网（World Wide Web），即我们熟悉的www。

蒂姆·伯纳斯·李，是是“互联网之父”、“千年技术奖”首位获奖者，被英国女王封为爵士。图中是李爵士在2012年伦敦奥运开幕式中用自己熟悉的电脑敲出了“This is for Everyone”, 将万维网无偿向全世界开放，接受全球人民的掌声。

![image-20210225171441077](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20210225171441077.png)

![(C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20210222103417118.png)

![图片](https://mmbiz.qpic.cn/mmbiz_png/wAkAIFs11qY7jqh1paIzGeMDunokbQY5J5SfqTZarMWqH8SVnh1lSOffTzsYO0QEoiaDr1uVYsXBh1BLficTGBIg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



### 1.2 http 特点

- 简单快速: 客户向服务器请求服务时，只需传送请求方法和路径。请求方法常用的有GET、HEAD、POST。每种方法规定了客户与服务器联系的类型不同。由于HTTP协议简单，使得HTTP服务器的程序规模小，因而通信速度很快
- 灵活：HTTP允许传输任意类型的数据对象。正在传输的类型由Content-Type（Content-Type是HTTP包中用来表示内容类型的标识）加以标记
- 无连接：限无连接的含义是限制每次连接只处理一个请求。服务器处理完客户的请求，并收到客户的应答后，即断开连接。采用这种方式可以节省传输时间
- **无状态**：指协议对于事务处理没有记忆能力，服务器不知道客户端是什么状态
- 支持B/S及C/S模式



## 2. 会话技术

- cookie 

  1. cookie 存储在客户端： cookie 是服务器发送到用户浏览器并保存在本地的一小块数据，它会在浏览器下次向同一服务器再发起请求时被携带并发送到服务器上。
  2. cookie 是不可跨域的： 每个 cookie 都会绑定单一的域名，无法在别的域名下获取使用，一级域名和二级域名之间是允许共享使用的（靠的是 domain）。

- session

  1. session 是另一种记录服务器和客户端会话状态的机制

  2. session 是基于 cookie 实现的，session 存储在服务器端，sessionId 会被存储到客户端的cookie 中

  3. session认证流程

     a.用户第一次请求服务器的时候，服务器根据用户提交的相关信息，创建对应的 Session。

     b.请求返回时将此 Session 的唯一标识信息 SessionID 返回给浏览器。

     c.浏览器接收到服务器返回的 SessionID 信息后，会将此信息存入到 Cookie 中，同时 Cookie 记录此 SessionID 属于哪个域名。

     d.当用户第二次访问服务器的时候，请求会自动判断此域名下是否存在 Cookie 信息，如果存在自动将 Cookie 信息也发送给服务	端，服务端会从 Cookie 中获取 SessionID，再根据 SessionID 查找对应的 Session 信息，如果没有找到说明用户没有登录或	者登录失效，如果找到 Session 证明用户已经登录可执行后面操作。

  ![图片](https://mmbiz.qpic.cn/sz_mmbiz/HV4yTI6PjbKqDiaBefzHFx8KIHDrvbkH0D0OibrjPZTK9AJrZaDoTbGiaibibguZe4h8Xlv9MQsYS3F6cagcLp7NO7w/640?wx_fmt=other&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

- token

  1. 访问资源接口（API）时所需要的资源凭证

  2. 简单 token 的组成： uid(用户唯一的身份标识)、time(当前时间的时间戳)、sign（签名，token 的前几位以哈希算法压缩成的一定长度的十六进制字符串）

  3. 特点：

     a.服务端无状态化、可扩展性好

     b.支持移动端设备

     c.安全

     d.支持跨程序调用

  4. 认证流程

     ![图片](https://mmbiz.qpic.cn/sz_mmbiz/HV4yTI6PjbKqDiaBefzHFx8KIHDrvbkH0ZPDy6AOF2jIlrtDHCd1CDJmXYqcuBrZKveOGr0EtqzvfGFVtFiaPiacw/640?wx_fmt=other&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

     1. 客户端使用用户名跟密码请求登录
     2. 服务端收到请求，去验证用户名与密码
     3. 验证成功后，服务端会签发一个 token 并把这个 token 发送给客户端
     4. 客户端收到 token 以后，会把它存储起来，比如放在 cookie 里或者 localStorage 里
     5. 客户端每次向服务端请求资源的时候需要带着服务端签发的 token
     6. 服务端收到请求，然后去验证客户端请求里面带着的 token ，如果验证成功，就向客户端返回请求的数据
     7. 每一次请求都需要携带 token，需要把 token 放到 HTTP 的 Header 里
     8. 基于 token 的用户认证是一种服务端无状态的认证方式，服务端不用存放 token 数据。用解析 token 的计算时间换取 session 的存储空间，从而减轻服务器的压力，减少频繁的查询数据库
     9. token 完全由应用管理，所以它可以避开同源策略

- jwt（JSON Web Token）

  1. jwt 由 Header（头部）、Payload（负载）和 Signature（签名）三部分组成

     

  ![图片](https://mmbiz.qpic.cn/mmbiz_png/rSyd2cclv2eyg6YoWDSGUEHCfaibpicAoJhLiaSjibTg7Aor0icXNmtekSB3axWL4VI2ZExRRkuSzZwTbTpgYEyj6WQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

  

## 3.实战开发

鉴于我司目前开发模式而言，我们可以就以下两个方面来讲一下，会话保持在实际中的应用。

-  基于servlet的session机制
-  前后端分离



### 3.1  基于servlet的session机制



#### 		A. 单体应用

​	![image-20210223095834507](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20210223095834507.png)



#### 	B. tomcat 集群



![image-20210223090615233](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20210223090615233.png)

#### C. spring-session

spring-session的核心思想在于此：将session从web容器中剥离，存储在独立的存储服务器中。目前支持多种形式的session存储器：Redis、Database、MogonDB等。session的管理责任委托给spring-session承担。当request进入web容器，根据request获取session时，由spring-session负责存存储器中获取session，如果存在则返回，如果不存在则创建并持久化至存储器中。



**特点：**

​	spring-session在无需绑定web容器的情况下提供对集群session的支持。并提供对以下情况的透明集成：

- HttpSession：容许替换web容器的HttpSession
- WebSocket：使用WebSocket通信时，提供Session的活跃
- WebSession：容许以应用中立的方式替换webflux的webSession

**工作原理：**



![image-20210225093600574](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20210225093600574.png)

spring-session分为以下核心模块：

- SessionRepositoryFilter：Servlet规范中Filter的实现，用来切换HttpSession至Spring Session，包装HttpServletRequest和HttpServletResponse
- HttpServerletRequest/HttpServletResponse/HttpSessionWrapper包装器：包装原有的HttpServletRequest、HttpServletResponse和Spring Session，实现切换Session和透明继承HttpSession的关键之所在
- Session：Spring Session模块
- SessionRepository：管理Spring Session的模块
- HttpSessionStrategy：映射HttpRequst和HttpResponse到Session的策略



#### D. JWT

**格式：**

![image-20210225133737024](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20210225133737024.png)

**特点：**

- 优势：不向共享数据库查询，将所有必要的信息放在令牌内部，授权服务器可以通过令牌本身间接地与受保护资源沟通，而不需要调用任何网络API。
- 劣势：颁发的令牌无法撤回



#### E. Oauth2

**定义：**

OAuth（开放授权）是一个开放标准，允许用户授权第三方移动应用访问他们存储在另外的服务提供者上的信息，而不需要将用户名和密码提供给第三方移动应用或分享他们数据的所有内容，OAuth2.0是OAuth协议的延续版本，但不向后兼容OAuth 1.0即完全废止了OAuth1.0。





