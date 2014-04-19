package adc.sample;

import adc.ADCContext;
import adc.ADCListener;
import adc.ADCObserver;

import adc.utils.EscapeSeq;

import java.io.BufferedWriter;

import java.io.FileWriter;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.TimeZone;

import org.fusesource.jansi.AnsiConsole;

public class BatteryMonitor
{
  private static boolean debug = false;
  private ADCObserver.MCP3008_input_channels channel = null;
  private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
  static { SDF.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles")); }
  
  private static BufferedWriter bw = null;
  private static ADCObserver obs;
  
  public BatteryMonitor(int ch) throws Exception
  {
    channel = findChannel(ch);
    final int deltaADC = maxADC - minADC;
    final float deltaVolt = maxVolt - minVolt;
    obs = new ADCObserver(channel); // Note: We could instantiate more than one observer (on several channels).
    bw = new BufferedWriter(new FileWriter("battery.log"));
    ADCContext.getInstance().addListener(new ADCListener()
       {
         @Override
         public void valueUpdated(ADCObserver.MCP3008_input_channels inputChannel, int newValue) 
         {
           if (inputChannel.equals(channel))
           {
             int volume = (int)(newValue / 10.23); // [0, 1023] ~ [0x0000, 0x03FF] ~ [0&0, 0&1111111111]
             float voltage = (minVolt + (deltaVolt * (float)(newValue - minADC) / (float)deltaADC));
             if (debug)
             {
               System.out.print("readAdc:" + Integer.toString(newValue) + 
                                             " (0x" + lpad(Integer.toString(newValue, 16).toUpperCase(), "0", 2) + 
                                             ", 0&" + lpad(Integer.toString(newValue, 2), "0", 8) + ") "); 
               System.out.println("Volume:" + volume + "% (" + newValue + ") Volt:" + voltage);
             }
             // Log the voltage, along with the date
             String line = SDF.format(new Date(System.currentTimeMillis())) + ";" + newValue +  ";" + voltage;
//           System.out.println(line);
             try { bw.write(line + "\n"); } catch (Exception ex) { ex.printStackTrace(); }
           }
         }
       });
    obs.start();         
  }
  
  private final static String DEBUG_PRM       = "-debug=";
  private final static String CALIBRATION_PRM = "-calibration";
  private final static String CHANNEL_PRM     = "-ch=";
  private final static String MIN_VALUE       = "-min=";
  private final static String MAX_VALUE       = "-max=";
  
  private static int minADC =    0;
  private static int maxADC = 1023;
  private static float minVolt = 0f;
  private static float maxVolt = 15f;
  
  public static void main(String[] args) throws Exception
  {
    System.out.println("Parameters are:");
    System.out.println("  -calibration");
    System.out.println("  -debug=y|n|yes|no|true|false - example -debug=y       (default is n)");
    System.out.println("  -ch=[0-7]                    - example -ch=0          (default is 0)");
    System.out.println("  -min=minADC:minVolt          - example -min=280:3.75  (default is 0:0.0)");
    System.out.println("  -max=maxADC:maxVolt          - example -min=879:11.25 (default is 1023:15.0)");
    int channel = 0;
    for (String prm : args)
    {
      if (prm.startsWith(CHANNEL_PRM))
        channel = Integer.parseInt(prm.substring(CHANNEL_PRM.length()));
      else if (prm.startsWith(CALIBRATION_PRM))
        debug = true;
      else if (!debug && prm.startsWith(DEBUG_PRM))
        debug = ("y".equals(prm.substring(DEBUG_PRM.length())) || 
                 "yes".equals(prm.substring(DEBUG_PRM.length())) || 
                 "true".equals(prm.substring(DEBUG_PRM.length())));
      else if (prm.startsWith(MIN_VALUE))
      {
        String val = prm.substring(MIN_VALUE.length());
        minADC = Integer.parseInt(val.substring(0, val.indexOf(":"))); 
        minVolt = Float.parseFloat(val.substring(val.indexOf(":") + 1)); 
      }
      else if (prm.startsWith(MAX_VALUE))
      {
        String val = prm.substring(MAX_VALUE.length());
        maxADC = Integer.parseInt(val.substring(0, val.indexOf(":"))); 
        maxVolt = Float.parseFloat(val.substring(val.indexOf(":") + 1)); 
      }
    }
    System.out.println("Prms: ADC Channel:" + channel + " MinADC:" + minADC + ", MinVolt:" + minVolt + ", MaxADC:" + maxADC + ", maxVolt:" + maxVolt);
    
    Runtime.getRuntime().addShutdownHook(new Thread()
       {
         public void run()
         {
           System.out.println("\nShutting down");
           if (bw != null)
           {
             System.out.println("Closing log file");
             try 
             { 
               bw.flush();
               bw.close(); 
              } 
             catch (Exception ex) { ex.printStackTrace(); }
           }
           if (obs != null)
             obs.stop();
         }
       });    
    new BatteryMonitor(channel);
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
