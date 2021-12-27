package io.github.whitedg.demo.mapper;

import io.github.whitedg.demo.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author White
 */
public interface UserMapper {

    int insert(User user);

    int updateById(@Param("et") User user);

    User selectById(@Param("id") Long id);

    List<User> selectAll();

}
