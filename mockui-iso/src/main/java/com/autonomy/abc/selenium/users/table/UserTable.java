package com.autonomy.abc.selenium.users.table;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class UserTable<T extends UserTableRow> extends AppElement implements Iterable<T> {
    public UserTable(WebElement element, WebDriver driver) {
        super(element, driver);
    }

    @Override
    public Iterator<T> iterator() {
        return rows().iterator();
    }

    public List<T> rows() {
        List<T> rows = new ArrayList<>();
        for (WebElement rowEl : findElements(By.cssSelector("tbody tr"))) {
            rows.add(rowForElement(rowEl));
        }
        return rows;
    }

    protected abstract T rowForElement(WebElement element);
}
