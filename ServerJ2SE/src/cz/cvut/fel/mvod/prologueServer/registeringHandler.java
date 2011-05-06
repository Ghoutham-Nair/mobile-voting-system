package cz.cvut.fel.mvod.prologueServer;
/*
Copyright 2011 Radovan Murin

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import cz.cvut.fel.mvod.global.GlobalSettingsAndNotifier;
import java.io.*;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xmlpull.v1.XmlPullParserException;
import java.util.HashMap;

/**
 * An HTTP handler that handles incoming traffic to the prologue server. It handles new user creating and information web page output.
 * 
 * @author Radovan Murin
 */
public class registeringHandler implements HttpHandler {

    XMLFactory wpb = new XMLFactory();
    webPageLocalizer introPage = new webPageLocalizer("index");

    public void handle(HttpExchange he) throws IOException {
        if (he.getRequestMethod().equalsIgnoreCase("GET")) {
            InputStream is = he.getRequestBody();
            String URI = he.getRequestURI().toString();
            String msg = null;
            String aLang = he.getRequestHeaders().getFirst("Accept-language");
            String[] langs = aLang.split(",");
            String responce = "";

            if (URI.equals("/")) {
                Headers heads = he.getResponseHeaders();
                heads.add("Content-Type", "text/html");

                responce = introPage.getWP(langs);
            } else if (URI.equals("/registration")) {
                Headers heads = he.getResponseHeaders();
                heads.add("Content-Type", "text/html");
                responce = generateRegWebPage();
            } else {
                responce = GlobalSettingsAndNotifier.singleton.messages.getString("404Error");
            }
            if(responce.equals("")) responce = "Error";

            OutputStream s = he.getResponseBody();
            is.close();
            he.sendResponseHeaders(200, responce.getBytes().length);
            System.out.println("Sending a responce");
            System.out.println(responce);
            s.write(responce.getBytes());
            s.close();
        } else if (he.getRequestMethod().equalsIgnoreCase("POST")) {

            InputStream is = he.getRequestBody();
            OutputStream out = he.getResponseBody();
            String responce = "OK";
            String msg = null;
            int bodyL = Integer.parseInt(he.getRequestHeaders().getFirst("Content-length"));

            byte[] mesidz = new byte[bodyL];
            is.read(mesidz);
            msg = new String(mesidz);
            responce = parsePost(msg);
            he.sendResponseHeaders(200, responce.getBytes().length);
            out.write(responce.getBytes());
            out.close();






            //System.out.println("Message stands " + new String(mesidz));
        } else {
            String responce = "<h2> hohoho</h2>";
            OutputStream s = he.getResponseBody();
            s.write(responce.getBytes());
            s.flush();
            he.sendResponseHeaders(200, responce.getBytes().length);
        }

    }
/**
 * Verifies that the inputted passwords match
 * @param p1 password1
 * @param p2 password2
 * @return true if the passwords match
 */
    private boolean passCheck(String p1, String p2) {
        return p1.equals(p2);
    }
/**
 * Returns the fallback introduction web page
 * @param sa
 * @return
 * @throws XmlPullParserException
 * @throws IOException
 */
    protected String generateMainWebPage(String[] sa) throws XmlPullParserException, IOException {
        wpb = new XMLFactory();
        return wpb.makeIntroPage(GlobalSettingsAndNotifier.singleton.getSetting("PUBLIC_IP"), Integer.parseInt(GlobalSettingsAndNotifier.singleton.getSetting("HTTP_PORT")));
    }
/**
 * Returns the registration web page
 * @return the string representation of the web page.
 */
    protected String generateRegWebPage() {
        FileOperator fr = new FileOperator();
        return fr.getWholeTextFile("regpage.html");
    }
/**
 * Parses the body of the post method.
 * @param body the body that needs parsing
 * @return the message that is to applear to the end user, informing the succes/failure of the request.
 */
    protected String parsePost(String body) {
        String[] tokens = body.split("&");


        HashMap<String, String> pairs = new HashMap<String, String>();
        try {
            for (int i = 0; i < tokens.length; i++) {

                String[] pair = tokens[i].split("=");
                pairs.put(pair[0], pair[1]);
            }
        } catch (Exception ex) {
            return "<p>" + GlobalSettingsAndNotifier.singleton.messages.getString("fillAllMSG") + "</p>";


        }
        if (!passCheck(pairs.get("pass1"), pairs.get("pass2"))) {
            return "<p>" + GlobalSettingsAndNotifier.singleton.messages.getString("passMismatchErr") + "</p>";
        }
        wpb = new XMLFactory();
        if (!wpb.addRegistrationEntry(pairs)) {
            return "<p>" + GlobalSettingsAndNotifier.singleton.messages.getString("usernameExistsErr") + "</p>";
        }
        return "OK";

    }
}