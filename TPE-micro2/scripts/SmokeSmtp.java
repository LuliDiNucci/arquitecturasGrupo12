import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import java.util.concurrent.CountDownLatch;

/** SMTP efímero del smoke: reutiliza GreenMail, ya presente en los tests. */
public class SmokeSmtp {
    public static void main(String[] args) throws Exception {
        var server = new GreenMail(new ServerSetup(Integer.parseInt(args[0]), "127.0.0.1", "smtp"));
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        new CountDownLatch(1).await();
    }
}
