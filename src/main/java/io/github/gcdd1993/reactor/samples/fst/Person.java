package io.github.gcdd1993.reactor.samples.fst;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Person implements Serializable {
    private String name;
    private int age;
    private String love;
}
