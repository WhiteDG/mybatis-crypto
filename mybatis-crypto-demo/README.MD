# mybatis-crypto-demo
<br> English | <a href="README_CN.MD">中文</a>

Demo project for `mybatis-crypto-spring-boot-starter`.

## Steps

1. Clone this project and run `MybatisCryptoDemoApplication`.
2. Access http://localhost:7070/swagger-ui/index.html#/
3. Call the POST /users API to create a user.
4. Access http://localhost:7070/h2-console/
   1. Both the username and password are "sa".
   2. Verify if the data in the user table is cipher.
5. Access the GET /users API to retrieve user information.
   1. The response data should be plain text.
