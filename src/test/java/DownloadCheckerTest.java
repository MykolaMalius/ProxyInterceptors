import com.jayway.restassured.response.Cookies;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.proxy.ProxyServer;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.Test;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.testng.AssertJUnit.assertTrue;

/**
 * Created by M.Malyus on 2/9/2018.
 */
public class DownloadCheckerTest {

    @Test
    public void checkIfFileIsDownloaded() throws Exception {
        ProxyServer proxyServer = new ProxyServer(8081);
        proxyServer.start();
        proxyServer.setCaptureHeaders(true);
        proxyServer.setCaptureContent(true);
        proxyServer.newHar("results//DownloadedFile.har");

        /*Add our interceptor to http response */
        HttpResponseInterceptor downloaderChecker = new FileDownloaderInterceptor().
                addTypeOfContent("OperaSetup.exe");
        proxyServer.addResponseInterceptor(downloaderChecker);

        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability(CapabilityType.PROXY, proxyServer.seleniumProxy());
        System.setProperty("webdriver.chrome.driver", "chromedriverv2.35.exe");
        WebDriver driver = new ChromeDriver(caps);
        driver.manage().window().maximize();    
        driver.get("http://10.10.0.77:8080");
        /*Download our file*/
        driver.findElement(By.xpath("//a[@class='button os-windows']")).click();
        String fileName = driver.findElement(By.tagName("body")).getText();
        assertTrue(new File(fileName).exists());

        Har har = proxyServer.getHar();
        har.writeTo(new File("//results//Download.har"));
        Thread.sleep(3000);
        driver.quit();
        proxyServer.stop();

    }
    public static class FileDownloaderInterceptor implements HttpResponseInterceptor{
        private Set<String> contentTypes = new HashSet<String>();
        private File tempDir = null;
        private File tempFile = null;

        public FileDownloaderInterceptor addTypeOfContent(String contentType){
            contentTypes.add(contentType);
            return this;
        }

        @Override
        public void process(HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {
         /*Check for valid content-type
         * if content type is valid whe get him.Than proxy read file and
          * write it to the temp file*/
         String contentType = httpResponse.getFirstHeader("Content-Type").getValue();
         if(contentType.contains(contentType)){
             String postFix = contentType.substring(contentType.indexOf('/' + 1));
             tempFile = File.createTempFile("downloaded", "."+postFix, tempDir);
             tempFile.deleteOnExit();
             FileOutputStream outputStream = new FileOutputStream(tempFile);
             outputStream.write(EntityUtils.toByteArray(httpResponse.getEntity()));
             outputStream.close();

             httpResponse.removeHeaders("Content-Type");
             httpResponse.removeHeaders("Content-Encoding");
             httpResponse.removeHeaders("Content-Disposition");

             httpResponse.addHeader("Content-Type","text/html");
             httpResponse.addHeader("Content-Length","" + tempFile.getAbsolutePath().length());
             httpResponse.setEntity(new StringEntity(tempFile.getAbsolutePath()));

         }
        }
    }
}
