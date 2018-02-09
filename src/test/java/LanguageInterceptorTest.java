import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.proxy.ProxyServer;
import net.lightbody.bmp.proxy.http.BrowserMobHttpRequest;
import net.lightbody.bmp.proxy.http.RequestInterceptor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Created by M.Malyus on 2/8/2018.
 */
public class LanguageInterceptorTest {

    @Test    public void checkLocalization() throws Exception {
        ProxyServer proxyServer = new ProxyServer(8081);
        proxyServer.start();

        RequestInterceptor languageChanger = new LanguageInterceptor("en, ru");
        proxyServer.addRequestInterceptor(languageChanger);
        proxyServer.setCaptureHeaders(true);
        proxyServer.setCaptureContent(true);
        proxyServer.newHar("results//Accept-Language.har");

        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability(CapabilityType.PROXY,proxyServer.seleniumProxy());
        System.setProperty("webdriver.chrome.driver", "chromedriverv2.35.exe");
        WebDriver driver = new ChromeDriver(caps);
        /*Creating HAR with name 'proxystatus'*/
        proxyServer.newHar("proxystatus");
        driver.manage().window().maximize();
        driver.get("http://toolsqa.com/");
        /*Getting info from HAR*/
        Har har = proxyServer.getHar();
        har.writeTo(new File("results//Accept-Language.har"));
        /*Stop driver and then our proxy server*/
        driver.quit();
        proxyServer.stop();
    }
    public static class LanguageInterceptor implements RequestInterceptor {
        private String language;

        public LanguageInterceptor(String language){
            this.language = language;
        }
        @Override
        public void process(BrowserMobHttpRequest requestHttp) {
            requestHttp.addRequestHeader("Accept-Language",language);
        }
    }
}
