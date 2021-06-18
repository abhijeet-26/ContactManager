package com.proContact.Service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

	
	public boolean sendEmail(String subject,String message,String to)
	{
				String from="abhibas14@gmail.com";
				boolean f=false;
				
				//Getting gmail host api
				
				String host="smtp.gmail.com";
				final String username = "abhibas14@gmail.com";
		        final String password = "Abhibas1234@";
				
				
				// Get the system Properties
				Properties properties = System.getProperties();
				System.out.println("Properties: "+properties);
				
				//setting important properties
				
				//host set
				properties.put("mail.smtp.host", host);
				properties.put("mail.smtp.port", "465");
				properties.put("mail.smtp.ssl.enable", "true"); //SSL-->Secure Sockets Layer
				properties.put("mail.smtp.auth", "true"); 
				
				//Step-1: to get the session object...
				Session session=Session.getInstance(properties, new Authenticator() {

					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						
						return new PasswordAuthentication(username,password);
					}

				});
				
				session.setDebug(true);
				
				// Step 2: Compose The messages[text, multi-media]...
				
				MimeMessage mimeMessage = new MimeMessage(session);
				try {
					
					//set From
					mimeMessage.setFrom(from);
					
					//add Recipients
					mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
					
					// adding subject to message
					mimeMessage.setSubject(subject);
					
					//adding text to message
					//mimeMessage.setText(message);
					
					mimeMessage.setContent(message, "text/html");
					
					//STEP:3 SEND THE MESSAGE
					Transport.send(mimeMessage);
					System.out.println("Message Send successfully");
					
					f=true;
					
		
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				return f;
	}
}
