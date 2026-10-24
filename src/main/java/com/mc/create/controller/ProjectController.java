package com.mc.create.controller;

import com.mc.create.common.Result;
import com.mc.create.dto.CreateProjectDTO;
import com.mc.create.dto.UpdateProjectDTO;
import com.mc.create.entity.Project;
import com.mc.create.service.ProjectService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /** 查询当前用户某功能类型下的项目列表，可按 status(draft/published) 过滤 */
    @GetMapping
    public Result<List<Project>> list(@RequestAttribute("userId") Long userId,
                                      @RequestParam(value = "type", required = false) String type,
                                      @RequestParam(value = "status", required = false) String status) {
        return Result.ok(projectService.getProjects(userId, type, status));
    }

    /** 新建项目（默认草稿） */
    @PostMapping
    public Result<Project> create(@RequestAttribute("userId") Long userId,
                                  @RequestBody CreateProjectDTO dto) {
        return Result.ok(projectService.createProject(userId, dto));
    }

    /** 查询单个项目 */
    @GetMapping("/{id}")
    public Result<Project> get(@RequestAttribute("userId") Long userId,
                               @PathVariable Long id) {
        return Result.ok(projectService.getProject(userId, id));
    }

    /** 更新项目：改名/封面/发布状态/皮肤数据 */
    @PutMapping("/{id}")
    public Result<Project> update(@RequestAttribute("userId") Long userId,
                                  @PathVariable Long id,
                                  @RequestBody UpdateProjectDTO dto) {
        return Result.ok(projectService.updateProject(userId, id, dto));
    }

    /** 删除项目 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@RequestAttribute("userId") Long userId,
                               @PathVariable Long id) {
        projectService.deleteProject(userId, id);
        return Result.ok(null);
    }
}
