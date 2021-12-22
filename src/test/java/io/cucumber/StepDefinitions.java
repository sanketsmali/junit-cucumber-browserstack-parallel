package io.cucumber;

import com.browserstack.local.Local;
import com.saucelabs.saucerest.SauceREST;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;



import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class StepDefinitions {
    private WebDriver driver;
    private String sessionId;
    private WebDriverWait wait;
    private Local l;
    

	public static final String AUTOMATE_USERNAME = "sanketmali4";
	public static final String AUTOMATE_ACCESS_KEY = "Nt7WxbXzjUzT8kfpbEKC";
	public static final String URL = "https://" + AUTOMATE_USERNAME + ":" + AUTOMATE_ACCESS_KEY + "@hub-cloud.browserstack.com/wd/hub";

    private final String BASE_URL = "https://www.saucedemo.com";
    private static JSONObject config;
   

    @Parameter(value = 0)
    public int taskID;


    @Before
    public void setUp(Scenario scenario) throws Exception {
    	
    	
    
    	 List<Integer> taskIDs = new ArrayList<Integer>();
         
         JSONParser  parser = new JSONParser();
             config = (JSONObject) parser.parse(new FileReader("src/test/resources/conf/caps.json"));
             int env = ((JSONArray)config.get("environments")).size();

             for(int i=0; i<env; i++) {
               taskIDs.add(i);
             }
    	
    	  JSONArray envs = (JSONArray) config.get("environments");

          DesiredCapabilities capabilities = new DesiredCapabilities();

          Map<String, String> envCapabilities = (Map<String, String>) envs.get(taskID);
          Iterator it = envCapabilities.entrySet().iterator();
          while (it.hasNext()) {
              Map.Entry pair = (Map.Entry)it.next();
              capabilities.setCapability(pair.getKey().toString(), pair.getValue().toString());
          }
          
          Map<String, String> commonCapabilities = (Map<String, String>) config.get("capabilities");
          it = commonCapabilities.entrySet().iterator();
          while (it.hasNext()) {
              Map.Entry pair = (Map.Entry)it.next();
              if(capabilities.getCapability(pair.getKey().toString()) == null){
                  capabilities.setCapability(pair.getKey().toString(), pair.getValue().toString());
              }
          }

          String username = System.getenv("BROWSERSTACK_USERNAME");
          if(username == null) {
              username = (String) config.get("user");
          }

          String accessKey = System.getenv("BROWSERSTACK_ACCESS_KEY");
          if(accessKey == null) {
              accessKey = (String) config.get("key");
          }

          if(capabilities.getCapability("browserstack.local") != null && capabilities.getCapability("browserstack.local") == "true"){
              l = new Local();
              Map<String, String> options = new HashMap<String, String>();
              options.put("key", accessKey);
              l.start(options);
          }
          System.out.println(scenario.getName());
          capabilities.setCapability("name", scenario.getName());
          driver = new RemoteWebDriver(new URL("https://"+username+":"+accessKey+"@"+config.get("server")+"/wd/hub"), capabilities);
       
          JavascriptExecutor jse = (JavascriptExecutor)driver;
    }

    @After
    public void tearDown(Scenario scenario){
        driver.quit();
       
    }

    @Given("^I go to the login page$")
    public void go_to_login_page() {
        driver.get(BASE_URL);
    }

    @Given("I am on the inventory page")
    public void go_to_the_inventory_page(){
        driver.get(BASE_URL + "/inventory.html");
    }

    @When("I login as a valid user")
    public void login_as_valid_user() {
        login("standard_user", "secret_sauce");
    }

    @When("I login as an invalid user")
    public void login_as_invalid_user() {
        login("doesnt_exist", "secret_sauce");
    }

    /**
     * Use this method to send any number of login/password parameters, to test different edge cases or roles within
     * the software. This method exists to show an example of how steps can call other parameterized methods.
     * @param username The user name to login with
     * @param password The password to use (for testing the password field
     */
    private void login(String username, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        driver.findElement(By.id("user-name")).sendKeys(username);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        driver.findElement(By.id("password")).sendKeys(password);

        driver.findElement(By.className("btn_action")).click();
    }

    @When("^I add (\\d+) items? to the cart$")
    public void add_items_to_cart(int items){
        By itemButton = By.className("btn_primary");

        IntStream.range(0, items).forEach(i -> {
            wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(itemButton)));
            driver.findElement(itemButton).click();
        });
    }

    @And("I remove an item")
    public void remove_an_item(){
        By itemButton = By.className("btn_secondary");

        wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(itemButton)));
        driver.findElement(itemButton).click();
    }

    @Then("I have (\\d) items? in my cart")
    public void one_item_in_cart(Integer items){
        String expected_items = items.toString();

        By itemsInCart = By.className("shopping_cart_badge");

        wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(itemsInCart)));
        Assert.assertEquals(driver.findElement(itemsInCart).getText(), expected_items);
    }

    @Then("The item list is not displayed")
    public void item_list_is_not_diplayed() {
        Assert.assertEquals(driver.findElements(By.id("inventory_container")).size(), 0);
    }

    @Then("The item list is displayed")
    public void item_list_is_diplayed() {
        Assert.assertTrue(driver.findElement(By.id("inventory_container")).isDisplayed());
    }
}
