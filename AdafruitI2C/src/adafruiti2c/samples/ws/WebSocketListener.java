package adafruiti2c.samples.ws;

import adafruiti2c.AdafruitPCA9685;

import java.io.FileOutputStream;

import oracle.generic.ws.client.ClientFacade;
import oracle.generic.ws.client.ServerListenerAdapter;
import oracle.generic.ws.client.ServerListenerInterface;

import org.json.JSONObject;

public class WebSocketListener
{
  private final static boolean DEBUG = false;

  private boolean keepWorking = true;
  private ClientFacade webSocketClient = null;
  
  AdafruitPCA9685 servoBoard = null;
  private final int freq = 60;
  // For the TowerPro SG-5010
  private final static int servoMin = 150;   // -90 deg
  private final static int servoMax = 600;   // +90 deg

  private final static int STANDARD_SERVO_CHANNEL   = 15;
  
  private int servo = STANDARD_SERVO_CHANNEL;
  
  public WebSocketListener() throws Exception
  {
    try
    {
      servoBoard = new AdafruitPCA9685();
      servoBoard.setPWMFreq(freq); // Set frequency in Hz
    }
    catch (UnsatisfiedLinkError ule)
    {
      System.err.println("You're not on the PI, are you?");  
    }
    
    String wsUri = System.getProperty("ws.uri", "ws://localhost:9876/"); 
    
    initWebSocketConnection(wsUri);
  }
  
  private void initWebSocketConnection(String serverURI)
  {
    String[] targetedTransports = new String[] {"WebSocket", 
                                                "XMLHttpRequest"};
    ServerListenerInterface serverListener = new ServerListenerAdapter()
    {
      @Override
      public void onMessage(String mess)
      {
    //  System.out.println("    . Text message :[" + mess + "]");
        JSONObject json = new JSONObject(mess);
        String valueContent = ((JSONObject)json.get("data")).get("text").toString().replace("&quot;", "\"");
        JSONObject valueObj = new JSONObject(valueContent);
    //  System.out.println("    . Mess content:[" + ((JSONObject)json.get("data")).get("text") + "]");
        int servoValue = valueObj.getInt("value");
        System.out.println("Servo Value:" + servoValue);
        // TODO Drive the servo here
        if (servoBoard != null)
        {
          System.out.println("Setting the servo to " + servoValue);
          if (servoValue < -90 || servoValue > 90)
            System.err.println("Between -90 and 90 only");
          else
          {
            int on = 0;
            int off = (int)(servoMin + (((double)(servoValue + 90) / 180d) * (servoMax - servoMin)));
            System.out.println("setPWM(" + servo + ", " + on + ", " + off + ");");
            servoBoard.setPWM(servo, on, off);
            System.out.println("-------------------");
          }
        }
      }

      @Override
      public void onMessage(byte[] bb)
      {
        System.out.println("    . Message for you (ByteBuffer) ...");
        System.out.println("Length:" + bb.length);
        try
        {
          FileOutputStream fos = new FileOutputStream("binary.xxx");
          for (int i=0; i<bb.length; i++)
            fos.write(bb[i]);
          fos.close();
          System.out.println("... was written in binary.xxx");
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
        }
      }

      @Override
      public void onConnect()
      {
        System.out.println("    .You're in!");
        keepWorking = true;
      }

      @Override
      public void onClose()
      {
        System.out.println("    .Connection has been closed...");
        keepWorking = false;
      }

      @Override
      public void onError(String error)
      {
        System.out.println("    .Oops! error [" + error + "]");
        keepWorking = false; // Careful with that one..., in case of a fallback, use the value returned by the init method.
      }

      @Override
      public void setStatus(String status)
      {
        System.out.println("    .Your status is now [" + status + "]");
      }
      
      @Override
      public void onPong(String s)
      {
        if (DEBUG)
          System.out.println("WS Pong");
      }
      
      @Override
      public void onPing(String s)
      {
        if (DEBUG)
          System.out.println("WS Ping");
      }
      
      @Override
      public void onHandShakeSentAsClient()
      {
        System.out.println("WS-HS sent as client");
      }
      
      @Override
      public void onHandShakeReceivedAsServer()
      {
        if (DEBUG)
          System.out.println("WS-HS received as server");
      }
      
      @Override
      public void onHandShakeReceivedAsClient()
      {
        if (DEBUG)
          System.out.println("WS-HS received as client");
      }      
    };
    
    try
    {
      webSocketClient = new ClientFacade(serverURI, 
                                targetedTransports, 
                                serverListener);
      keepWorking = webSocketClient.init();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }    
  }
  
  public static void main(String[] args) throws Exception
  {
    System.out.println("System variable ws.uri can be used if the URL is not ws://localhost:9876/");
    new WebSocketListener();
  }
}

