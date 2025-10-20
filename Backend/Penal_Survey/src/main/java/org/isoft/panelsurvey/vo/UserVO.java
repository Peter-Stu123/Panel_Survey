package org.isoft.panelsurvey.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {
    private Long id;
    private String username;
    private String email;
    private String realName;
    private String role;
    private String institution;
    private String phone;
    private Integer status;
    private LocalDateTime createTime;
}

