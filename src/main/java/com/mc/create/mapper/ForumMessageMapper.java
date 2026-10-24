package com.mc.create.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mc.create.entity.ForumMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ForumMessageMapper extends BaseMapper<ForumMessage> {
}
