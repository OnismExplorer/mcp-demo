package cn.onism.mcp.common;

import cn.onism.mcp.constants.CodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 通用返回类
 */
@Data
@Schema(name = "Result", title = "通用返回对象带泛型，用于传输简单对象")
public class Result<T> {

    @Schema(name = "code", title = "返回状态", example = "200")
    Integer code;
    @Schema(name = "message", title = "返回信息", example = "success")
    String message;
    @Schema(name = "data", title = "返回数据")
    T data;

    Result() {
    }

    public static <T> Result<T> fail() {
        return Result.fail(CodeEnum.FAIL);
    }

    public static <T> Result<T> fail(String message) {
        Result<T> result = new Result<>();
        result.code = CodeEnum.FAIL.getCode();
        result.message = message;
        return result;
    }

    public static <T> Result<T> fail(CodeEnum codeMsg) {
        Result<T> result = new Result<>();
        result.code = codeMsg.getCode();
        result.message = codeMsg.getMsg();
        return result;
    }

    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = CodeEnum.SUCCESS.getCode();
        result.data = data;
        return result;
    }

    public static <T> Result<T> isSuccess(Boolean isSuccess) {
        if (Boolean.TRUE.equals(isSuccess)) {
            return success();
        } else {
            return fail();
        }
    }

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.message = CodeEnum.SUCCESS.getMsg();
        result.code = CodeEnum.SUCCESS.getCode();
        return result;
    }

    /**
     * 返回结果
     */
    public static <T> Result<T> result(CodeEnum codeEnum) {
        return new Result<T>().message(codeEnum.getMsg()).code(codeEnum.getCode());
    }

    public Result<T> message(String message) {
        this.message = message;
        return this;
    }

    public Result<T> code(int code) {
        this.code = code;
        return this;
    }
}
