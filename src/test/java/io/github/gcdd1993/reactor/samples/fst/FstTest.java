package io.github.gcdd1993.reactor.samples.fst;

import org.junit.jupiter.api.Test;
import org.nustaq.serialization.FSTConfiguration;

public class FstTest {

    private static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

    @Test
    public void serialize() {
        var person = Person.builder()
                .name("张三")
                .age(24)
                .love("李四")
                .build();
        // write
        byte[] bytes = conf.asByteArray(person);
        // read
        var zhangsan = (Person) conf.asObject(bytes);
        System.out.println(zhangsan);
    }

}
