package raspisamples;

import java.io.InputStream;

import java.net.URI;

import org.java_websocket.client.WebSocketClient;

import org.java_websocket.handshake.ServerHandshake;

import org.json.JSONObject;

import raspisamples.servo.StandardServo;

/*
 * Driven by WerbSocket server
 * See in node/server.js
 * 
 * 2 Servos (UP/LR)
 */
public class PanTiltWebSocket
{
  private static StandardServo ssUD = null, 
                               ssLR = null;

  private static WebSocketClient webSocketClient = null;  
  
  public static void main(String[] args) throws Exception
  {
    ssUD = new StandardServo(14); // 14 : Address on the board (1..15)
    ssLR = new StandardServo(15); // 15 : Address on the board (1..15)
    
    // Init/Reset
    ssUD.stop();
    ssLR.stop();
    ssUD.setAngle(0f);
    ssLR.setAngle(0f);
    
    Runtime.getRuntime().addShutdownHook(new Thread()
    {
      public void run()
      {
        close();
      }
    });
    
    StandardServo.waitfor(2000);

    String wsUri = System.getProperty("ws.uri", "ws://localhost:9876/");     
    initWebSocketConnection(wsUri);

  }

  private static void initWebSocketConnection(String serverURI)
  {
    try
    {
      webSocketClient = new WebSocketClient(new URI(serverURI))
      {
        @Override
        public void onOpen(ServerHandshake serverHandshake)
        {
          System.out.println("WS On Open");
        }

        @Override
        public void onMessage(String string)
        {
    //    System.out.println("WS On Message:" + string);
          JSONObject message = new JSONObject(string);
          JSONObject leapmotion = new JSONObject(message.getJSONObject("data").getString("text"));
          int roll  = leapmotion.getInt("roll");    
          int pitch = leapmotion.getInt("pitch");    
          int yaw   = leapmotion.getInt("yaw");    
          System.out.println("Roll:" + roll + ", pitch:" + pitch + ", yaw:" + yaw);
          ssLR.setAngle(yaw);
          ssUD.setAngle(-roll); // Actually pitch...
        }

        @Override
        public void onClose(int i, String string, boolean b)
        {
          System.out.println("WS On Close");
        }

        @Override
        public void onError(Exception exception)
        {
          System.out.println("WS On Error");
          exception.printStackTrace();
        }
      }; 
      webSocketClient.connect();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }    
  }

  public static void close()
  {
    System.out.println("\nExiting...");
    webSocketClient.close();
    // Reset to 0,0 before shutting down.
    ssUD.setAngle(0f);
    ssLR.setAngle(0f);
    StandardServo.waitfor(2000);
    ssUD.stop();
    ssLR.stop();
    System.out.println("Bye");
  }
  
}
