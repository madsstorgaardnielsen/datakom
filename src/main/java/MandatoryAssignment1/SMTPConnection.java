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

    /* The socket to the server */
    private Socket connection;
    /* Streams for reading and writing the socket */
    private BufferedReader fromServer;
    private DataOutputStream toServer;
    /* Are we connected? Used in close() to determine what to do. */
    private boolean isConnected;

    /* Create an SMTPConnection object. Create the socket and the
       associated streams. Initialize SMTP connection. */
    public SMTPConnection(Envelope envelope) throws IOException {
        connection = new Socket(envelope.DestAddr, SMTP_PORT);
        fromServer = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        toServer = new DataOutputStream(connection.getOutputStream());

        String serverReply = fromServer.readLine();
        if (parseReply(serverReply) != 220) {
            System.out.println("SMTP Connection fejl");
            System.out.println(serverReply);
            throw new IOException();
        }

        String localhost = InetAddress.getLocalHost().getHostName();

        sendCommand("HELO " + localhost, 250);

        isConnected = true;
    }

    /* Send the message. Write the correct SMTP-commands in the
       correct order. No checking for errors, just throw them to the
       caller. */
    public void send(Envelope envelope) throws IOException {
        sendCommand("MAIL FROM: <" + envelope.Sender + ">", 250);
        sendCommand("RCPT TO: <" + envelope.Recipient + ">", 250);
        sendCommand("DATA", 354);

        //besked
        sendCommand(envelope.Message.toString() + CRLF + ".", 250);
    }

    /* Close the connection. First, terminate on SMTP level, then
       close the socket. */
    public void close() {
        isConnected = false;
        try {
            sendCommand("QUIT", 221);
            connection.close();
        } catch (IOException e) {
            System.out.println("Unable to close connection: " + e);
            isConnected = true;
        }
    }

    /* Send an SMTP command to the server. Check that the reply code is
       what is is supposed to be according to RFC 821. */
    private void sendCommand(String command, int rc) throws IOException {
        String reply;

        toServer.writeBytes(command + CRLF);
        if (rc == 0)
            return;

        reply = fromServer.readLine();
        if (parseReply(reply) != rc) {
            throw new IOException();
        }
    }

    /* Parse the reply line from the server. Returns the reply code. */
    private int parseReply(String reply) {
        StringTokenizer parser = new StringTokenizer(reply);
        String replyToken = parser.nextToken();
        return new Integer(replyToken);
    }

    /* Destructor. Closes the connection if something bad happens. */
    protected void finalize() throws Throwable {
        if (isConnected) {
            close();
        }
        super.finalize();
    }
}
