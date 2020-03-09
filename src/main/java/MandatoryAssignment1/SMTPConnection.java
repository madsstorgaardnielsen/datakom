package MandatoryAssignment1;

import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Base64;
import java.util.StringTokenizer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;


/**
 * Open an SMTP connection to a mailserver and send one mail.
 */
public class SMTPConnection {
    private static final int SMTP_PORT = 25;
    private static final String CRLF = "\r\n";

    //public String base64encodedImage;
    //public String imageString = encodeToString();

    /* The socket to the server */
    private Socket connection;
    /* Streams for reading and writing the socket */
    private BufferedReader fromServer;
    private DataOutputStream toServer;
    //
    /* Are we connected? Used in close() to determine what to do. */
    private boolean isConnected;

    /* Create an SMTPConnection object. Create the socket and the
       associated streams. Initialize SMTP connection. */
    public SMTPConnection(Envelope envelope) throws IOException {
        connection = new Socket(envelope.DestAddr, SMTP_PORT);
        fromServer = new BufferedReader(new InputStreamReader(connection.getInputStream()))/* Fill in */;
        toServer = new DataOutputStream(connection.getOutputStream()) /* Fill in */;

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

    public String imageString = encodeToString();

    public static String encodeToString() throws IOException {
        String base64encodedImage = null;
        BufferedImage pictureInput = ImageIO.read(new File("banned2.png"));
        ByteArrayOutputStream pictureOutput = new ByteArrayOutputStream();

        try {
            ImageIO.write(pictureInput, "png", pictureOutput);
            byte[] imageBytes = pictureOutput.toByteArray();

            base64encodedImage = Base64.getEncoder().encodeToString(imageBytes);

            pictureOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return base64encodedImage;
    }

    /* Send the message. Write the correct SMTP-commands in the
       correct order. No checking for errors, just throw them to the
       caller. */
    public void send(Envelope envelope) throws IOException {

        sendCommand("MAIL FROM: <" + envelope.Sender + ">", 250);
        sendCommand("RCPT TO: <" + envelope.Recipient + ">", 250);
        sendCommand("DATA", 354);
        sendCommand("Subject: s180076",0);
        sendCommand("MIME-Version: 1.0", 0);
        sendCommand("Content-Type:multipart/mixed;boundary=\"seperator\"", 0);
        sendCommand("--seperator", 0);

        // Filename and type to be sent
        sendCommand("Content-Type:application/octet-stream;name=\"banned2.png\"", 0);
        sendCommand("Content-Transfer-Encoding:base64", 0);
        sendCommand("Content-Disposition:attachment;filename=\"banned2.png\"", 0);
        sendCommand("", 0);

        //base 64 encoded billede
        sendCommand(imageString, 0);

        sendCommand("", 0);
        sendCommand("", 0);
        sendCommand("--seperator", 0);
        sendCommand(" ", 0);
        sendCommand(" ", 0);

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
