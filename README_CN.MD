# mybatis-crypto

<a href="https://github.com/WhiteDG/mybatis-crypto/releases"><img alt="GitHub release" src="https://img.shields.io/github/release/WhiteDG/mybatis-crypto.svg?style=flat-square&label=latest version"/></a>
<img alt="GitHub last commit" src="https://img.shields.io/github/last-commit/WhiteDG/mybatis-crypto?style=flat-square">
<a href="https://github.com/WhiteDG/mybatis-crypto/actions"><img alt="GitHub Workflow Status" src="https://img.shields.io/github/actions/workflow/status/WhiteDG/mybatis-crypto/release.yml?style=flat-square"></a>
<br> <a href="README.MD">English</a> | 中文

## 简介

`mybatis-crypto` 是一个基于 mybatis 插件机制实现的字段加解密组件，通过一个注解即可对敏感数据进行加解密处理。支持自定义 `Encryptor`、特殊字段单独指定 `Encryptor` 和 `key`，满足大部分使用场景。

## 模块

`mybatis-crypto` 包括三个模块：

- `mybatis-crypto-core`  插件的核心功能模块
- `mybatis-crypto-spring-boot-starter` 提供了 `Spring boot` 快速整合功能
- `mybatis-crypto-encryptors` 提供了一些 IEncryptor 实现

## 使用方法

1. 引入依赖

```xml

<dependency>
    <groupId>io.github.whitedg</groupId>
    <artifactId>mybatis-crypto-spring-boot-starter</artifactId>
    <version>${latest.version}</version>
</dependency>
```

2. 实现 IEncryptor

```java
import io.github.whitedg.mybatis.crypto.IEncryptor;

public class MyEncryptor implements IEncryptor {

    @Override
    public String encrypt(Object val2bEncrypted, String key) throws Exception {
        // 实现这个方法返回加密后的数据
        return "encrypted string";
    }

    @Override
    public String decrypt(Object val2bDecrypted, String key) throws Exception {
        // 实现这个方法返回解密后的数据
        return "decrypted string";
    }
}
```

或者引入 `mybatis-crypto-encryptors`

```xml

<dependency>
    <groupId>io.github.whitedg</groupId>
    <artifactId>mybatis-crypto-encryptors</artifactId>
    <version>${latest.version}</version>
</dependency>
```

使用其提供的 `Encryptor`：

- `io.github.whitedg.mybatis.crypto.Base64Encryptor`
- `io.github.whitedg.mybatis.crypto.BasicTextEncryptor`
- `io.github.whitedg.mybatis.crypto.AES256Encryptor`
- `io.github.whitedg.mybatis.crypto.StrongTextEncryptor`

3. 添加配置

```yaml
mybatis-crypto:
  # 是否启用插件，默认 true
  enabled: true
  # 快速失败，默认 true
  fail-fast: false
  # 全局默认 Encryptor
  default-encryptor: io.github.whitedg.mybatis.crypto.BasicTextEncryptor
  # Encryptor 默认密钥
  default-key: global-key
  # mybatis @Param 注解下需要加解密的参数 key 前缀
  mapped-key-prefixes: et,encrypted
  # 是否启用加密字段全匹配查询，默认 false
  encrypted-query: true
  # 实体包路径
  type-packages: io.github.whitedg.**.entity
  # 是否保留明文参数
  keep-parameter: true
```

4. 指定加密字段

- 在需要加解密的字段上添加注解 `@EncryptedField`

```java
public class User {
    @EncryptedField
    private String encryptedStr;

    @EncryptedField(encryptor = YourEncryptor.class, key = "Your Key")
    private String customizedStr;
}
```

- 使用配置的 @Param 参数 key 前缀

```java
import org.apache.ibatis.annotations.Param;

interface YourEntityMapper {
    int insert(@Param("et") YourEntity entity);

    // 支持数组
    int batchInsert(@Param("encrypted-entities") List<YourEntity> entity);

    // 字符串参数支持使用 @EncryptedField
    // ⚠️只有一个参数的时候需要同时加上 @Param
    int selectByName0(@EncryptedField(encryptor = YourEncryptor.class, key = "Your Key") @Param("name") String name);

    // 字符串参数支持 @Param 前缀匹配(使用全局的 Encryptor 和 key)
    int selectByName1(@Param("encrypted-name") String name);

    // 返回值也支持单个对象或数组
    YourEntity selectOne();

    List<YourEntity> selectList();
}
```

## [Demo](https://github.com/WhiteDG/mybatis-crypto/blob/main/mybatis-crypto-demo/README.MD)

## 配置项说明

| 配置项                                | 说明                                                     | 默认值   |
|------------------------------------|--------------------------------------------------------|-------|
| mybatis-crypto.enabled             | 是否启用 mybatis-crypto                                    | true  |
| mybatis-crypto.fail-fast           | 快速失败，加解密过程中发生异常是否中断。true:抛出异常，false:使用原始值，打印 warn 级别日志 | true  |
| mybatis-crypto.mapped-key-prefixes | @Param 参数名的前缀，前缀匹配则会进行加密处理                             | 空     |
| mybatis-crypto.default-encryptor   | 全局默认 Encryptor                                         | 空     |
| mybatis-crypto.encrypted-query     | 是否启用加密字段全匹配查询                                          | false |
| mybatis-crypto.type-packages       | 实体的包路径，可选配置，配置后会在服务启动时提前将 Encryptor 加载到缓存中             | 空     |
| mybatis-crypto.keep-parameter      | 是否保留实体的明文参数，insert/update语句执行后实体的加密字段可以选择保留明文或者用密文覆盖   | false |

## LICENSE

```
Copyright 2021 WhiteDG

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
