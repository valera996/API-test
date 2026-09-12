package models;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateAccountResponse {
    int id;
    String accountNumber;
    double balance;
    int[] transactions;
}
