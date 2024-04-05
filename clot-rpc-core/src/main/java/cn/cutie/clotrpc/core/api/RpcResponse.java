package cn.cutie.clotrpc.core.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcResponse<T> {
    boolean status; // 状态 true
    T data; // 返回数据 new User()
    RpcException ex;

//    Object data; // 返回数据
}
