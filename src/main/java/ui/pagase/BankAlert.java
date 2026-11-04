package ui.pagase;

import lombok.Getter;

@Getter
public enum BankAlert {
    USER_CREATED_SUCCESSFULLY("✅ User created successfully!"),
    USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS("Username must be between 3 and 15 characters"),
    NEW_ACCOUNT_CREATED("✅ New Account Created! Account Number: "),
    NAME_UPDATED_SUCCESSFULLY("✅ Name updated successfully!"),
    NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS_ONLY("Name must contain two words with letters only"),
    SUCCESSFULLY_DEPOSITED("✅ Successfully deposited $%s to account %s!"),
    PLEASE_SELECT_AN_ACCOUNT("❌ Please select an account."),
    PLEASE_DEPOSIT_LESS_OR_EQUALS_TO_5000("❌ Please deposit less or equal to 5000$."),
    SUCCESSFULLY_TRANSFERRED("✅ Successfully transferred $%s to account %s!"),
    PLEASE_FILL_ALL_FIELDS_AND_CONFIRM("❌ Please fill all fields and confirm."),
    ERROR_TRANSFER_AMOUNT_CANNOT_EXCEED_10000("❌ Error: Transfer amount cannot exceed 10000"),
    ERROR_TRANSFER_AMOUNT_MUST_BE_AT_LEAST_001("❌ Error: Transfer amount must be at least 0.01"),
    ERROR_INVALID_TRANSFER_INSUFFICIENT_FUNDS("❌ Error: Invalid transfer: insufficient funds or invalid accounts");



    private final String message;
    BankAlert(String message) {
        this.message = message;
    }
}
