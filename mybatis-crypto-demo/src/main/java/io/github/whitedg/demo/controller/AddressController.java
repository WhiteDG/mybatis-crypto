package io.github.whitedg.demo.controller;

import io.github.whitedg.demo.entity.Address;
import io.github.whitedg.demo.mapper.AddressMapper;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author White
 */
@RequestMapping("address")
@RestController
@AllArgsConstructor
public class AddressController {

    @Autowired
    private AddressMapper addressMapper;

    @PostMapping
    public Address create(@RequestBody Address address) {
        addressMapper.insert(address);
        return address;
    }
}
