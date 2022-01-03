package io.github.whitedg.demo.controller;

import io.github.whitedg.demo.entity.User;
import io.github.whitedg.demo.mapper.UserMapper;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author White
 */
@RequestMapping("users")
@RestController
@AllArgsConstructor
public class UserController {

    private UserMapper userMapper;

    @PostMapping
    public User create(@RequestBody User user) {
        userMapper.insert(user);
        return user;
    }

    @PutMapping("{id}")
    public User update(@PathVariable("id") Long id, @RequestBody User user) {
        user.setId(id);
        userMapper.updateById(user);
        return user;
    }

    @GetMapping("{id}")
    public User get(@PathVariable("id") Long id) {
        return userMapper.selectById(id);
    }

    @GetMapping
    public List<User> getList() {
        return userMapper.selectAll();
    }

    @GetMapping("map/{id}")
    public Map<Long, ?> getMap(@PathVariable("id") Long id) {
        return userMapper.selectMap(id);
    }
}
