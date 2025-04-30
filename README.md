Client for qBruce Chat Server

```kotlin
import java.awt.Color
import java.awt.Font
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import javax.swing.*
import javax.swing.border.LineBorder


fun sendGetRequest(url: String): String {
    val client = HttpClient.newHttpClient()

    val request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .GET()
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}


fun main() {

    SwingUtilities.invokeLater {
        val window = JFrame("QBChat Client")
        window.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        window.isResizable = false
        window.layout = null
        window.setSize(600, 950)
        val messageList = JTextArea()
        messageList.setBorder(LineBorder(Color.blue, 1))
        messageList.setBounds(0, 0, 600, 780)
        messageList.isEditable = false
        messageList.text = "Waiting for server..."
        messageList.font = Font("New", 1, 35)
        val scrollField = JScrollPane(messageList)
        scrollField.setBounds(0, 0, 600, 780)
        var userName = ""
        var ip = ""
        val textInput = JTextField("Enter user name: ")
        textInput.setBorder(LineBorder(Color.red, 1))
        textInput.setBounds(0, 800, 600, 100)

        textInput.addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent) {
                if (e.keyChar == '\n') {
                    if (userName == "") {
                        userName = textInput.text.replace("Enter user name: ", "")
                        textInput.text = "Enter Server IP address: "
                    } else if (ip == "") {
                        ip = textInput.text.replace("Enter Server IP address: ", "")
                        textInput.text = ""
                    } else {
                        val url =
                            "http://$ip/$userName:_!_" + textInput.text.replace(
                                " ",
                                "_!_"
                            ) + "_qbchat_"
                        messageList.text = sendGetRequest(url).replace("_!_", " ")
                        textInput.text = ""
                    }
                }
            }

            override fun keyPressed(e: KeyEvent) {}
            override fun keyReleased(e: KeyEvent) {}
        })

        window.add(scrollField)
        window.add(textInput)
        window.isVisible = true
        var oldText = ""
        var text: String
        val timer = Timer(1000) {
            if (ip != "") {
                text = sendGetRequest("http://$ip")
                if (text != oldText) {
                    messageList.text = text.replace("_!_", " ")
                    oldText = text
                }
            }
        }
        timer.start()
    }
}
```

Server (part of my unreleased qBruce Esp32 Firmware forked from https://github.com/pr3y/Bruce):
```cpp
void qbchat_server() {
    delay(200);
    wifiConnectMenu(WIFI_STA);
    tft.fillScreen(bruceConfig.bgColor);
    String ip = WiFi.localIP().toString();
    String oldLine;
    String httpResponseText = "Hello World\n";
    tft.drawCentreString(ip, 120, 62, SMOOTH_FONT);
    WiFiServer server(80);
    server.begin();
    while (!check(PrevPress)) {
        WiFiClient client = server.available();   // listen for incoming clients
       
         if (client) {                             // if you get a client,
           String currentLine = "";                // make a String to hold incoming data from the client
           while (client.connected()) {            // loop while the client's connected
             if (client.available()) {             // if there's bytes to read from the client,
               char c = client.read();             // read a byte, then
               if (c == '\n') {                    // if the byte is a newline character
       
                 // if the current line is blank, you got two newline characters in a row.
                 // that's the end of the client HTTP request, so send a response:
                 if (currentLine.length() == 0) {
                   // HTTP headers always start with a response code (e.g. HTTP/1.1 200 OK)
                   // and a content-type so the client knows what's coming, then a blank line:
                   client.println("HTTP/1.1 200 OK");
                   client.println("Content-type:text/html");
                   client.println();
       
                   // the content of the HTTP response follows the header:
                   client.print(httpResponseText);
       
                   // The HTTP response ends with another blank line:
                   client.println();
                   // break out of the while loop:
                   break;
                 } else {    // if you got a newline, then clear currentLine:
                   currentLine = "";
                 }
               } else if (c != '\r') {  // if you got anything else but a carriage return character,
                 currentLine += c;      // add it to the end of the currentLine
               }
       //read and send message
               if (currentLine != "" && currentLine != oldLine && currentLine != "_qbchat_" && currentLine.endsWith("_qbchat_")) {
                String message = currentLine.substring(5,currentLine.length()-8);
                Serial.println("CURRENT LINE:  "+currentLine);
                httpResponseText += (message+"\n");
                resetTftDisplay();
                tft.println(replaceAll(message, "_!_", " "));
                oldLine = currentLine;
               }
             } 
           }
           // close the connection:
           client.stop();
           Serial.println("Client Disconnected.");
         }
        }
    }
```

