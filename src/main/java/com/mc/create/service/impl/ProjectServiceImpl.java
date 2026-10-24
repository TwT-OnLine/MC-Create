package com.mc.create.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mc.create.common.BizException;
import com.mc.create.dto.CreateProjectDTO;
import com.mc.create.dto.UpdateProjectDTO;
import com.mc.create.entity.Project;
import com.mc.create.mapper.ProjectMapper;
import com.mc.create.service.ProjectService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {

    private static final String STATUS_DRAFT = "draft";
    private static final String STATUS_PUBLISHED = "published";
    /** 与 project.project_name VARCHAR(100) / type VARCHAR(20) 对应，避免超长写入抛 500 */
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_TYPE_LENGTH = 20;

    private final ProjectMapper projectMapper;

    public ProjectServiceImpl(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    @Override
    public List<Project> getProjects(Long userId, String type, String status) {
        LambdaQueryWrapper<Project> qw = new LambdaQueryWrapper<Project>()
                .eq(Project::getUserId, userId)
                // 列表页只需要名称/封面/状态，皮肤数据是 base64 LONGTEXT（可达数 MB），
                // 不查出来可以显著减小响应体，也避免调试日志被大字段刷屏。
                .select(Project::getId, Project::getUserId, Project::getType,
                        Project::getProjectName, Project::getVersion, Project::getStatus,
                        Project::getCover, Project::getCreateTime);
        if (type != null && !type.isBlank()) {
            qw.eq(Project::getType, type);
        }
        if (status != null && !status.isBlank()) {
            qw.eq(Project::getStatus, status);
        }
        qw.orderByDesc(Project::getCreateTime);
        return projectMapper.selectList(qw);
    }

    @Override
    public Project createProject(Long userId, CreateProjectDTO dto) {
        String projectName = dto.getName() == null ? "" : dto.getName().trim();
        if (projectName.isEmpty())
            throw new BizException("项目名称不能为空");
        if (projectName.length() > MAX_NAME_LENGTH)
            throw new BizException("项目名称不能超过 " + MAX_NAME_LENGTH + " 个字符");

        String type = (dto.getType() == null || dto.getType().isBlank()) ? "default" : dto.getType().trim();
        if (type.length() > MAX_TYPE_LENGTH)
            throw new BizException("功能类型不合法");

        Project project = new Project();
        project.setUserId(userId);
        project.setType(type);
        project.setProjectName(projectName);
        project.setVersion(dto.getVersion());
        project.setStatus(STATUS_DRAFT); // 新建默认草稿
        project.setCreateTime(LocalDateTime.now());
        projectMapper.insert(project);
        return project;
    }

    @Override
    public Project getProject(Long userId, Long id) {
        Project project = requireOwned(userId, id);
        return project;
    }

    @Override
    public Project updateProject(Long userId, Long id, UpdateProjectDTO dto) {
        Project project = requireOwned(userId, id);
        if (dto.getName() != null && !dto.getName().isBlank()) {
            String projectName = dto.getName().trim();
            if (projectName.length() > MAX_NAME_LENGTH)
                throw new BizException("项目名称不能超过 " + MAX_NAME_LENGTH + " 个字符");
            project.setProjectName(projectName);
        }
        if (dto.getCover() != null) {
            project.setCover(dto.getCover());
        }
        if (dto.getSkinData() != null) {
            project.setSkinData(dto.getSkinData());
        }
        if (dto.getStatus() != null) {
            String s = dto.getStatus().trim();
            if (!STATUS_DRAFT.equals(s) && !STATUS_PUBLISHED.equals(s))
                throw new BizException("状态只能为 draft 或 published");
            project.setStatus(s);
        }
        projectMapper.updateById(project);
        return project;
    }

    @Override
    public void deleteProject(Long userId, Long id) {
        Project project = requireOwned(userId, id);
        projectMapper.deleteById(project.getId());
    }

    /** 查出项目并校验归属，不存在或不属于该用户则抛异常 */
    private Project requireOwned(Long userId, Long id) {
        if (id == null) throw new BizException("项目ID不能为空");
        Project project = projectMapper.selectById(id);
        if (project == null) throw new BizException("项目不存在");
        if (!project.getUserId().equals(userId)) throw new BizException("无权操作该项目");
        return project;
    }
}
