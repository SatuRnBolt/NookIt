package com.nookit.modules.ping;

import com.nookit.common.api.Result;
import com.nookit.common.api.ResultCode;
import com.nookit.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 健康检查 / 框架自测接口。落地业务前用来验证脚手架是否启动成功。
 * <p>
 * 该 controller 在第一个业务模块上线后可移除。
 */
@RestController
@RequestMapping("/ping")
@Tag(name = "Ping", description = "框架自测")
public class PingController {

    @Operation(summary = "健康检查", description = "返回 pong + 时间戳")
    @GetMapping
    public Result<Map<String, Object>> ping() {
        return Result.success(Map.of(
                "message", "pong",
                "service", "nookit-backend",
                "timestamp", System.currentTimeMillis()
        ));
    }

    @Operation(summary = "异常处理自测", description = "传 ?type=biz / auth / oops 验证全局异常处理")
    @GetMapping("/error")
    public Result<Void> error(@RequestParam(defaultValue = "biz") String type) {
        switch (type) {
            case "biz" -> throw new BusinessException(ResultCode.BAD_REQUEST, "这是一个业务异常示例");
            case "oops" -> throw new RuntimeException("这是一个未捕获异常示例");
            default -> throw new BusinessException(ResultCode.INTERNAL_ERROR, "未知错误类型：" + type);
        }
    }
}
