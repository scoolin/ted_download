import com.scoolin.ted_download.Downloader;
import io.vertx.core.Vertx;

/**
 * @author scoolin on 2017-09-03.
 */

public class App {

    public static void main(String[] args) {
        Downloader app = new Downloader(Vertx.vertx());
        app.start();
    }

}
