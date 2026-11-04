package ui.pagase;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class MakeATransfer extends BasePage<MakeATransfer>{
    private SelenideElement recipientNameInput = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
    private SelenideElement recipientAccountInput = $(Selectors.byAttribute("placeholder","Enter recipient account number"));
    private SelenideElement amountInput = $(Selectors.byAttribute("placeholder","Enter amount"));
    private SelenideElement checkbox = $("#confirmCheck");
    private SelenideElement sendTransferButton =  $(Selectors.byTagAndText("button","🚀 Send Transfer"));

    @Override
    public String url() {
        return "/transfer";
    }

    public MakeATransfer selectAccount(String accountNumber) {
        $(Selectors.byText(accountNumber)).click();
        return this;
    }

    public MakeATransfer makeATransfer(String accountNumber, String recipientAccountNumber, String recipientName, double transferAmount) {
        selectAccount(accountNumber);
        recipientNameInput.setValue(recipientName);
        recipientAccountInput.setValue(recipientAccountNumber);
        amountInput.setValue(String.valueOf(transferAmount));
        checkbox.click();
        sendTransferButton.click();
        return this;
    }
    //Перегрузка для негативного теста
    public MakeATransfer makeATransfer(String recipientAccountNumber, String recipientName, double transferAmount) {
        recipientNameInput.setValue(recipientName);
        recipientAccountInput.setValue(recipientAccountNumber);
        amountInput.setValue(String.valueOf(transferAmount));
        checkbox.click();
        sendTransferButton.click();
        return this;
    }
    //Без принимающего аккаунта для негативного теста
    public MakeATransfer makeATransferWithoutRecipientAccount(String senderAccountNumber, String recipientName, double transferAmount) {
        selectAccount(senderAccountNumber);
        recipientNameInput.setValue(recipientName);
        amountInput.setValue(String.valueOf(transferAmount));
        checkbox.click();
        sendTransferButton.click();
        return this;
    }
    //Без чекбокса для негативного теста
    public MakeATransfer makeATransferWithoutCheckbox(String accountNumber, String recipientAccountNumber, String recipientName, double transferAmount) {
        selectAccount(accountNumber);
        recipientNameInput.setValue(recipientName);
        recipientAccountInput.setValue(recipientAccountNumber);
        amountInput.setValue(String.valueOf(transferAmount));
        sendTransferButton.click();
        return this;
    }


}
