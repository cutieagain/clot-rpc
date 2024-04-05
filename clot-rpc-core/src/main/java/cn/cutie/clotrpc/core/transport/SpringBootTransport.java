package cn.cutie.clotrpc.core.transport;

import cn.cutie.clotrpc.core.api.RpcRequest;
import cn.cutie.clotrpc.core.api.RpcResponse;
import cn.cutie.clotrpc.core.provider.ProviderInvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class SpringBootTransport {

    @Autowired
    ProviderInvoker providerInvoker;

    @RequestMapping("/clotrpc")
    public RpcResponse<Object> invoke(@RequestBody RpcRequest request) {
        return providerInvoker.invoke(request);
    }

}
