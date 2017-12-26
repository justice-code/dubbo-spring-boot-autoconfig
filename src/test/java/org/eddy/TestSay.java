package org.eddy;

import com.alibaba.dubbo.config.annotation.Reference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Start.class)
public class TestSay {

    private Say say;

    @Test
    public void test() {
        System.out.println(say.say());
    }

    @Reference
    public void setSay(Say say) {
        this.say = say;
    }
}
