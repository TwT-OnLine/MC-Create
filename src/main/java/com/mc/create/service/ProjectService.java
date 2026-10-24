package com.mc.create.service;

import com.mc.create.dto.CreateProjectDTO;
import com.mc.create.dto.UpdateProjectDTO;
import com.mc.create.entity.Project;

import java.util.List;

/**
 * 项目服务接口。
 * 由 impl/ProjectServiceImpl 实现。
 */
public interface ProjectService {

    /** 按用户 + 功能类型 + 状态查询项目列表（status 为空则查全部） */
    List<Project> getProjects(Long userId, String type, String status);

    /** 新建项目（默认进入草稿箱 draft） */
    Project createProject(Long userId, CreateProjectDTO dto);

    /** 查询单个项目（校验归属） */
    Project getProject(Long userId, Long id);

    /** 更新项目（改名/封面/状态/皮肤数据，仅非空字段） */
    Project updateProject(Long userId, Long id, UpdateProjectDTO dto);

    /** 删除项目（校验归属） */
    void deleteProject(Long userId, Long id);
}
