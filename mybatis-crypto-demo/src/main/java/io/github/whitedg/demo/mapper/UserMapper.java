package io.github.whitedg.demo.mapper;

import io.github.whitedg.demo.encryptor.MyEncryptor;
import io.github.whitedg.demo.entity.User;
import io.github.whitedg.mybatis.crypto.EncryptedField;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author White
 */
public interface UserMapper {

    int insert(User user);

    int batchInsert(@Param("encryptedUsers") List<User> users);

    int updateById(@Param("et") User user);

    User selectById(@Param("id") Long id);

    List<User> selectList(@Param("et") User user);

    List<User> selectAll();

    @MapKey("id")
    Map<Long, User> selectMap(@Param("id") Long id);

    List<User> selectByName(@EncryptedField(encryptor = MyEncryptor.class) @Param("name") String name);
}
