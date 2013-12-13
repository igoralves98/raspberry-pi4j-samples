package pi4j.stby;

import com.sun.mail.smtp.SMTPTransport;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import java.io.InputStreamReader;

import java.io.StringReader;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Part;
import javax.mail.Transport;
import javax.mail.PasswordAuthentication;
import javax.mail.Flags;
import javax.mail.Store;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Authenticator;
import javax.mail.Session;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.AddressException;
import javax.mail.search.AndTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

import org.json.JSONObject;

public class EmailPollingClient
{
  private String protocol;
  private int port;
  private int incomingPort;
  private String username;
  private String password;
  private String outgoing;
  private String incoming;
  private String replyto;
  private boolean smtpauth;
  private String dest;
  private String subject;
  private String content;  
  
  private static boolean verbose = "true".equals(System.getProperty("verbose", "false"));

  private static class SMTPAuthenticator extends Authenticator
  {
    private String SMTP_AUTH_USER = "";
    private String SMTP_AUTH_PWD  = "";

    public SMTPAuthenticator(String u, String p)
    {
      SMTP_AUTH_USER = u;
      SMTP_AUTH_PWD = p;
    }

    public PasswordAuthentication getPasswordAuthentication()
    {
      String username = SMTP_AUTH_USER;
      String password = SMTP_AUTH_PWD;
      return new PasswordAuthentication(username, password);
    }
  }

  public EmailPollingClient(String provider) throws RuntimeException
  {
    protocol = "";
    port = 0;
    incomingPort = 0;
    username = "";
    password = "";
    outgoing = "";
    incoming = "";
    replyto = "";
    smtpauth = false;
    dest = "";
    subject = "";
    content = "";
    Properties props = new Properties();
    String propFile = "email.properties";
    try
    {
      FileInputStream fis = new FileInputStream(propFile);
      props.load(fis);
    }
    catch (Exception e)
    {
      System.out.println("email.properies file problem...");
      throw new RuntimeException("File not found:email.properies");
    }
    protocol     = props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "mail.protocol");
    port         = Integer.parseInt(props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "outgoing.server.port", "0"));
    incomingPort = Integer.parseInt(props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "incoming.server.port", "0"));
    username     = props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "mail.username",   "");
    password     = props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "mail.password",   "");
    outgoing     = props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "outgoing.server", "");
    incoming     = props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "incoming.server", "");
    replyto      = props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "mail.replyto",    "");
    smtpauth     = "true".equals(props.getProperty("pi." + "mail.smtpauth", "false"));
    
    if (verbose)
    {
      System.out.println(protocol);
      System.out.println(username);
    }
    dest = "olivier@lediouris.net";
    subject = "PI Request";
    content = "{ content: 'whatever' }";
  }

  public boolean isAuthRequired()
  {
    return smtpauth;
  }

  public String getUserName()
  {
    return username;
  }

  public String getPassword()
  {
    return password;
  }

  public String getReplyTo()
  {
    return replyto;
  }

  public String getIncomingServer()
  {
    return incoming;
  }

  public String getOutgoingServer()
  {
    return outgoing;
  }

  public void send(String user, String pw, String[] dest, String subject, String content)
    throws MessagingException, AddressException
  {
    Properties props = new Properties();
    props.put("mail.smtp.host", outgoing);
    props.put("mail.smtp.port", Integer.toString(port));
    props.put("mail.debug", verbose?"true":"false");
    Authenticator auth = null;
    if (user != null && pw != null)
    {
      if (verbose) System.out.println("Setting SMTP authentication");
      props.put("mail.smtp.auth", "true");
      auth = new SMTPAuthenticator(user, pw);
    } 
    else
    {
      if (verbose) System.out.println("SMTP Authentication NOT set.");
    }

    props.put("mail.smtp.starttls.enable", "true"); //  See http://www.oracle.com/technetwork/java/faq-135477.html#yahoomail

    Session session = Session.getDefaultInstance(props, auth);
    Transport tr = session.getTransport("smtp");
    if (tr instanceof SMTPTransport)
    {
      SMTPTransport smtptr = (SMTPTransport)tr;
      // Set the STARTTLS if necessary
    }
    else
      System.out.println("This is NOT an SMTPTransport:[" + tr.getClass().getName() + "]");

    Message msg = new MimeMessage(session);
    msg.setFrom(new InternetAddress(replyto));
    msg.setRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(dest[0]));
    for (int i=1; i<dest.length; i++)
      msg.addRecipient(javax.mail.Message.RecipientType.CC, new InternetAddress(dest[i]));
    
    msg.setSubject(subject);
    msg.setText(content != null ? content : "");
    if (verbose) System.out.println("sending:[" + content + "], " + Integer.toString(content.length()) + " characters");
    Transport.send(msg);
  }

  public List<String> receive()
    throws Exception
  {
    return receive(null);
  }
  
  public List<String> receive(String dir)
    throws Exception
  {
    List<String> messList = new ArrayList<String>();
    Store store = null;
    Folder folder = null;
    try
    {
      Properties props = System.getProperties();
      Session session = Session.getInstance(props, null);
      store = session.getStore(protocol);
      if (incomingPort == 0)
        store.connect(incoming, username, password);
      else
        store.connect(incoming, incomingPort, username, password);
      if (verbose) System.out.println("Connected to store");        
      folder = store.getDefaultFolder();
      if (folder == null)
        throw new RuntimeException("No default folder");

      folder = store.getFolder("INBOX");
      if (folder == null)
        throw new RuntimeException("No INBOX");

      folder.open(Folder.READ_WRITE);
      if (verbose) System.out.println("Connected... filtering, please wait.");
      SearchTerm st = new AndTerm(new SearchTerm[] { //new OrTerm(new FromStringTerm("olivier@lediouris.net"), 
                                                     //           new FromStringTerm("olivier.lediouris@yahoo.com")), 
                                                     new FromStringTerm("olivier.lediouris@oracle.com"),
                                                     new SubjectTerm("PI Request"),
                                                     new FlagTerm(new Flags(Flags.Flag.SEEN), false) });
   // st = new SubjectTerm("PI Request");
      Message msgs[] = folder.search(st);

//    Message msgs[] = folder.getMessages();

      if (verbose) System.out.println("Search completed, " + msgs.length + " message(s).");
      for (int msgNum=0; msgNum<msgs.length; msgNum++)
      {
        try
        {
          Message mess = msgs[msgNum];
          Address from[] = mess.getFrom();
          String subject = mess.getSubject();
          String sender = "";
          try
          {
            sender = from[0].toString();
          }
          catch(Exception exception) 
          {
            exception.printStackTrace();
          }
//        System.out.println("Message from [" + sender + "], subject [" + subject + "], content [" + mess.getContent().toString().trim() + "]");
          
          if (true)
          {
            if (!mess.isSet(javax.mail.Flags.Flag.SEEN) && 
                !mess.isSet(javax.mail.Flags.Flag.DELETED))
            {
              messList.add(printMessage(mess, dir));
              mess.setFlag(javax.mail.Flags.Flag.SEEN, true);
              mess.setFlag(javax.mail.Flags.Flag.DELETED, true);
              // TODO Send an ack - by email
            } 
            else
            {
              if (verbose) System.out.println("Old message in your inbox..., received " + mess.getReceivedDate().toString());
            }
          }
        }
        catch(Exception ex)
        {
          System.err.println(ex.getMessage());
        }
      }
    }
    catch(Exception ex)
    {
      throw ex;
    }
    finally
    {
      try
      {
        if(folder != null)
          folder.close(true);
        if(store != null)
          store.close();
      }
      catch(Exception ex2)
      {
        System.err.println("Finally ...");
        ex2.printStackTrace();
      }
    }
    return messList;
  }

  public static String printMessage(Message message, String dir)
  {
    String ret = "";
    try
    {
      String from = ((InternetAddress)message.getFrom()[0]).getPersonal();
      if(from == null)
        from = ((InternetAddress)message.getFrom()[0]).getAddress();
      if (verbose) System.out.println("From: " + from);
      String subject = message.getSubject();
      if (verbose) System.out.println("Subject: " + subject);
      Part messagePart = message;
      Object content = messagePart.getContent();
      if (content instanceof Multipart)
      {
//      messagePart = ((Multipart)content).getBodyPart(0);
        int nbParts = ((Multipart)content).getCount();
        if (verbose) System.out.println("[ Multipart Message ], " + nbParts + " part(s).");
        for (int i=0; i<nbParts; i++)
        {
          messagePart = ((Multipart)content).getBodyPart(i);
          if (messagePart.getContentType().toUpperCase().startsWith("APPLICATION/OCTET-STREAM"))
          {
            if (verbose) System.out.println(messagePart.getContentType() + ":" + messagePart.getFileName());
            InputStream is = messagePart.getInputStream();
            String newFileName = "";
            if (dir != null)
              newFileName = dir + File.separator;
            newFileName += messagePart.getFileName();
            FileOutputStream fos = new FileOutputStream(newFileName);
            ret = messagePart.getFileName();
            if (verbose) System.out.println("Downloading " + messagePart.getFileName() + "...");
            copy(is, fos);
            if (verbose) System.out.println("...done.");
          } 
          else // text/plain, text/html
          {
            if (verbose) System.out.println("-- Part #" + i + " --, " + messagePart.getContentType().replace('\n', ' ').replace('\r', ' ').replace("\b", "").trim());
            InputStream is = messagePart.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = "";
            while (line != null)
            {
              line = br.readLine();
              if (line != null)
              {
                if (verbose) System.out.println("[" + line + "]");
                if (messagePart.getContentType().toUpperCase().startsWith("TEXT/PLAIN"))
                  ret += line;
              }
            }
            br.close();
            if (verbose) System.out.println("-------------------");
          }
        }
      }
      else
      {
//      System.out.println("  .Message is a " + content.getClass().getName());
//      System.out.println("Content:");
//      System.out.println(content.toString());
        ret = content.toString();
      }
      if (verbose) System.out.println("-----------------------------");
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
    return ret;
  }

  public static void copy(InputStream in, OutputStream out)
    throws IOException
  {
    synchronized(in)
    {
      synchronized(out)
      {
        byte buffer[] = new byte[256];
        while (true)
        {
          int bytesRead = in.read(buffer);
          if(bytesRead == -1)
            break;
          out.write(buffer, 0, bytesRead);
        }
      }
    }
  }

  private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
  
  public static void main(String[] args)
  {
//  String provider = "yahoo";
    String provider = "google";
    EmailPollingClient emc = new EmailPollingClient(provider);
    try
    {
      if (true)
      {
//      emc.send("olivier@lediouris.net", "c2h5oh", "olivier@lediouris.net", "PI Request", "{ operation: 'whatever' }");
        emc.send("olivier.lediouris", 
                 "c2h5oh", 
                 new String[] { "olivier.lediouris@gmail.com", 
                                "olivier@lediouris.net", 
                                "webmaster@lediouris.net" }, 
                 "PI Request", 
                 "{ operation: 'whatever' }");
        System.out.println("Sent.");
        return;
      }
      boolean keepLooping = true;
      while (keepLooping)
      {
        List<String> received = emc.receive();
        System.out.println(SDF.format(new Date())  + " - Retrieved " + received.size() + " message(s).");
        for (String s : received)
        {
//        System.out.println(s);
          String operation = "";
          try
          {
            JSONObject json = new JSONObject(s);
            operation = json.getString("operation");
          }
          catch (Exception ex)
          {
            System.err.println(ex.getMessage());
            // Try XML? Properties file syntax?
            try
            {
              Properties props = new Properties();
              props.load(new StringReader(s));
              operation = props.getProperty("operation");
            }
            catch (Exception ex2)
            {
              ex2.printStackTrace();
            }
          }
          if ("exit".equals(operation))
          {
            keepLooping = false;
            System.out.println("Will exit next batch.");
        //  break;
          }
          else
          {
            System.out.println("Operation: [" + operation + "], sent for processing.");
            try { Thread.sleep(1000L); } catch (InterruptedException ie) {}
          }
        }
      }
      System.out.println("Done.");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
