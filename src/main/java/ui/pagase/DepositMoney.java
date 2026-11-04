package ui.pagase;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class DepositMoney extends BasePage<DepositMoney>{
   private SelenideElement amountInput = $(Selectors.byAttribute("placeholder","Enter amount"));
   private SelenideElement depositButton =  $(Selectors.byText("💵 Deposit"));

    @Override
    public String url() {
        return "/deposit";
    }

    public DepositMoney selectAccount(String accountNumber) {
        $(Selectors.byText(accountNumber)).click();
        return this;
    }
    public DepositMoney depositMoney(String account,double amount){
        selectAccount(account);
        amountInput.setValue(String.valueOf(amount));
        depositButton.click();
        return this;
    }

    //Перегружаю для негативного теста
    public DepositMoney depositMoney(double amount){
        amountInput.setValue(String.valueOf(amount));
        depositButton.click();
        return this;
    }
}
