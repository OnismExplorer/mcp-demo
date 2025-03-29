package cn.onism.mcp.handler;

import cn.onism.mcp.common.Result;
import cn.onism.mcp.constants.CodeEnum;
import cn.onism.mcp.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 异常处理器
 *
 * @author Onism
 * @date 2025-03-27
 */
@Slf4j
@RestControllerAdvice
public class AllExceptionHandler {

    /**
     * 服务器异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<String> deException(RuntimeException ex) {
        HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
        log.error("端口{}发生异常:", request.getServletPath(), ex);
        log.error("发生异常:", ex);
        return Result.<String>fail().message(CodeEnum.SYSTEM_REPAIR.getMsg() + "\n异常信息：" + ex.getMessage()).code(CodeEnum.SYSTEM_REPAIR.getCode());
    }

    /**
     * 自定义异常
     */
    @ExceptionHandler(CustomException.class)
    public Result<String> handleCustomException(CustomException ex) {
        log.error("异常：{}", ex.getMessage());
        return Result.<String>fail().message(ex.getMessage()).code(ex.getCode());
    }
}
