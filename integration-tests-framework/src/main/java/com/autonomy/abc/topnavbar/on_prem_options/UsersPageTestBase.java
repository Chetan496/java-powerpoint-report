package com.autonomy.abc.topnavbar.on_prem_options;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.control.Window;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.external.GmailSignupEmailHandler;
import com.autonomy.abc.selenium.external.GoogleAuth;
import com.autonomy.abc.selenium.users.*;
import com.autonomy.abc.selenium.util.Waits;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.NoSuchElementException;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ControlMatchers.url;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static com.autonomy.abc.matchers.ElementMatchers.modalIsDisplayed;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.openqa.selenium.lift.Matchers.displayed;

public class UsersPageTestBase<T extends NewUser> extends ABCTestBase {
    protected final NewUser aNewUser = getConfig().getNewUser("james");
    protected final NewUser newUser2 = getConfig().getNewUser("john");
    protected int defaultNumberOfUsers = (getConfig().getType() == ApplicationType.HOSTED) ? 0 : 1;
    protected UsersPage usersPage;
    protected UserService<?> userService;
    protected SignupEmailHandler emailHandler;
    private LoginService loginService;

    public UsersPageTestBase(TestConfig config) {
        super(config);
        if(config.getType().equals(ApplicationType.HOSTED)) {
            emailHandler = new GmailSignupEmailHandler((GoogleAuth) config.getUser("google").getAuthProvider());
        }
    }

    @Before
    public void setUp() throws MalformedURLException, InterruptedException {
        loginService = getApplication().loginService();
        userService = getApplication().userService();
        usersPage = userService.goToUsers();
        userService.deleteOtherUsers();
    }

    @After
    public void emailTearDown() {
        if(getConfig().getType().equals(ApplicationType.HOSTED)) {
            Window firstWindow = getWindow();
            Window secondWindow = getMainSession().openWindow("about:blank");
            try {
                emailHandler.markAllEmailAsRead(getDriver());
            } catch (TimeoutException e) {
                LoggerFactory.getLogger(UsersPageTestBase.class).warn("Could not tear down");
            } finally {
                secondWindow.close();
                firstWindow.activate();
            }
        }
    }

    @After
    public void userTearDown() {
        if (loginService.getCurrentUser() != getInitialUser()) {
            logoutAndNavigateToWebApp();
            loginAs(getInitialUser());
        }
        userService.deleteOtherUsers();
    }

    protected User singleSignUp() {
        usersPage.createUserButton().click();
        assertThat(usersPage, modalIsDisplayed());
        final ModalView newUserModal = ModalView.getVisibleModalView(getDriver());
        User user = usersPage.addNewUser(aNewUser, Role.USER);
        user.authenticate(getConfig().getWebDriverFactory(), emailHandler);
//		assertThat(newUserModal, containsText("Done! User " + user.getUsername() + " successfully created"));
        verifyUserAdded(newUserModal, user);
        usersPage.closeModal();
        return user;
    }

    protected void signUpAndLoginAs(T newUser) {
        usersPage.createUserButton().click();
        assertThat(usersPage, modalIsDisplayed());

        User user = usersPage.addNewUser(newUser, Role.USER);
        user.authenticate(getConfig().getWebDriverFactory(), emailHandler);
        usersPage.closeModal();

        try {
            Waits.waitForGritterToClear();
        } catch (InterruptedException e) { /**/ }

        logoutAndNavigateToWebApp();

        try {
            loginAs(user);
        } catch (TimeoutException | NoSuchElementException e) { /* Probably because of the sessions you're already logged in */ }

        getElementFactory().getPromotionsPage();
        assertThat(getWindow(), url(not(containsString("login"))));
    }

    protected void deleteAndVerify(User user) {
        userService.deleteUser(user);
        if (getConfig().getType().equals(ApplicationType.ON_PREM)) {
            verifyThat(usersPage, containsText("User " + user.getUsername() + " successfully deleted"));
        } else {
            new WebDriverWait(getDriver(),10).withMessage("User " + user.getUsername() + " not successfully deleted").until(GritterNotice.notificationContaining("Deleted user " + user.getUsername()));
        }
    }

    protected void verifyUserAdded(ModalView newUserModal, User user){
        if(getConfig().getType().equals(ApplicationType.ON_PREM)){
            verifyThat(newUserModal, containsText("Done! User " + user.getUsername() + " successfully created"));
        }

        //Hosted notifications are dealt with within the sign up method and there is no real way to ensure that a user's been created at the moment
    }

    protected void logoutAndNavigateToWebApp() {
        if (loginService.getCurrentUser() != null) {
            loginService.logout();
        }
        getDriver().get(getAppUrl());
    }

    protected LoginService getLoginService() {
        return loginService;
    }

    protected void verifyCreateDeleteInTable(NewUser newUser) {
        User user = userService.createNewUser(newUser, Role.USER);
        String username = user.getUsername();

        verifyThat(usersPage.deleteButton(user), displayed());
        verifyThat(usersPage.getTable(), containsText(username));

        deleteAndVerify(user);
        verifyThat(usersPage.getTable(), not(containsText(username)));
    }

    protected void loginAs(User user) {
        loginService.login(user);
    }
}
