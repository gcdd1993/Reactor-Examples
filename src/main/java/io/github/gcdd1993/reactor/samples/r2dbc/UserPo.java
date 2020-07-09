package io.github.gcdd1993.reactor.samples.r2dbc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPo {
    private String password;
    private String salt;

    @Builder.Default
    private Long createTime = System.currentTimeMillis();

    private String name;
    private String creatorId;

    @Builder.Default
    private String id = UUID.randomUUID().toString();

    private String type;
    private String username;
    private int status;
}
