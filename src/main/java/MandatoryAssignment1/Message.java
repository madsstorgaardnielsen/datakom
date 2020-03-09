package MandatoryAssignment1;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.text.*;

/* $Id: Message.java,v 1.5 1999/07/22 12:10:57 kangasha Exp $ */

/**
 * Mail message.
 *
 * @author Jussi Kangasharju
 */
public class Message {
    private static final String CRLF = "\r\n";
    public String Headers;
    public String Body;
    private String From;
    private String To;
    public String base64EncodedPic = encodeToString();

    public Message(String from, String to, String subject, String text) throws IOException {
        From = from.trim();
        To = to.trim();
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'");
        String dateString = format.format(new Date());

        Headers = "MIME-Version: 1.0" + CRLF;
        Headers += "Date: " + dateString + CRLF;
        Headers += "From: Mads Storgaard-Nielsen " + "<" + From + ">" + CRLF;
        Headers += "To: " + To + CRLF;
        Headers += "Subject: " + subject.trim() + CRLF;
        Headers += "Content-Type: multipart/mixed; boundary=\"sep\"" + CRLF;

        Body = "--sep" + CRLF;
        Body += "Content-Type: text/plain; charset=\"us-ascii\"" + CRLF + CRLF;
        Body += text + CRLF + CRLF;
        Body += "--sep";

        Body += "Content-Type:image/png; name=dtu.png" + CRLF;
        Body += "Content-Disposition: attachment;filename=\"bannedXD.png\"" + CRLF;
        Body += "Content-transfer-encoding: base64" + CRLF + CRLF;
        Body += base64EncodedPic;
        Body += CRLF + CRLF + "--";

        Body += CRLF + CRLF + "--sep" + CRLF;
    }

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

    /* Two functions to access the sender and recipient. */
    public String getFrom() {
        return From;
    }

    public String getTo() {
        return To;
    }

    /* Check whether the message is valid. In other words, check that
       both sender and recipient contain only one @-sign. */
    public boolean isValid() {
        int fromat = From.indexOf('@');
        int toat = To.indexOf('@');

        if (fromat < 1 || (From.length() - fromat) <= 1) {
            System.out.println("Sender address is invalid");
            return false;
        }
        if (toat < 1 || (To.length() - toat) <= 1) {
            System.out.println("Recipient address is invalid");
            return false;
        }
        if (fromat != From.lastIndexOf('@')) {
            System.out.println("Sender address is invalid");
            return false;
        }
        if (toat != To.lastIndexOf('@')) {
            System.out.println("Recipient address is invalid");
            return false;
        }
        return true;
    }

    /* For printing the message. */
    public String toString() {
        String res;

        res = Headers + CRLF;
        res += Body;
        return res;
    }
}
