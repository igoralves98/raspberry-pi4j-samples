package sunservo;

import adafruiti2c.AdafruitPCA9685;

import calculation.AstroComputer;
import calculation.SightReductionUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.text.DecimalFormat;

import java.util.Calendar;
import java.util.TimeZone;

import nmea.CustomNMEASerialReader;

import ocss.nmea.api.NMEAClient;
import ocss.nmea.api.NMEAEvent;
import ocss.nmea.api.NMEAListener;
import ocss.nmea.parser.GeoPos;
import ocss.nmea.parser.RMC;
import ocss.nmea.parser.StringParsers;

public class SunServoNMEAReader extends NMEAClient
{
  private final static DecimalFormat DFH = new DecimalFormat("#0.00'\272'");
  private final static DecimalFormat DFZ = new DecimalFormat("##0.00'\272'");
  
  private static GeoPos prevPosition = null;
  private static long   prevDateTime = -1L;
  private static int prevZ = -1;
  private static boolean parked = false;
  
  private static boolean calibrated = false;
  private static AdafruitPCA9685 servoBoard = null;
  private static int servoMin = 150;   // Min pulse length out of 4096
  private static int servoMax = 600;   // Max pulse length out of 4096
  
  private final static int CONTINUOUS_SERVO_CHANNEL = 14;
  private final static int STANDARD_SERVO_CHANNEL   = 15;

  private static final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

  public SunServoNMEAReader()
  {
    super();
  }
  
  public static String userInput(String prompt)
  {
    String retString = "";
    System.err.print(prompt);
    try
    {
      retString = stdin.readLine();
    }
    catch(Exception e)
    {
      System.out.println(e);
      String s;
      try
      {
        s = userInput("<Oooch/>");
      }
      catch(Exception exception) 
      {
        exception.printStackTrace();
      }
    }
    return retString;
  }

  @Override
  public void dataDetectedEvent(NMEAEvent e)
  {
//  System.out.println("Received:" + e.getContent());
    manageData(e.getContent().trim());
  }

  private static SunServoNMEAReader customClient = null;  
  
  private static void manageData(String sentence)
  {
    boolean valid = StringParsers.validCheckSum(sentence);
    if (valid)
    {
      String id = sentence.substring(3, 6);
      if ("RMC".equals(id))
      {
     // System.out.println(line);
        RMC rmc = StringParsers.parseRMC(sentence);
     // System.out.println(rmc.toString());
        if (rmc != null && rmc.getRmcDate() != null && rmc.getGp() != null)
        {
          if ((prevDateTime == -1L || prevPosition == null) ||
              (prevDateTime != (rmc.getRmcDate().getTime() / 1000) || !rmc.getGp().equals(prevPosition)))
          {
            Calendar current = Calendar.getInstance(TimeZone.getTimeZone("etc/UTC"));
            current.setTime(rmc.getRmcDate());
            AstroComputer.setDateTime(current.get(Calendar.YEAR), 
                                      current.get(Calendar.MONTH) + 1, 
                                      current.get(Calendar.DAY_OF_MONTH), 
                                      current.get(Calendar.HOUR_OF_DAY), 
                                      current.get(Calendar.MINUTE), 
                                      current.get(Calendar.SECOND));
            AstroComputer.calculate();
            SightReductionUtil sru = new SightReductionUtil(AstroComputer.getSunGHA(),
                                                            AstroComputer.getSunDecl(),
                                                            rmc.getGp().lat,
                                                            rmc.getGp().lng);
            sru.calculate();
            Double he = sru.getHe();
            Double  z = sru.getZ();
            // Orient the servo here
            if (!calibrated)
            {
              calibrate();
              calibrated = true;
            }
            
            try
            {
              if (he > 0)
              {
                if (parked)
                  System.out.println("Resuming, sun is up.");
                parked = false;    
                int angle = 180 - (int)Math.round(z);
                if (angle < -90 || angle > 90)
                  System.err.println("Between -90 and 90 only");
                else
                {
                  if (prevZ != angle)
                  {
                    System.out.println("From [" + sentence + "]");
                    System.out.println(current.getTime().toString() + ", He:" + DFH.format(he)+ ", Z:" + DFZ.format(z) + " (" + rmc.getGp().toString() + ") -> " + angle);
                    int on = 0;
                    int off = (int)(servoMin + (((double)(angle + 90) / 180d) * (servoMax - servoMin)));
    //              System.out.println("setPWM(" + STANDARD_SERVO_CHANNEL + ", " + on + ", " + off + ");");
                    servoBoard.setPWM(STANDARD_SERVO_CHANNEL, on, off);
    //              System.out.println("-------------------");
                  }
                  prevZ = angle;
                }
              }
              else
              {
                // Parking
                if (!parked)
                  System.out.println("Parking, sun is down");
                parked = true;
                int on = 0;
                int angle = 0;
                int off = (int)(servoMin + (((double)(angle + 90) / 180d) * (servoMax - servoMin)));
//              System.out.println("setPWM(" + STANDARD_SERVO_CHANNEL + ", " + on + ", " + off + ");");
                servoBoard.setPWM(STANDARD_SERVO_CHANNEL, on, off);
              }
            }
            catch (Exception ex)
            {
              ex.printStackTrace();
            }
          }
          prevPosition = rmc.getGp();
          prevDateTime = (rmc.getRmcDate().getTime() / 1000);
        }
        else
        {
          if (rmc == null)
            System.out.println("... no RMC data in [" + sentence + "]");
          else
          {  
            String errMess = "";
            if (rmc.getRmcDate() == null)
              errMess += ("no Date ");
            if (rmc.getGp() == null)
              errMess += ("no Pos ");
            System.out.println(errMess + "in [" + sentence + "]");
          }
        }
      }
    }    
    else
      System.out.println("Invalid data [" + sentence + "]");
  }

  private static void calibrate()
  {
    try
    {
      int angle = 0;
      if (angle < -90 || angle > 90)
        System.err.println("Between -90 and 90 only");
      else
      {
        int on = 0;
        int off = (int)(servoMin + (((double)(angle + 90) / 180d) * (servoMax - servoMin)));
        System.out.println("setPWM(" + STANDARD_SERVO_CHANNEL + ", " + on + ", " + off + ");");
        servoBoard.setPWM(STANDARD_SERVO_CHANNEL, on, off);
        // TODO Vers le pole abaissé.
        String dummy = userInput("Orient the arrow SOUTH (true S, with no W), and hit return when ready.");        
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  
  }
  
  public static void main(String[] args)
  {
    System.setProperty("deltaT", System.getProperty("deltaT", "67.2810")); // 2014-Jan-01
    /*
     * Serial port possibly overriden by -Dserial.port
     * Default is /dev/ttyAMA0
     */    
    int br = 4800; 
    System.out.println("CustomNMEAReader invoked with " + args.length + " Parameter(s).");
    for (String s : args)
    {
      System.out.println("CustomNMEAReader prm:" + s);
      try { br = Integer.parseInt(s); } catch (NumberFormatException nfe) {}
    }
    
    customClient = new SunServoNMEAReader();
    
    servoBoard = new AdafruitPCA9685();
    servoBoard.setPWMFreq(60); // Set frequency to 60 Hz
    if (!calibrated) 
    {
      calibrate(); // Point the arrow South.
      calibrated = true;
    }    
      
    Runtime.getRuntime().addShutdownHook(new Thread() 
      {
        public void run() 
        {
          System.out.println ("Shutting down nicely.");
          customClient.stopDataRead();
        }
      });    
    customClient.initClient();
    customClient.setReader(new CustomNMEASerialReader(customClient.getListeners(), br));
    customClient.startWorking(); // Feignasse!
  }

  private void stopDataRead()
  {
    if (customClient != null)
    {
      for (NMEAListener l : customClient.getListeners())
        l.stopReading(new NMEAEvent(this));
    }
  }
}