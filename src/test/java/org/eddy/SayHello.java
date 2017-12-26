package org.eddy;

import com.alibaba.dubbo.config.annotation.Service;

@Service(parameters = {"today", "25", "yi", "yan"})
public class SayHello implements Say{
    @Override
    public String say() {
        return "hello, my reference";
    }
}
