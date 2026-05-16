package com.nookit.modules.admin.notice.controller;

import com.nookit.common.annotation.CurrentUser;
import com.nookit.common.api.PageResult;
import com.nookit.common.api.Result;
import com.nookit.modules.admin.notice.dto.NoticeCreateReq;
import com.nookit.modules.admin.notice.dto.NoticeVO;
import com.nookit.modules.admin.notice.service.NoticeService;
import com.nookit.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/notices")
@RequiredArgsConstructor
@Tag(name = "Admin-Notice", description = "通知公告管理")
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    @Operation(summary = "公告列表")
    public Result<PageResult<NoticeVO>> pageNotices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type) {
        return Result.success(noticeService.pageNotices(page, pageSize, search, type));
    }

    @GetMapping("/{id}")
    @Operation(summary = "公告详情")
    public Result<NoticeVO> getById(@PathVariable Long id) {
        return Result.success(noticeService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建公告")
    public Result<NoticeVO> createNotice(@Valid @RequestBody NoticeCreateReq req,
                                          @CurrentUser UserPrincipal operator) {
        return Result.success(noticeService.createNotice(req, operator));
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑公告")
    public Result<Void> updateNotice(@PathVariable Long id,
                                     @Valid @RequestBody NoticeCreateReq req) {
        noticeService.updateNotice(id, req);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除公告")
    public Result<Void> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return Result.success();
    }

    @PatchMapping("/{id}/publish")
    @Operation(summary = "发布草稿公告")
    public Result<Void> publishNotice(@PathVariable Long id) {
        noticeService.publishNotice(id);
        return Result.success();
    }
}
