package raspisamples.log.net;

import adafruiti2c.sensor.AdafruitBMP180;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.json.JSONObject;

import raspisamples.util.HTTPClient;

public class BMP180Logging
{
  private final static String LOGGER_URL = "http://donpedro.lediouris.net/php/raspi/insert.php"; // ?board=OlivRPi1&sensor=BMP180&type=TEMPERATURE&data=24
  private final static String SENSOR_ID   = "BMP180";
  private final static String TEMPERATURE = "TEMPERATURE";
  private final static String PRESSURE    = "PRESSURE";

  private static String boardID = "OlivRPi1";
  private static long waitTime  = 10000L;

  private final static String BOARD_PRM = "-board";
  private final static String WAIT_PRM  = "-wait";
  private final static String HELP_PRM  = "-help";
  
  protected static void waitfor(long howMuch)
  {
    try { Thread.sleep(howMuch); } catch (InterruptedException ie) { ie.printStackTrace(); }
  }
  
  private static void processPrm(String[] args)
  {
    for (int i=0; i<args.length; i++)
    {
      if (BOARD_PRM.equals(args[i]))
        boardID = args[i + 1];
      else if (WAIT_PRM.equals(args[i]))
      {
        try
        {
          waitTime = 1000L * Integer.parseInt(args[i + 1]);
        }
        catch (Exception ex) 
        {
          ex.printStackTrace();
        }
      }
      else if (HELP_PRM.equals(args[i]))
      {
        System.out.println("Usage is:");
        System.out.println("  java raspisamples.log.net.BMP180Logging -board <BoardID> -wait <time-in-sec> -help ");
        System.out.println("  <BoardID> is your board ID (default is OlivRPi1)");
        System.out.println("  <time-in-sec> is the amount of seconds between loops (default is 10)");
        System.exit(0);
      }
    }
  }
  
  public static void main(String[] args)
  {
    processPrm(args);

    System.out.println("Logging data for [" + boardID + "]");
    
    final NumberFormat NF = new DecimalFormat("##00.00");
    AdafruitBMP180 sensor = new AdafruitBMP180();
    float press = 0;
    float temp  = 0;
    double alt  = 0;

    Runtime.getRuntime().addShutdownHook(new Thread()
                                         {
                                           public void run()
                                           {
                                             System.out.println("\nBye now.");
                                           }
                                         });

    while (true)
    {
      try { press = sensor.readPressure(); } 
      catch (Exception ex) 
      { 
        System.err.println(ex.getMessage()); 
        ex.printStackTrace();
      }
      sensor.setStandardSeaLevelPressure((int)press); // As we ARE at the sea level (in San Francisco).
      try { alt = sensor.readAltitude(); } 
      catch (Exception ex) 
      { 
        System.err.println(ex.getMessage()); 
        ex.printStackTrace();
      }
      try { temp = sensor.readTemperature(); } 
      catch (Exception ex) 
      { 
        System.err.println(ex.getMessage()); 
        ex.printStackTrace();
      }
      
      System.out.println("Temperature: " + NF.format(temp) + " C");
      System.out.println("Pressure   : " + NF.format(press / 100) + " hPa");
      System.out.println("Altitude   : " + NF.format(alt) + " m");
      
      // Log here
      try
      {
        String url = LOGGER_URL + "?board=" + boardID + "&sensor=" + SENSOR_ID + "&type=" + TEMPERATURE + "&data=" + NF.format(temp);
        String response = HTTPClient.getContent(url);
        JSONObject json = new JSONObject(response);
        System.out.println("Returned\n" + json.toString(2));
        try { Thread.sleep(1000); } catch (Exception ex) { ex.printStackTrace(); } // To avoid duplicate PK
        url = LOGGER_URL + "?board=" + boardID + "&sensor=" + SENSOR_ID + "&type=" + PRESSURE + "&data=" + NF.format(press / 100);
        response = HTTPClient.getContent(url);
        json = new JSONObject(response);
        System.out.println("Returned\n" + json.toString(2));
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
      waitfor(waitTime);
    }
  }
}
