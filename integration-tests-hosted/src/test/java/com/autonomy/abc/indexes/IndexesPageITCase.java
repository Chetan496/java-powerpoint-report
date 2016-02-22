package com.autonomy.abc.indexes;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.KnownBug;
import com.autonomy.abc.selenium.connections.*;
import com.autonomy.abc.selenium.control.Window;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.HSODFind;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.IndexService;
import com.autonomy.abc.selenium.indexes.IndexesDetailPage;
import com.autonomy.abc.selenium.indexes.IndexesPage;
import com.autonomy.abc.selenium.indexes.tree.IndexNodeElement;
import com.autonomy.abc.selenium.promotions.PinToPositionPromotion;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.promotions.PromotionsPage;
import com.autonomy.abc.selenium.search.IndexFilter;
import com.autonomy.abc.selenium.search.SearchQuery;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.PageUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.NoSuchElementException;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static com.autonomy.abc.matchers.ElementMatchers.hasClass;
import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

public class IndexesPageITCase extends HostedTestBase {
    private final static Logger LOGGER = LoggerFactory.getLogger(IndexesPageITCase.class);
    private IndexService indexService;
    private IndexesPage indexesPage;

    public IndexesPageITCase(TestConfig config) {
        super(config);
        // requires a separate account where indexes can safely be added and deleted
        setInitialUser(config.getUser("index_tests"));
    }

    @Before
    public void setUp() {
        indexService = getApplication().indexService();
        indexesPage = indexService.goToIndexes();
    }

    @Test
    @KnownBug("CSA-1450")
    public void testDeletingIndex(){
        Index index = new Index("index");
        indexesPage = indexService.setUpIndex(index);

        verifyThat(indexesPage.getIndexDisplayNames(), hasItem(index.getName()));

        indexService.deleteIndex(index);

        verifyThat(indexesPage.getIndexDisplayNames(), not(hasItem(index.getName())));
    }

    @Test
    @KnownBug("CSA-1720")
    public void testDefaultIndexIsNotDeletedWhenDeletingTheSoleConnectorAssociatedWithIt(){
        ConnectionService cs = getApplication().connectionService();
        WebConnector connector = new WebConnector("http://www.bbc.co.uk","bbc", Index.DEFAULT).withDuration(150);

        //Create connection
        cs.setUpConnection(connector);

        try {
            //Try to delete the connection, (and the default index)
            cs.deleteConnection(connector, true);
        } catch (ElementNotVisibleException | NoSuchElementException e) {
            //If there's an error it is likely because the index couldn't be deleted - which is expected
            //Need to exit the deletion modal that will still be open
            getDriver().findElement(By.cssSelector(".modal-footer [type=button]")).click();
        }

        indexesPage = indexService.goToIndexes();

        //Make sure default index is still there
        assertThat(indexesPage.getIndexDisplayNames(), hasItem(Index.DEFAULT.getDisplayName()));
    }

    @Test
    //Potentially should be in ConnectionsPageITCase
    @KnownBug("CSA-1710")
    public void testAttemptingToDeleteConnectionWhileItIsProcessingDoesNotDeleteAssociatedIndex(){
        ConnectionService connectionService = getApplication().connectionService();

        ConnectionsPage connectionsPage = connectionService.goToConnections();

        //Create connector; index will be automatically set to 'bbc'
        WebConnector connector = new WebConnector("http://www.bbc.co.uk","bbc").withDepth(2);
        Index index = connector.getIndex();

        //Create new connector - NO WAIT
        connectionsPage.newConnectionButton().click();
        NewConnectionPage newConnectionPage = getElementFactory().getNewConnectionPage();
        connector.makeWizard(newConnectionPage).apply();

        //Try deleting the index straight away, while it is still processing
        //TODO change the Gritter Notice it's expecting
        try {
            connectionService.deleteConnection(connector, true);
        } catch (Exception e) {
            LOGGER.warn("Error deleting index");
        }

        indexesPage = indexService.goToIndexes();

        //Ensure the index wasn't deleted
        assertThat(indexesPage.getIndexDisplayNames(), hasItem(index.getName()));
    }

    @Test
    @KnownBug("CSA-1626")
    public void testDeletingIndexDoesNotInvalidatePromotions(){
        //Create connection - attached to the same index (we need it to have data for a promotion)
        ConnectionService connectionService = getApplication().connectionService();
        WebConnector connector = new WebConnector("http://www.bbc.co.uk","bbc").withDepth(2);

        connectionService.setUpConnection(connector);

        //Create a promotion (using the index created)
        PromotionService promotionService = getApplication().promotionService();
        PinToPositionPromotion ptpPromotion = new PinToPositionPromotion(1,"trigger");
        SearchQuery search = new SearchQuery("bbc").withFilter(new IndexFilter(connector.getIndex()));

        try {
            int numberOfDocs = 1;
            promotionService.setUpPromotion(ptpPromotion, search, numberOfDocs);

            //Now delete the index
            connectionService.deleteConnection(connector, true);

            //Navigate to the promotion - this will time out if it can't get to the Promotions Detail Page
            promotionService.goToDetails(ptpPromotion);

            //Get the promoted documents, there should still be one
            //TODO this is a workaround as getting promoted documents 'properly' errors if they are 'Unknown Document's
            List<WebElement> promotedDocuments = getDriver().findElements(By.cssSelector(".promoted-documents-list h3"));

            assertThat(promotedDocuments.size(), is(numberOfDocs));

            //All documents should know be 'unknown documents'
            for(WebElement promotedDocument : promotedDocuments){
                assertThat(promotedDocument.getText(), is("Unknown Document"));

            }
        } finally {
            promotionService.deleteAll();
        }
    }

    @Test
    @KnownBug("CSA-1544")
    public void testNoInvalidIndexNameNotifications(){
        ConnectionService connectionService = getApplication().connectionService();

        Connector hassleRecords = new WebConnector("http://www.hasslerecords.com","hassle records").withDepth(1);
        String errorMessage = "Index name invalid";

        connectionService.setUpConnection(hassleRecords);

        try {
            new WebDriverWait(getDriver(),30).until(GritterNotice.notificationContaining(errorMessage));

            fail("Index name should be valid - likely failed due to double encoding of requests");
        } catch (TimeoutException e){
            LOGGER.info("Timeout exception");
        }

        getElementFactory().getTopNavBar().notificationsDropdown();
        for(String message : getElementFactory().getTopNavBar().getNotifications().getAllNotificationMessages()){
            assertThat(message,not(errorMessage));
        }
    }

    @Test
    @KnownBug("CSA-1689")
    public void testNewlyCreatedIndexSize (){
        indexService.deleteAllIndexes();

        Index index = new Index("yellow cat red cat");

        indexService.setUpIndex(index);
        indexService.goToDetails(index);

        IndexesDetailPage indexesDetailPage = getElementFactory().getIndexesDetailPage();

        verifyThat(indexesDetailPage.sizeString(), allOf(containsString("128 B"), containsString("(0 items)")));
    }

    @Test
    @KnownBug("CSA-1735")
    public void testNavigatingToNonExistingIndexByURL(){
        getDriver().get(getAppUrl().split("searchoptimizer")[0] + "search/#/index/doesntexistmate");
        verifyThat(PageUtil.getWrapperContent(getDriver()), containsText(Errors.Index.INVALID_INDEX));
    }

    @Test
    @KnownBug("CSA-1886")
    @Ignore("Breaking too many tests")
    public void testDeletingDefaultIndex(){
        indexService.deleteIndexViaAPICalls(Index.DEFAULT, getCurrentUser(), getConfig().getApiUrl());

        getDriver().navigate().refresh();
        indexesPage = getElementFactory().getIndexesPage();

        verifyThat(indexesPage.getIndexDisplayNames(), hasItem(Index.DEFAULT.getDisplayName()));
    }

    @Test
    @Ignore("Breaking too many tests")
    public void testDeletingSearchDefaultIndex(){
        indexService.deleteIndexViaAPICalls(new Index("search_default_index"), getCurrentUser(), getConfig().getApiUrl());
        getDriver().navigate().refresh();

        verifyThat(getApplication().switchTo(PromotionsPage.class), containsText("There are no promotions..."));
    }

    @Test
    @KnownBug("CCUK-3450")
    public void testFindNoParametricFields(){
        Index index = new Index("index");
        indexService.setUpIndex(index);

        Window searchWindow = getMainSession().getActiveWindow();
        HSODFind findApp = new HSODFind();
        Window findWindow = launchInNewWindow(findApp);

        try {
            findWindow.activate();
            FindPage findPage = findApp.elementFactory().getFindPage();

            findPage.search("search");
            findPage.filterBy(new IndexFilter(index));

            verifyThat(findPage.getResultsPage().resultsDiv().getText(), is("No results found"));
        } finally {
            findWindow.close();
            searchWindow.activate();
        }
    }

    @Test
    @KnownBug("CCUK-3620")
    public void testFindBehavesAfterDeletingIndex() {
        Index index = new Index("index");
        indexService.setUpIndex(index);

        Window searchWindow = getMainSession().getActiveWindow();
        HSODFind findApp = new HSODFind();
        Window findWindow = launchInNewWindow(findApp);

        try {
            findWindow.activate();
            FindPage findPage = findApp.elementFactory().getFindPage();

            findPage.search("Exeter");
            verifyThat(findPage, not(containsText("An error occurred")));
            verifyThat("Index displayed properly", indexElement(findPage), not(hasClass("disabled-index")));

            searchWindow.activate();
            indexService.deleteIndex(index);

            findWindow.activate();
            findPage.search("Plymouth");
            verifyThat("Deleted index disabled", indexElement(findPage), hasClass("disabled-index"));

            findWindow.refresh();
            findPage = findApp.elementFactory().getFindPage();
            findPage.search("Plymouth");

            for (IndexNodeElement node : findPage.indexesTree()) {
                verifyThat(node.getName(), not(index.getName()));
            }
        } finally {
            findWindow.close();
            searchWindow.activate();
        }
    }

    private WebElement indexElement(FindPage findPage){
        return ElementUtil.ancestor(findPage.getResultsPage().resultsDiv().findElement(By.xpath("//*[@class='database-name' and text()='index']")), 2);
    }

    @After
    public void tearDown(){
        try {
            getApplication().connectionService().deleteAllConnections(false);
            getApplication().indexService().deleteAllIndexes();
        } catch (Exception e) {
            LOGGER.warn("Failed to tear down");
        }
    }
}
