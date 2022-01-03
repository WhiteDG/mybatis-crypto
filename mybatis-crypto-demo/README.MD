# mybatis-crypto-demo

`mybatis-crypto-spring-boot-starter` 的演示项目

## 步骤

1. 克隆本项目到本地然后运行 `MybatisCryptoDemoApplication`
2. 访问 http://localhost:7070/swagger-ui/index.html#/
3. 调用 POST /users 接口保存一个用户信息
4. 访问 http://localhost:7070/h2-console/
   1. 账号、密码都是 sa
   2. 查看 user 表数据是否为加密后数据
5. 访问 GET /users 接口获取用户信息
   1. 返回的数据为解密后的数据
