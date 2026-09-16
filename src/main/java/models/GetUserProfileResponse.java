package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetUserProfileResponse extends BaseModel{
    private int id;
    private String username;
    private String password;
    private String name;
    private String role;
    private List<Account> accounts;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Account {
        private int id;
        private String accountNumber;
        private double balance;
        private List<Object> transactions;
    }
}
