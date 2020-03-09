package MandatoryAssignment1;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 * Open an SMTP connection to a mailserver and send one mail.
 */
public class SMTPConnection {
    private static final int SMTP_PORT = 25;
    private static final String CRLF = "\r\n";

    private Socket connection;
    private BufferedReader fromServer;
    private DataOutputStream toServer;
    private boolean isConnected;

    SMTPConnection(Envelope envelope) throws IOException {
        //a socket connection object, the parameters are destination address and SMTP port, when both computers have this information, they are able to communicate
        connection = new Socket(envelope.DestAddr, SMTP_PORT);

        //buffered reader reads the answers from the server and saves it in the variable "fromServer"
        fromServer = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        //Outputstream, this is what i send to the server
        toServer = new DataOutputStream(connection.getOutputStream());

        //when the server replies, the if statement checks the reply, and if it is anything else than 220 (The server is ready (response to the client’s attempt to establish a TCP connection))
        //then i print out the reply and throw an exception
        String serverReply = fromServer.readLine();
        if (parseReply(serverReply) != 220) {
            System.out.println("SMTP Connection fejl");
            System.out.println(serverReply);
            throw new IOException();
        }

        //a variable to save my IP address
        String localhost = InetAddress.getLocalHost().getHostName();

        //initial command to start the connection with the server
        sendCommand("HELO " + localhost, 250);

        isConnected = true;
    }

    //"the message" which is sent for more information, please go to the message Class
    void send(Envelope envelope) throws IOException {
        sendCommand("MAIL FROM: <" + envelope.Sender + ">", 250);
        sendCommand("RCPT TO: <" + envelope.Recipient + ">", 250);
        sendCommand("DATA", 354);
        sendCommand(envelope.Message.toString() + CRLF + ".", 250);
    }

    //this method is for closing the connection to the server, the code 221 means the server closed the transmission channel
    void close() {
        isConnected = false;
        try {
            sendCommand("QUIT", 221);
            connection.close();
        } catch (IOException e) {
            System.out.println("Unable to close connection: " + e);
            isConnected = true;
        }
    }

    //this function sends the command + CRLF to the server, and then the reply is saved in the reply variable, then the if statement checks if we got the rc we expected.
    private void sendCommand(String command, int rc) throws IOException {
        String reply;
        toServer.writeBytes(command + CRLF);
        reply = fromServer.readLine();
        if (parseReply(reply) != rc) {
            throw new IOException();
        }
    }

    //this method breaks the reply into a "token" and parses it into an integer
    private int parseReply(String reply) {
        StringTokenizer parser = new StringTokenizer(reply);
        String replyToken = parser.nextToken();
        return new Integer(replyToken);
    }


    protected void finalize() throws Throwable {
        if (isConnected) {
            close();
        }
        super.finalize();
    }
}
