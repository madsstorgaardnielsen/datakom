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

    //the method encodes the string into base64, and then the string is saved in the variable
    public String base64EncodedPic = encodeToString();

    //a variable used to create boundaries within the sent email
    public String messageSeperator = "sep";

    public Message(String from, String to, String subject, String text) throws IOException {
        From = from.trim();
        To = to.trim();
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'");
        String dateString = format.format(new Date());

        //the headers
        Headers = "MIME-Version: 1.0" + CRLF; //MIME is a standard which supports text other than ASCII as well as attachments of different file formats such as images.
        Headers += "Date: " + dateString + CRLF;
        Headers += "From: Hans Peter Byager " + "<" + From + ">" + CRLF;
        Headers += "To: " + To + CRLF;
        Headers += "Subject: " + subject.trim() + CRLF;
        Headers += "Content-Type: multipart/mixed; boundary=\""+messageSeperator+"\"" + CRLF; //the content type multipart/mixed is used when a message is made up of different data types

        //the body, this has the text message in it
        Body = "--"+messageSeperator + CRLF;
        Body += "Content-Type: text/plain; charset=\"us-ascii\"" + CRLF + CRLF; //text/plain type is used when the text is readable text
        Body += text + CRLF + CRLF;
        Body += "--"+messageSeperator + CRLF;

        //the picture
        Body += "Content-Type:image/png; name=thumbsup.png" + CRLF; // image/png type, is used for pictures
        Body += "Content-Disposition: attachment;filename=\"thumbsup.png\"" + CRLF;
        Body += "Content-transfer-encoding: base64" + CRLF + CRLF; //content transfer encoding is used when the message has an encoded part
        Body += base64EncodedPic;
        Body += CRLF + CRLF + "--"+messageSeperator;

        //ending the message
        Body += CRLF + CRLF + "--"+messageSeperator+"--" + CRLF;
    }

    // a function which encodes the picture into base64
    public static String encodeToString() throws IOException {
        String base64encodedImage = null;
        BufferedImage pictureInput = ImageIO.read(new File("thumbsup.png"));
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

    public String toString() {
        String res;

        res = Headers + CRLF;
        res += Body;
        return res;
    }
}
