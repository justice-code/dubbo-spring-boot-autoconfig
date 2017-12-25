package org.eddy;

import com.alibaba.dubbo.config.annotation.Service;

@Service
public class SayHello implements Say{
    @Override
    public String say() {
        return "hello";
    }
}
