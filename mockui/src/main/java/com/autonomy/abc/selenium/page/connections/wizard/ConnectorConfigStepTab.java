package com.autonomy.abc.selenium.page.connections.wizard;

import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.page.SAASPageBase;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by avidan on 10-11-15.
 */
public class ConnectorConfigStepTab extends SAASPageBase {
    private ConnectorConfigStepTab(WebDriver driver) {
        super(driver);
    }

    public static ConnectorConfigStepTab make(WebDriver driver){
        new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.name("connectorConfigStepForm")));
        return new ConnectorConfigStepTab(driver);
    }

    public AppElement scheduleForm(){
        return new AppElement(findElement(By.name("scheduleForm")), getDriver());
    }

    public FormInput timeIntervalInput(){
        return new FormInput(findElement(By.name("timeInterval")), getDriver());
    }

    public AppElement repeatingForm(){
        return new AppElement(findElement(By.name("limitForm")), getDriver());
    }

    public AppElement unlimitedOccurrencesCheckBox(){
        return new AppElement(findElement(By.name("no-limit")), getDriver());
    }

    public AppElement limitedOccurrencesCheckBox(){
        return new AppElement(findElement(By.name("limit-occurrences")), getDriver());
    }

    public FormInput occurrencesInput(){
        return new FormInput(findElement(By.name("occurrences")), getDriver());
    }

    public WebElement advancedConfigurations(){
        return findElement(By.id("advancedConfigurationPropsHeader"));
    }

    public WebElement getDepthBox(){
        return findElement(By.cssSelector("[name='depth']"));
    }

    public WebElement getMaxPagesBox() {
        return findElement(By.cssSelector("[name='max_pages']"));
    }

    private WebElement scheduleButton(String time){
        return findElement(By.xpath("//button[text()='" + time + "']"));
    }

    public WebElement hoursButton(){
        return scheduleButton("Hours");
    }

    public WebElement daysButton(){
        return scheduleButton("Days");
    }

    public WebElement weeksButton(){
        return scheduleButton("Weeks");
    }

    public String scheduleString(){
        return findElement(By.cssSelector("label.ng-scope.m-t")).getText();
    }

    public List<WebElement> getAllButtons() {
        List<WebElement> buttons = new ArrayList<>();
        buttons.add(hoursButton());
        buttons.add(daysButton());
        buttons.add(weeksButton());
        return buttons;
    }
}
