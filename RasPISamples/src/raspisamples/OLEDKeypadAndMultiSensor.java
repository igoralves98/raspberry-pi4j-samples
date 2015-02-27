package raspisamples;

import adafruiti2c.sensor.AdafruitHMC5883L;
import adafruiti2c.sensor.AdafruitMPL115A2;

import adafruitspi.oled.AdafruitSSD1306;
import adafruitspi.oled.ScreenBuffer;
import com.pi4j.io.gpio.RaspiPin;

import java.io.IOException;

import java.text.DecimalFormat;

import phonekeyboard3x4.KeyboardController;

/*
 * A phone keypad, and a 128x32 oled screen
 * 
 * Plus HMC5883L & MPL115A2
 * (triple-axis compass, and temp + pressure)
 */
public class OLEDKeypadAndMultiSensor
{
  private KeyboardController kbc;
  private AdafruitSSD1306 oled;
  private ScreenBuffer sb;
  
  private AdafruitMPL115A2 ptSensor;
  private AdafruitHMC5883L magnetometer;
  
  private final DecimalFormat HDG_FMT  = new DecimalFormat("000");
  private final DecimalFormat PR_FMT   = new DecimalFormat("#000.0");
  private final DecimalFormat TEMP_FMT = new DecimalFormat("##00.0");
  
  private boolean keepReading = true;
  
  // This one overrides the default pins for the OLED
  public OLEDKeypadAndMultiSensor()
  {
    kbc = new KeyboardController();
    //                                              Override the default pins
    oled = new AdafruitSSD1306(RaspiPin.GPIO_12, // Clock
                               RaspiPin.GPIO_13, // MOSI (data) 
                               RaspiPin.GPIO_14, // CS
                               RaspiPin.GPIO_15, // RST
                               RaspiPin.GPIO_16);// DC
    oled.begin();

    sb = new ScreenBuffer(128, 32);
//  sb.clear(ScreenBuffer.Mode.BLACK_ON_WHITE);
    clear();
    
    ptSensor = new AdafruitMPL115A2();
    try
    {
      ptSensor.begin();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      System.exit(1);
    }
    magnetometer = new AdafruitHMC5883L();
    
    // First readings
    try
    {
      double hdg = magnetometer.readHeading();
      displayHdg(hdg);
      float[] data = ptSensor.measure();
      displayPT(data[AdafruitMPL115A2.PRESSURE_IDX], 
                data[AdafruitMPL115A2.TEMPERATURE_IDX]);
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();
    }
    // Wait...
    try { Thread.sleep(2000L); } catch (Exception ex) {}
  
    Thread ptThread = new Thread()
      {
        public void run()
        {
          while (keepReading)
          {
            try
            {
              float[] data = ptSensor.measure();
              displayPT(data[AdafruitMPL115A2.PRESSURE_IDX], data[AdafruitMPL115A2.TEMPERATURE_IDX]);
              try { Thread.sleep(500L); } catch (Exception ex) {}
            }
            catch (IOException ioe)
            {
              ioe.printStackTrace();
            }
          }
          System.out.println("ptThread completed");
          ptSensor.close();
        }
      };
    Thread hdgThread = new Thread()
      {
        public void run()
        {
          while (keepReading)
          {
            try
            {
              double hdg = magnetometer.readHeading();
              displayHdg(hdg);
              try { Thread.sleep(500L); } catch (Exception ex) {}
            }
            catch (IOException ioe)
            {
              ioe.printStackTrace();
            }
          }
          System.out.println("hdgThread completed");
          magnetometer.close(); 
        }
      };
    ptThread.start();
    hdgThread.start();
    
    reset();
  }
  
  // User input
  public synchronized void display(String txt) 
  {
    synchronized (sb)
    {
      sb.text(txt, 2, 8);
      oled.setBuffer(sb.getScreenBuffer());
      oled.display();
    }
  }
  
  public synchronized void displayHdg(double hdg)
  {
//  System.out.println("HDG:" + Math.toDegrees(hdg) + " deg");
    synchronized (sb)
    {
      String txt = "HDG:" + HDG_FMT.format(Math.toDegrees(hdg));
      sb.text(txt, 2, 18);
      oled.setBuffer(sb.getScreenBuffer());
      oled.display();
    }
  }
  
  public synchronized void displayPT(double press, double temp)
  {
//  System.out.println("P:" + press + ", T:" + temp);
    synchronized (sb)
    {
      String txt = "Baro:" + PR_FMT.format(press * 10) + " hPa, T:" + TEMP_FMT.format(temp) + " C";
      sb.text(txt, 2, 28);
      oled.setBuffer(sb.getScreenBuffer());
      oled.display();
    }
  }

  @SuppressWarnings("oracle.jdeveloper.java.insufficient-catch-block")
  public void userInput()
  {
    StringBuffer charBuff = new StringBuffer();
    boolean go = true;
    while (go)
    {
      char c = kbc.getKey();    
//    System.out.println("At " + System.currentTimeMillis() + ", Char: " + c);
      if (c == '#')
        go = false;
      else if (c == '*')
      {
        charBuff = new StringBuffer();
        reset();
      }
      else
        charBuff.append(c);
      display(charBuff.toString());
      try { Thread.sleep(200L); } catch (Exception ex) {}
    }
    reset();
    display("Bye-bye");
    System.out.println("Bye");
    kbc.shutdown();
    keepReading = false;
    try { Thread.sleep(1000L); } catch (Exception ex) {}
    clear();
    oled.shutdown();
  }

  public void reset()
  {
    synchronized (sb)
    {
      synchronized (oled)
      {
        sb.clear();
        oled.clear();
        sb.text("# = Exit.", 2, 8);
        oled.setBuffer(sb.getScreenBuffer());
        oled.display();   
    //  clear();   
      }
    }
  }
  
  public void clear()
  {
    synchronized (sb)
    {
      sb.clear();
      oled.clear();
      oled.setBuffer(sb.getScreenBuffer());
      oled.display();       
    }
  }
  
  public static void main(String[] args)
  {
    System.out.println("Hit # to exit");
    OLEDKeypadAndMultiSensor ui = new OLEDKeypadAndMultiSensor();
    ui.userInput();
  }
}