package cn.cutie.clotrpc.core.registry;

import cn.cutie.clotrpc.core.meta.InstanceMata;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Event {
    List<InstanceMata> data;
}
