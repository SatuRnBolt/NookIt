package com.nookit.modules.student.notice.controller;

import com.nookit.common.api.PageResult;
import com.nookit.common.api.Result;
import com.nookit.modules.admin.notice.dto.NoticeVO;
import com.nookit.modules.admin.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
@Tag(name = "Student-Notice", description = "学生端通知公告")
public class StudentNoticeController {

    private final NoticeService noticeService;

    @GetMapping("/notices")
    @Operation(summary = "通知公告列表（仅已发布）")
    public Result<PageResult<Map<String, Object>>> listPublished(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String type) {
        PageResult<NoticeVO> pg = noticeService.pageNotices(page, pageSize, null, type);
        // filter only published — and add snake_case aliases for frontend
        List<Map<String, Object>> records = new ArrayList<>();
        if (pg.getRecords() != null) {
            for (NoticeVO n : pg.getRecords()) {
                if (!"published".equals(n.getStatus())) continue;
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", n.getId());
                m.put("title", n.getTitle());
                m.put("content", n.getContent());
                m.put("notice_type", n.getType());
                m.put("type", n.getType());
                m.put("published_at", n.getPublishedAt() != null ? n.getPublishedAt().toString() : null);
                m.put("publishedAt", n.getPublishedAt() != null ? n.getPublishedAt().toString() : null);
                m.put("created_at", n.getCreatedAt() != null ? n.getCreatedAt().toString() : null);
                m.put("author_name", n.getAuthor());
                m.put("authorName", n.getAuthor());
                records.add(m);
            }
        }
        PageResult<Map<String, Object>> result = PageResult.of(records, records.size(), page, pageSize);
        return Result.success(result);
    }

    @GetMapping("/notices/{id}")
    @Operation(summary = "公告详情")
    public Result<NoticeVO> getDetail(@PathVariable Long id) {
        return Result.success(noticeService.getById(id));
    }
}
