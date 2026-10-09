package models;

import generators.GeneratingRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangeUserNameRequest extends BaseModel {
    @GeneratingRule(regex = "^[A-Za-z]{10} [A-Za-z]{10}$")
    private String name;
}
