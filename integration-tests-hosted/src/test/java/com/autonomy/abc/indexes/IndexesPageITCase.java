package com.autonomy.abc.indexes;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.config.HSOApplication;
import com.autonomy.abc.selenium.connections.ConnectionService;
import com.autonomy.abc.selenium.connections.WebConnector;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.IndexService;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.connections.ConnectionsPage;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;
import com.autonomy.abc.selenium.page.indexes.IndexesPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.promotions.PinToPositionPromotion;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.search.IndexFilter;
import com.autonomy.abc.selenium.search.Search;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static junit.framework.TestCase.fail;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

public class IndexesPageITCase extends ABCTestBase {
    private IndexesPage indexesPage;
    private HSOElementFactory hsoElementFactory;
    private HSOApplication hsoApplication;
    private Logger logger = LoggerFactory.getLogger(IndexesPageITCase.class);

    public IndexesPageITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    @Before
    @Override
    public void baseSetUp() throws InterruptedException {
        regularSetUp();
        hostedLogIn("yahoo");
        getElementFactory().getPromotionsPage();

        hsoElementFactory = (HSOElementFactory) getElementFactory();
        hsoApplication = (HSOApplication) getApplication();

        body.getSideNavBar().switchPage(NavBarTabId.INDEXES);
        indexesPage = hsoElementFactory.getIndexesPage();

        body = getBody();
    }

    @Test
    //CSA1720
    public void testDefaultIndexIsNotDeletedWhenDeletingTheSoleConnectorAssociatedWithIt(){
        ConnectionService cs = hsoApplication.createConnectionService(hsoElementFactory);
        Index default_index = new Index("default_index");
        WebConnector connector = new WebConnector("http://www.bbc.co.uk","bbc",default_index);

        //Create connection
        cs.setUpConnection(connector);

        try {
            //Try to delete the connection, (and the default index)
            cs.deleteConnection(connector, true);
        } catch (ElementNotVisibleException e) {
            //If there's an error it is likely because the index couldn't be deleted - which is expected
            //Need to exit the modal
            getDriver().findElement(By.cssSelector(".modal-footer [type=button]")).click();
        }

        //Navigate to indexes
        body.getSideNavBar().switchPage(NavBarTabId.INDEXES);
        IndexesPage indexesPage = hsoElementFactory.getIndexesPage();

        //Make sure default index is still there
        assertThat(indexesPage.getIndexNames(),hasItem(default_index.getName()));
    }

    @Test
    //Potentially should be in ConnectionsPageITCase
    //CSA1710
    public void testDeletingConnectionWhileItIsProcessingDoesNotDeleteAssociatedIndex(){
        body.getSideNavBar().switchPage(NavBarTabId.CONNECTIONS);
        ConnectionsPage connectionsPage = hsoElementFactory.getConnectionsPage();
        ConnectionService connectionService = hsoApplication.createConnectionService(hsoElementFactory);

        //Create connector; index will be automatically set to 'bbc'
        WebConnector connector = new WebConnector("www.bbc.co.uk","bbc");
        Index index = connector.getIndex();

        //Create new connector - NO WAIT
        connectionsPage.newConnectionButton().click();
        NewConnectionPage newConnectionPage = hsoElementFactory.getNewConnectionPage();
        connector.makeWizard(newConnectionPage).apply();

        //Try deleting the index straight away, while it is still processing
        //TODO change the Gritter Notice it's expecting
        connectionService.deleteConnection(connector, true);

        //Navigate to Indexes
        body.getSideNavBar().switchPage(NavBarTabId.INDEXES);
        IndexesPage indexesPage = hsoElementFactory.getIndexesPage();

        //Ensure the index wasn't deleted
        assertThat(indexesPage.getIndexNames(),hasItem(index.getName()));
    }

    @Test
    //CSA1626
    public void testDeletingIndexDoesNotInvalidatePromotions(){
        Index index = new Index("bbc");

        //Create index
        IndexService indexService = hsoApplication.createIndexService(hsoElementFactory);
        indexService.setUpIndex(index);

        //Create connection - attached to the same index (we need it to have data for a promotion)
        ConnectionService connectionService = hsoApplication.createConnectionService(hsoElementFactory);
        WebConnector connector = new WebConnector("www.bbc.co.uk","bbc",index);

        connectionService.setUpConnection(connector);

        //Create a promotion (using the index created)
        PromotionService promotionService = hsoApplication.createPromotionService(hsoElementFactory);
        PinToPositionPromotion ptpPromotion = new PinToPositionPromotion(1,"trigger");
        Search search = new Search(hsoApplication,hsoElementFactory,"search").applyFilter(new IndexFilter(index));

        promotionService.setUpPromotion(ptpPromotion, search, 3);

        //Now delete the index
        indexService.deleteIndex(index);

        //Navigate to the promotion - this will time out if it can't get to the Promotions Detail Page
        PromotionsDetailPage pdp = promotionService.goToDetails(ptpPromotion);

        //Get the promoted documents, there should still be three
        List<String> promotionTitles = pdp.getPromotedTitles();
        assertThat(promotionTitles.size(),is(3));

        //All documents should know be 'unknown documents'
        for(String promotionTitle : promotionTitles){
            assertThat(promotionTitle,is("Unknown Document"));
        }
    }

    @Test
    public void testIndexNameWithSpaceDoesNotGiveInvalidNameNotifications(){

    }

    @After
    public void tearDown(){
        try {
            hsoApplication.createConnectionService(hsoElementFactory).deleteAllConnections();
            hsoApplication.createIndexService(hsoElementFactory).deleteAllIndexes();
        } catch (Exception e) {
            logger.warn("Failed to tear down");
        }
    }
}
