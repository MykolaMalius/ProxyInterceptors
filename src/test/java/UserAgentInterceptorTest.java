import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.proxy.ProxyServer;
import net.lightbody.bmp.proxy.http.BrowserMobHttpRequest;
import net.lightbody.bmp.proxy.http.RequestInterceptor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

/**
 * Created by M.Malyus on 2/8/2018.
 */
public class UserAgentInterceptorTest {

    @Test
    public void checkInterceptorOfBrowser() throws Exception {
        ProxyServer proxyServer = new ProxyServer(8081);
        proxyServer.setCaptureHeaders(true);
        proxyServer.setCaptureContent(true);
        proxyServer.start();
        /*Add request to make IOS version of browser*/
        RequestInterceptor userAgentChanger = new UserAgentChanger("Mozilla/5.0 (iPhone; CPU iPhone OS 10_3 like Mac OS X) AppleWebKit/602.1.50 (KHTML, like Gecko) CriOS/56.0.2924.75 Mobile/14E5239e Safari/602.1");

        /* "GoogleChrome/5.0 (Windows10; U; Android 2.2; en-us; Nexus One Build/FRF91)" +
          "AppleWebKit/533.1 (KHTML like chrome) Version/4.0 Mobile Safari/533.1"*/
        //Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.140 Safari/537.36
        proxyServer.addRequestInterceptor(userAgentChanger);

        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability(CapabilityType.PROXY,proxyServer.seleniumProxy());
        System.setProperty("webdriver.chrome.driver", "chromedriverv2.35.exe");
        WebDriver driver = new ChromeDriver(caps);
        //driver.manage().window().maximize();
        driver.get("http://localhost:8080");
        /*WebElement button = driver.findElement(By.xpath("//div[@class=\"nav-list\"]//ul[@class=\"nav navbar-nav navbar-right\"]//li[4]"));
        Assert.assertEquals(button.getText(),"Get One Right Now");*/
        /*Stop driver and then our proxy server*/
        Har har = proxyServer.getHar();
        har.writeTo(new File("results//Network.json"));
        driver.quit();
        proxyServer.stop();

    }
    public static class UserAgentChanger implements RequestInterceptor{
        private String userAgent;

        public UserAgentChanger(String userAgent){
            this.userAgent = userAgent;
        }
        @Override
        public void process(BrowserMobHttpRequest requestHttp) {
            requestHttp.addRequestHeader("User-Agent",userAgent);
        }
    }
}
