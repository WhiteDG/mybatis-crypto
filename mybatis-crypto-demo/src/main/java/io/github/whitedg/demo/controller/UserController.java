package io.github.whitedg.demo.controller;

import io.github.whitedg.demo.entity.User;
import io.github.whitedg.demo.mapper.UserMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @PostMapping("/batchCreate")
    public List<User> batchCreate(@RequestBody List<User> users) {
        int inserted = userMapper.batchInsert(users);
        return users;
    }

    @PutMapping("{id}")
    public User update(@PathVariable("id") Long id, @RequestBody User user) {
        user.setId(id);
        userMapper.updateById(user);
        return user;
    }

    @GetMapping("{id}")
    public ResponseEntity<User> get(@PathVariable("id") Long id) {
        User user = userMapper.selectById(id);
        return Optional.ofNullable(user)
                .map(u -> ResponseEntity.status(HttpStatus.OK).body(u))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping
    public List<User> getList(User user) {
        return userMapper.selectList(user);
    }

    @GetMapping("map/{id}")
    public Map<Long, ?> getMap(@PathVariable("id") Long id) {
        return userMapper.selectMap(id);
    }

    @GetMapping("name/{name}")
    public List<User> getByName(@PathVariable("name") String name) {
        return userMapper.selectByName(name);
    }
}
