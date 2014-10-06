package ws;

import adc.ADCContext;
import adc.ADCListener;
import adc.ADCObserver;

import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import relay.RelayManager;

public class WebSocketFeeder
{
  private final static boolean DEBUG = "true".equals(System.getProperty("debug", "false"));
  private ADCObserver.MCP3008_input_channels channel = null;
  private boolean keepWorking = true;
  private WebSocketClient webSocketClient = null;
  private static RelayManager rm;
  
  public WebSocketFeeder(int ch) throws Exception
  {
    channel = findChannel(ch);
    try 
    { 
      rm = new RelayManager(); 
      rm.set("00", "on");
    }
    catch (Exception ex)
    {
      System.err.println("You're not on the PI, hey?");
      ex.printStackTrace();
    }
    
    String wsUri = System.getProperty("ws.uri", "ws://localhost:9876/"); 
    
    initWebSocketConnection(wsUri);
    final ADCObserver obs = new ADCObserver(channel); // Note: We could instantiate more than one observer (on several channels).
    ADCContext.getInstance().addListener(new ADCListener()
       {
         @Override
         public void valueUpdated(ADCObserver.MCP3008_input_channels inputChannel, int newValue) 
         {
           if (inputChannel.equals(channel))
           {
             int volume = (int)(newValue / 10.23); // [0, 1023] ~ [0x0000, 0x03FF] ~ [0&0, 0&1111111111]
             if (DEBUG)
               System.out.println("readAdc:" + Integer.toString(newValue) + 
                                               " (0x" + lpad(Integer.toString(newValue, 16).toUpperCase(), "0", 2) + 
                                               ", 0&" + lpad(Integer.toString(newValue, 2), "0", 8) + ")"); 
             System.out.println("Volume:" + volume + "% (" + newValue + ")");
             try { webSocketClient.send(Integer.toString(volume)); }
             catch (Exception ex) 
             {
               ex.printStackTrace();
             }
             // Turn relay off above 75%
             if (volume > 75)
             {
               String status = rm.getStatus("00");
//             System.out.println("Relay is:" + status);
               if ("on".equals(status))
               {
                 System.out.println("Turning relay off!");
                 try { rm.set("00", "off"); }
                 catch (Exception ex)
                 {
                   System.err.println(ex.toString());
                 }
               }
             }
           }
         }
       });
    obs.start();         
    
    Runtime.getRuntime().addShutdownHook(new Thread()
       {
         public void run()
         {
           System.out.println("Shutting down nicely.");
           if (obs != null)
             obs.stop();
           keepWorking = false;
           webSocketClient.close();
           try 
           { 
             rm.set("00", "on"); 
             rm.shutdown();
           }
           catch (Exception ex)
           {
             System.err.println(ex.toString());
           }         
         }
       });    
  }
  
  private void initWebSocketConnection(String serverURI)
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
//        System.out.println("WS On Message");
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
  
  public static void main(String[] args) throws Exception
  {
    int channel = 0;
    if (args.length > 0)
      channel = Integer.parseInt(args[0]);
    new WebSocketFeeder(channel);
  }

  private static ADCObserver.MCP3008_input_channels findChannel(int ch) throws IllegalArgumentException
  {
    ADCObserver.MCP3008_input_channels channel = null;
    switch (ch)
    {
      case 0:
        channel = ADCObserver.MCP3008_input_channels.CH0;
        break;
      case 1:
        channel = ADCObserver.MCP3008_input_channels.CH1;
        break;
      case 2:
        channel = ADCObserver.MCP3008_input_channels.CH2;
        break;
      case 3:
        channel = ADCObserver.MCP3008_input_channels.CH3;
        break;
      case 4:
        channel = ADCObserver.MCP3008_input_channels.CH4;
        break;
      case 5:
        channel = ADCObserver.MCP3008_input_channels.CH5;
        break;
      case 6:
        channel = ADCObserver.MCP3008_input_channels.CH6;
        break;
      case 7:
        channel = ADCObserver.MCP3008_input_channels.CH7;
        break;
      default:
        throw new IllegalArgumentException("No channel " + Integer.toString(ch));
    }
    return channel;
  }
  
  private static String lpad(String str, String with, int len)
  {
    String s = str;
    while (s.length() < len)
      s = with + s;
    return s;
  }
}

