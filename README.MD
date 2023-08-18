# mybatis-crypto

<a href="https://github.com/WhiteDG/mybatis-crypto/releases"><img alt="GitHub release" src="https://img.shields.io/github/release/WhiteDG/mybatis-crypto.svg?style=flat-square&label=latest version"/></a>
<img alt="GitHub last commit" src="https://img.shields.io/github/last-commit/WhiteDG/mybatis-crypto?style=flat-square">
<a href="https://github.com/WhiteDG/mybatis-crypto/actions"><img alt="GitHub Workflow Status" src="https://img.shields.io/github/actions/workflow/status/WhiteDG/mybatis-crypto/release.yml?style=flat-square"></a>
<br> English | <a href="README_CN.MD">中文</a>

## Introduction

`mybatis-crypto` is a field encryption and decryption component based on the MyBatis plugin mechanism. It allows you to
encrypt and decrypt sensitive data with a simple annotation. It supports custom Encryptor, specifying separate Encryptor
and key for special fields, and meets most use cases.

## Modules

`mybatis-crypto` consists of three modules:

- `mybatis-crypto-core` The core functionality module of the plugin.
- `mybatis-crypto-spring-boot-starter` Provides quick integration with `Spring Boot`.
- `mybatis-crypto-encryptors` Provides several `IEncryptor` implementations.

## Usage

1. Add the dependency

```xml

<dependency>
    <groupId>io.github.whitedg</groupId>
    <artifactId>mybatis-crypto-spring-boot-starter</artifactId>
    <version>${latest.version}</version>
</dependency>
```

2. Implement the IEncryptor

```java
import io.github.whitedg.mybatis.crypto.IEncryptor;

public class MyEncryptor implements IEncryptor {

    @Override
    public String encrypt(Object val2bEncrypted, String key) throws Exception {
        // Implement this method to return the encrypted data
        return "encrypted string";
    }

    @Override
    public String decrypt(Object val2bDecrypted, String key) throws Exception {
        // Implement this method to return the decrypted data
        return "decrypted string";
    }
}
```

Alternatively, you can use `mybatis-crypto-encryptors`

```xml

<dependency>
    <groupId>io.github.whitedg</groupId>
    <artifactId>mybatis-crypto-encryptors</artifactId>
    <version>${latest.version}</version>
</dependency>
```

and use the provided `Encryptor` implementations:

- `io.github.whitedg.mybatis.crypto.Base64Encryptor`
- `io.github.whitedg.mybatis.crypto.BasicTextEncryptor`
- `io.github.whitedg.mybatis.crypto.AES256Encryptor`
- `io.github.whitedg.mybatis.crypto.StrongTextEncryptor`

3. Add configurations

```yaml
mybatis-crypto:
  # Enable the plugin (default: true)
  enabled: true
  # Fail fast during encryption/decryption (default: true)
  fail-fast: false
  # Global default Encryptor
  default-encryptor: io.github.whitedg.mybatis.crypto.BasicTextEncryptor
  # Default key for Encryptor
  default-key: global-key
  # Prefixes of @Param parameters that need to be encrypted
  mapped-key-prefixes: et,encrypted
  # Enable encrypted field query (default: false)
  encrypted-query: true
  # Entity package path
  type-packages: io.github.whitedg.**.entity
  # Keep plaintext parameters
  keep-parameter: true
```

4. Specify encrypted fields

- Add the `@EncryptedField` annotation to the fields that need to be encrypted/decrypted

```java
public class User {
    @EncryptedField
    private String encryptedStr;

    @EncryptedField(encryptor = YourEncryptor.class, key = "Your Key")
    private String customizedStr;
}
```

- Use the configured @Param parameter key prefix

```java
import org.apache.ibatis.annotations.Param;

interface YourEntityMapper {
    int insert(@Param("et") YourEntity entity);

    // Support for List
    int batchInsert(@Param("encrypted-entities") List<YourEntity> entity);

    // @EncryptedField can be applied to the string parameter
    // ⚠️If there is only one parameter, @Param is also required
    int selectByName0(@EncryptedField(encryptor = YourEncryptor.class, key = "Your Key") @Param("name") String name);

    // Support for string parameters using @Param key prefix(Uses the global encryptor and key)
    int selectByName1(@Param("encrypted-name") String name);

    // The result can be an object or a List
    YourEntity selectOne();

    List<YourEntity> selectList();
}
```

## [Demo](https://github.com/WhiteDG/mybatis-crypto/blob/main/mybatis-crypto-demo/README.MD)

## Configuration

| Property                           | Description                                                                                                                                                                                | Default Value |
|------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------|
| mybatis-crypto.enabled             | Enable mybatis-crypto                                                                                                                                                                      | true          |
| mybatis-crypto.fail-fast           | Fail fast during encryption/decryption. true: Throw an exception; false: Use the original value and log a warning level message                                                            | true          |
| mybatis-crypto.mapped-key-prefixes | Prefixes for @Param parameter names. If a parameter name matches any of the prefixes, encryption/decryption will be applied.                                                               | null          |
| mybatis-crypto.default-encryptor   | Global default Encryptor class name                                                                                                                                                        | null          |
| mybatis-crypto.encrypted-query     | Whether to enable exact matching for encrypted field queries                                                                                                                               | false         |
| mybatis-crypto.type-packages       | Package paths of entity classes. Optional configuration. If configured, the Encryptor will be loaded into the cache during service startup                                                 | null          |
| mybatis-crypto.keep-parameter      | Whether to keep plaintext parameters of entities. After executing insert/update statements, encrypted fields of entities can choose to keep the plaintext or overwrite with the ciphertext | false         |

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
