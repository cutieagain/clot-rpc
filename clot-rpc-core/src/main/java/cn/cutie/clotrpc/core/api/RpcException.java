package cn.cutie.clotrpc.core.api;

import lombok.Data;

/**
 * @Description: 自定义rpc异常
 * @Author: Cutie
 * @CreateDate: 2024/4/1 12:17
 * @Version: 0.0.1
 */
@Data
public class RpcException extends RuntimeException{

    private String errorCode;

    public RpcException() {
    }

    public RpcException(String message) {
        super(message);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }

    public RpcException(Throwable cause, String errorCode) {
        super(cause);
        this.errorCode = errorCode;
    }

    // x => 技术类异常
    // y => 业务类异常
    // Z => unknown异常
    public static final String SocketTimeoutEx = "X001" + "-" + "http_invoke_timeout";
    public static final String NoSuchMethodEx = "X002" + "-" + "method_not_exists";
    public static final String UnknownEx = "Z001" + "-" + "unknown";
}
