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

public class GetAccountResponse extends BaseModel {

        private String id;
        private String accountNumber;
        private double balance;
        List<Object> transactions;
}
