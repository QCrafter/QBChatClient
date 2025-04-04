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
        val window = JFrame()
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
        val textInput = JTextField("Enter user name: ")
        textInput.setBorder(LineBorder(Color.red, 1))
        textInput.setBounds(0, 800, 600, 100)

//TODO IP-Addresse über GUI ändern

        textInput.addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent) {
                if (e.keyChar == '\n') {
                    if (userName == "") {
                        userName = textInput.text.replace("Enter user name: ", "")
                        textInput.text = ""
                    } else {
                        val url =
                            "http://192.168.4.175/" + userName + ":_!_" + textInput.text.replace(
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

            text = sendGetRequest("http://192.168.4.175/")
            if (text != oldText) {
                messageList.text = text.replace("_!_", " ")
                oldText = text
            }

        }
        timer.start()
    }
}
