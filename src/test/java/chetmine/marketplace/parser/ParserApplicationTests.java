package chetmine.marketplace.parser;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class ParserApplicationTests {

    public Integer sum(Integer a, Integer b) {
        return a + b;
    }

    @Test
    void contextLoads() {
        assert sum(1,2).equals(3);
    }

}
