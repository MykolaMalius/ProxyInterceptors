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
public class ProxyTest {

    /*HAR is a HTTP Archive format.This format is using for
    * getting info from HTTP monitoring.Its like JSON file with UTF-8.
    * We can just save by .java streams this HTTP traffic.
    * In this test we just get .har file with network status.*/
    @Test
    public void checkProxyStatus() throws Exception {
        ProxyServer proxyServer = new ProxyServer(8081);
        proxyServer.start();
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability(CapabilityType.PROXY,proxyServer.seleniumProxy());
        System.setProperty("webdriver.chrome.driver", "chromedriverv2.35.exe");
        WebDriver driver = new ChromeDriver(caps);
        /*Creating HAR with name 'proxystatus'*/
        proxyServer.newHar("proxystatus");
        driver.manage().window().maximize();
        driver.get("");
        /*Getting info from HAR*/
        Har har = proxyServer.getHar();
        try{
            File file = new File("results//ChromeTraffic.har");
            if(!file.exists()){
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            try{
                har.writeTo(fos);
            }
            finally {
                fos.close();
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
        /*WebElement button = driver.findElement(By.xpath("//div[@class=\"nav-list\"]//ul[@class=\"nav navbar-nav navbar-right\"]//li[4]"));
        Assert.assertEquals(button.getText(),"Get One Right Now");*/
        /*Stop driver and then our proxy server*/
        driver.quit();
        proxyServer.stop();

    }
}
