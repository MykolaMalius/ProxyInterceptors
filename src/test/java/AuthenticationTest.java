import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.proxy.ProxyServer;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by M.Malyus on 2/8/2018.
 */
public class AuthenticationTest {
    @Test
    public void checkAuthentication() throws Exception {
        ProxyServer proxyServer = new ProxyServer(8081);
        proxyServer.start();
        proxyServer.autoBasicAuthorization("","admin","password");

        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability(CapabilityType.PROXY,proxyServer.seleniumProxy());
        System.setProperty("webdriver.chrome.driver", "chromedriverv2.35.exe");
        WebDriver driver = new ChromeDriver(caps);
        driver.manage().window().maximize();
        driver.get("http://localhost:8080");
        WebElement button = driver.findElement(By.xpath("//div[@class=\"nav-list\"]//ul[@class=\"nav navbar-nav navbar-right\"]//li[4]"));
        Assert.assertEquals(button.getText(),"Get One Right Now");
        /*Stop driver and then our proxy server*/
        driver.quit();
        proxyServer.stop();
    }
}
