package adafruiti2c.sensor;

import com.pi4j.system.NetworkInfo;
import com.pi4j.system.SystemInfo;

import java.io.IOException;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import ocss.nmea.parser.StringGenerator;

/*
 * Altitude, Pressure, Temperature
 */
public class AdafruitBMP180NMEA extends AdafruitBMP180
{
  private static void displaySysInfo() throws InterruptedException, IOException, ParseException
  {
    System.out.println("----------------------------------------------------");
    System.out.println("HARDWARE INFO");
    System.out.println("----------------------------------------------------");
    System.out.println("Serial Number     :  " + SystemInfo.getSerial());
    System.out.println("CPU Revision      :  " + SystemInfo.getCpuRevision());
    System.out.println("CPU Architecture  :  " + SystemInfo.getCpuArchitecture());
    System.out.println("CPU Part          :  " + SystemInfo.getCpuPart());
    System.out.println("CPU Temperature   :  " + SystemInfo.getCpuTemperature());
    System.out.println("CPU Core Voltage  :  " + SystemInfo.getCpuVoltage());
//  System.out.println("MIPS              :  " + SystemInfo.getBogoMIPS());
    try { System.out.println("Processor         :  " + SystemInfo.getProcessor()); } catch (Exception ex) { System.out.println("Processor: Oops."); }
    System.out.println("Hardware Revision :  " + SystemInfo.getRevision());
    System.out.println("Is Hard Float ABI :  " + SystemInfo.isHardFloatAbi());
    System.out.println("Board Type        :  " + SystemInfo.getBoardType().name());
    
    System.out.println("----------------------------------------------------");
    System.out.println("MEMORY INFO");
    System.out.println("----------------------------------------------------");
    System.out.println("Total Memory      :  " + SystemInfo.getMemoryTotal());
    System.out.println("Used Memory       :  " + SystemInfo.getMemoryUsed());
    System.out.println("Free Memory       :  " + SystemInfo.getMemoryFree());
    System.out.println("Shared Memory     :  " + SystemInfo.getMemoryShared());
    System.out.println("Memory Buffers    :  " + SystemInfo.getMemoryBuffers());
    System.out.println("Cached Memory     :  " + SystemInfo.getMemoryCached());
    System.out.println("SDRAM_C Voltage   :  " + SystemInfo.getMemoryVoltageSDRam_C());
    System.out.println("SDRAM_I Voltage   :  " + SystemInfo.getMemoryVoltageSDRam_I());
    System.out.println("SDRAM_P Voltage   :  " + SystemInfo.getMemoryVoltageSDRam_P());

    System.out.println("----------------------------------------------------");
    System.out.println("OPERATING SYSTEM INFO");
    System.out.println("----------------------------------------------------");
    System.out.println("OS Name           :  " + SystemInfo.getOsName());
    System.out.println("OS Version        :  " + SystemInfo.getOsVersion());
    System.out.println("OS Architecture   :  " + SystemInfo.getOsArch());
    System.out.println("OS Firmware Build :  " + SystemInfo.getOsFirmwareBuild());
    System.out.println("OS Firmware Date  :  " + SystemInfo.getOsFirmwareDate());
    
    System.out.println("----------------------------------------------------");
    System.out.println("JAVA ENVIRONMENT INFO");
    System.out.println("----------------------------------------------------");
    System.out.println("Java Vendor       :  " + SystemInfo.getJavaVendor());
    System.out.println("Java Vendor URL   :  " + SystemInfo.getJavaVendorUrl());
    System.out.println("Java Version      :  " + SystemInfo.getJavaVersion());
    System.out.println("Java VM           :  " + SystemInfo.getJavaVirtualMachine());
    System.out.println("Java Runtime      :  " + SystemInfo.getJavaRuntime());
    
    System.out.println("----------------------------------------------------");
    System.out.println("NETWORK INFO");
    System.out.println("----------------------------------------------------");
    
    // display some of the network information
    System.out.println("Hostname          :  " + NetworkInfo.getHostname());
    for (String ipAddress : NetworkInfo.getIPAddresses())
        System.out.println("IP Addresses      :  " + ipAddress);
    for (String fqdn : NetworkInfo.getFQDNs())
        System.out.println("FQDN              :  " + fqdn);
    for (String nameserver : NetworkInfo.getNameservers())
        System.out.println("Nameserver        :  " + nameserver);
    
    System.out.println("----------------------------------------------------");
    System.out.println("CODEC INFO");
    System.out.println("----------------------------------------------------");
    System.out.println("H264 Codec Enabled:  " + SystemInfo.getCodecH264Enabled());
    System.out.println("MPG2 Codec Enabled:  " + SystemInfo.getCodecMPG2Enabled());
    System.out.println("WVC1 Codec Enabled:  " + SystemInfo.getCodecWVC1Enabled());

    System.out.println("----------------------------------------------------");
    System.out.println("CLOCK INFO");
    System.out.println("----------------------------------------------------");
    System.out.println("ARM Frequency     :  " + SystemInfo.getClockFrequencyArm());
    System.out.println("CORE Frequency    :  " + SystemInfo.getClockFrequencyCore());
    System.out.println("H264 Frequency    :  " + SystemInfo.getClockFrequencyH264());
    System.out.println("ISP Frequency     :  " + SystemInfo.getClockFrequencyISP());
    System.out.println("V3D Frequency     :  " + SystemInfo.getClockFrequencyV3D());
    System.out.println("UART Frequency    :  " + SystemInfo.getClockFrequencyUART());
    System.out.println("PWM Frequency     :  " + SystemInfo.getClockFrequencyPWM());
    System.out.println("EMMC Frequency    :  " + SystemInfo.getClockFrequencyEMMC());
    System.out.println("Pixel Frequency   :  " + SystemInfo.getClockFrequencyPixel());
    System.out.println("VEC Frequency     :  " + SystemInfo.getClockFrequencyVEC());
    System.out.println("HDMI Frequency    :  " + SystemInfo.getClockFrequencyHDMI());
    System.out.println("DPI Frequency     :  " + SystemInfo.getClockFrequencyDPI());    
  }
  
  private static boolean go = true;
  
  public static void main(String[] args)
  {
    final NumberFormat NF = new DecimalFormat("##00.00");
    AdafruitBMP180NMEA sensor = new AdafruitBMP180NMEA();
    
    Runtime.getRuntime().addShutdownHook(new Thread()
     {
       public void run()
       {
         System.out.println("Exiting.");
         go = false; 
       }       
     });
    
    try
    {
      displaySysInfo();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    
    while (go)
    {
      float press = 0;
      float temp  = 0;
      double alt  = 0;
  
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
      
      String nmeaMMB = StringGenerator.generateMMB("II", (press / 100));
      String nmeaMTA = StringGenerator.generateMTA("II", temp);
      
      System.out.println(NF.format(press / 100) + " hPa  " + nmeaMMB);
      System.out.println(NF.format(temp) + " C      " + nmeaMTA);
      
//    System.out.println("Temperature: " + NF.format(temp) + " C");
//    System.out.println("Pressure   : " + NF.format(press / 100) + " hPa");
//    System.out.println("Altitude   : " + NF.format(alt) + " m");
      waitfor(1000L);
    }
    System.out.println("Bye...");
  }
}
