package io.github.whitedg.demo.mapper;

import io.github.whitedg.demo.entity.User;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author White
 */
public interface UserMapper {

    int insert(User user);

    int updateById(@Param("et") User user);

    User selectById(@Param("id") Long id);

    List<User> selectList(@Param("et") User user);

    List<User> selectAll();

    @MapKey("id")
    Map<Long, User> selectMap(@Param("id") Long id);
}
