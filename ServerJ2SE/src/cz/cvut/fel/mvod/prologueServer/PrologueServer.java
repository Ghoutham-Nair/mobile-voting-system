package cz.cvut.fel.mvod.prologueServer;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.*;
import cz.cvut.fel.mvod.global.GlobalSettingsAndNotifier;
import cz.cvut.fel.mvod.global.Notifiable;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.*;
import java.security.cert.*;
import javax.net.ssl.*;

/**
 *
 * @author Murko
 */
public class PrologueServer implements Notifiable {

    public static final int STATE_REGISTERING = 1;
    public static final int STATE_PROVIDING = 2;
    public static final int STATE_INACTIVE = 3;
    HttpsServer s;
    SSLContext sslContext;
    HttpsServer server;

    public PrologueServer() throws IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, KeyManagementException {


        GlobalSettingsAndNotifier.singleton.addListener(this);

        server = HttpsServer.create(new InetSocketAddress(10443), -1);
        server.createContext("/", new registeringHandler());
        server.setExecutor(null);

        char[] passphrase = "passphrase".toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("testkeys"), passphrase);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);
        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        server.setHttpsConfigurator(new HttpsConfigurator(ssl) {

            public void configure(HttpsParameters params) {
                InetSocketAddress remote = params.getClientAddress();
                SSLContext c = getSSLContext();
                SSLParameters sslparams = c.getDefaultSSLParameters();
                params.setSSLParameters(sslparams);

            }
        });
        server.start();
        GlobalSettingsAndNotifier.singleton.modifySettings("prologueState", STATE_REGISTERING + "",false);



    }

    private void stopRegistration() {

        server.removeContext("/");
        server.createContext("/", new ProvidingHandler());

    }

    private void stopServer() {

        server.stop(1);

    }

    private void changeState(int state) {
        //if(state == )
        switch (state) {
            case STATE_REGISTERING:
                break;
            case STATE_PROVIDING:
                stopRegistration();
                break;
            case STATE_INACTIVE:
                stopServer();
                break;
            default:
                break;
        }
        GlobalSettingsAndNotifier.singleton.modifySettings("prologueState", state + "",false);

    }

    public int getState() {
        return Integer.parseInt(GlobalSettingsAndNotifier.singleton.getSetting("prologueState"));
    }

    @Override
    public void notifyOfChange() {
        changeState(Integer.parseInt(GlobalSettingsAndNotifier.singleton.getSetting("prologueState")));
    }
    
}