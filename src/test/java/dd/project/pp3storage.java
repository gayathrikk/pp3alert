package dd.project;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.testng.annotations.Test;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;

public class pp3storage {
	 @Test
	    public void testStorageDetails() {
	        JSch jsch = new JSch();
	        com.jcraft.jsch.Session session = null;
	        try {
	            String user = "appUser";
	            String host = "pp3.humanbrain.in";
	            String password = "Brain@123";  // ⚠ Consider using environment variables instead.
	            int port = 22;
	            session = jsch.getSession(user, host, port);
	            session.setPassword(password);
	            session.setConfig("StrictHostKeyChecking", "no");
	            session.connect();

	            Channel channel = session.openChannel("exec");
	            ((ChannelExec) channel).setCommand("ls -lh --time-style=long-iso /mnt/local/nvmestorage/postImageProcessor");
	            channel.setInputStream(null);
	            ((ChannelExec) channel).setErrStream(System.err);
	            InputStream in = channel.getInputStream();
	            channel.connect();

	            byte[] tmp = new byte[1024];
	            StringBuilder output = new StringBuilder();
	            while (true) {
	                while (in.available() > 0) {
	                    int i = in.read(tmp, 0, 1024);
	                    if (i < 0) break;
	                    output.append(new String(tmp, 0, i));
	                }
	                if (channel.isClosed()) {
	                    if (in.available() > 0) continue;
	                    break;
	                }
	                Thread.sleep(1000);
	            }

	            channel.disconnect();
	            session.disconnect();

	            String[] lines = output.toString().split("\n");
	            System.out.println("Files in  /mnt/local/nvmestorage/postImageProcessor:\n");

	            int todayFileCount = 0;
	            int oldFileCount = 0;
	            StringBuilder todayFiles = new StringBuilder();
	            StringBuilder oldFiles = new StringBuilder();
	        
	            // Define date format and today's date outside the loop
	            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	            String todayDate = sdf.format(new Date());

	            for (String line : lines) {
	                System.out.println("DEBUG: " + line);  // Print each line for debugging

	                if (!line.startsWith("total") && !line.startsWith("drwx")) {
	                    String[] parts = line.trim().split("\\s+", 8); // Splitting properly

	                    if (parts.length >= 8) {
	                        String fileDate = parts[5];   // Picking date in YYYY-MM-DD format
	                        String fileName = parts[7];   // This should be the filename

	                        System.out.println("Parsed Date: " + fileDate + ", File: " + fileName); // Debugging output

	                        if (fileDate.equals(todayDate)) {
	                            todayFileCount++;
	                            todayFiles.append("<span style='color:red;'>" + fileDate + " - " + fileName + "</span><br>");
	                        } else {
	                            oldFileCount++;
	                            oldFiles.append(fileDate + " - " + fileName + "<br>");
	                        }
	                    }
	                }
	            }

	            // **Send email only if old files exist**
	            if (oldFileCount > 0) {  
	                sendEmailAlert(todayFiles.toString(), oldFiles.toString(), todayFileCount, oldFileCount, host);
	            } else {
	                System.out.println("No old files found. Email not sent.");
	            }

	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }  

	    private void sendEmailAlert(String messageBody) {
	        // Recipient's email ID needs to be mentioned.
	  String[] to = {"nathan.i@htic.iitm.ac.in"};
       String[] cc = {"venip@htic.iitm.ac.in", "nitheshkumarsundhar@gmail.com"};

        String[] bcc = {"divya.d@htic.iitm.ac.in"};
	    
	        // Sender's email ID needs to be mentioned
	        String from = "automationsoftware25@gmail.com";
	        // Assuming you are sending email through Gmail's SMTP
	        String host = "smtp.gmail.com";
	        // Get system properties
	        Properties properties = System.getProperties();
	        // Setup mail server
	        properties.put("mail.smtp.host", host);
	        properties.put("mail.smtp.port", "465");
	        properties.put("mail.smtp.ssl.enable", "true");
	        properties.put("mail.smtp.auth", "true");
	        // Get the Session object and pass username and password
	        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
	            protected PasswordAuthentication getPasswordAuthentication() {
	                return new PasswordAuthentication("automationsoftware25@gmail.com", "wjzcgaramsqvagxu");
	            }
	        });
	        // Used to debug SMTP issues
	        session.setDebug(true);
	        try {
	            // Create a default MimeMessage object.
	            MimeMessage message = new MimeMessage(session);
	            // Set From: header field of the header.
	            message.setFrom(new InternetAddress(from));
	            // Set To: header field of the header.
	            for (String recipient : to) {
	                message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
	            }
	            for (String ccRecipient : cc) {
	                message.addRecipient(Message.RecipientType.CC, new InternetAddress(ccRecipient));
	            }
	            for (String bccRecipient : bcc) {
	                message.addRecipient(Message.RecipientType.BCC, new InternetAddress(bccRecipient));
	            }
	            // Set Subject: header field
	            message.setSubject("pp4.humanbrain.in - STORAGE ALERT ⚠️ ");
	            // Set the actual message
	            message.setContent("This email has been automatically generated:<br>" + messageBody + 
	            	    "Attention and Action Required <br>" + messageBody +
	            	    "<br>pp4 <b>nvmeShare</b> storage utilization has crossed <b style='color:red;'>70%</b> :<br>" + messageBody + 
	            	    "<br>Please clear unnecessary files to free up space and avoid storage-related issues.<br>" + messageBody, "text/html");

	            System.out.println("sending...");
	            // Send message
	            Transport.send(message);
	            System.out.println("Sent message successfully....");
	        } catch (MessagingException mex) {
	            mex.printStackTrace();
	        }
     }}
