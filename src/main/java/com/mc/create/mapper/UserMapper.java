package com.mc.create.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mc.create.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
