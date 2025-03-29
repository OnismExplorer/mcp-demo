package cn.onism.mcp.constants;

/**
 * 系统状态常量
 *
 * @author Onism
 * @date 2025-03-27
 */
public enum CodeEnum {
    /**
     * 成功标志
     */
    SUCCESS(200, "成功！"),

    /**
     * 参数异常
     */
    PARAMETER_ERROR(400, "参数异常！"),

    /**
     * 邮箱为空
     */
    EMAIL_EMPTY(265, "邮箱为空！"),

    /**
     * 邮箱格式错误
     */
    EMAIL_FORMAT_ERROR(275, "邮箱格式错误！"),

    /**
     * 邮件发送失败
     */
    EMAIL_SEND_ERROR(295, "邮件发送失败，请稍后重试！"),
    /**
     * 数据不存在
     */
    DATA_NOT_EXIST(429,"当前查询的数据不存在,请稍后再试"),

    /**
     * 系统维护
     */
    SYSTEM_REPAIR(501, "系统维护中，请稍后！"),

    /**
     * 服务异常
     */
    FAIL(500, "服务异常！"),

    /**
     * 系统异常
     */
    SYSTEM_ERROR(502, "服务器异常！");

    private final int code;
    private final String msg;

    CodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
