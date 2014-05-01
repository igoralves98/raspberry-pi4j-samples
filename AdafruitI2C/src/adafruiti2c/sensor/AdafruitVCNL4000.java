package adafruiti2c.sensor;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import com.pi4j.system.SystemInfo;

import java.io.IOException;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/*
 * Proximity sensor
 */
public class AdafruitVCNL4000
{
  public final static int LITTLE_ENDIAN = 0;
  public final static int BIG_ENDIAN    = 1;
  private final static int VCNL4000_ENDIANNESS = LITTLE_ENDIAN;
  /*
  Prompt> sudo i2cdetect -y 1
       0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f
  00:          -- -- -- -- -- -- -- -- -- -- -- -- --
  10: -- -- -- 13 -- -- -- -- -- -- -- -- -- -- -- --
  20: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
  30: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
  40: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
  50: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
  60: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
  70: -- -- -- -- -- -- -- --
   */
  // This next addresses is returned by "sudo i2cdetect -y 1", see above.
  public final static int VCNL4000_ADDRESS = 0x13; 
  // Commands
  public final static int VCNL4000_COMMAND          = 0x80;
  public final static int VCNL4000_PRODUCTID        = 0x81;
  public final static int VCNL4000_IRLED            = 0x83;
  public final static int VCNL4000_AMBIENTPARAMETER = 0x84;
  public final static int VCNL4000_AMBIENTDATA      = 0x85;
  public final static int VCNL4000_PROXIMITYDATA    = 0x87;
  public final static int VCNL4000_SIGNALFREQ       = 0x89;
  public final static int VCNL4000_PROXINITYADJUST  = 0x8A;

  public final static int VCNL4000_3M125   = 0;
  public final static int VCNL4000_1M5625  = 1;
  public final static int VCNL4000_781K25  = 2;
  public final static int VCNL4000_390K625 = 3;

  public final static int VCNL4000_MEASUREAMBIENT   = 0x10;
  public final static int VCNL4000_MEASUREPROXIMITY = 0x08;
  public final static int VCNL4000_AMBIENTREADY     = 0x40;
  public final static int VCNL4000_PROXIMITYREADY   = 0x20;

  private static boolean verbose = false;
  
  private I2CBus bus;
  private I2CDevice vcnl4000;
  
  public AdafruitVCNL4000()
  {
    this(VCNL4000_ADDRESS);
  }
  
  public AdafruitVCNL4000(int address)
  {
    try
    {
      // Get i2c bus
      bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends onthe RasPI version
      if (verbose)
        System.out.println("Connected to bus. OK.");

      // Get device itself
      vcnl4000 = bus.getDevice(address);
      if (verbose)
        System.out.println("Connected to device. OK.");
      
      vcnl4000.write(VCNL4000_PROXINITYADJUST, (byte)0x81);
    }
    catch (IOException e)
    {
      System.err.println(e.getMessage());
    }
  }
  
  private int readU8(int reg) throws Exception
  {
    // "Read an unsigned byte from the I2C device"
    int result = 0;
    try
    {
      result = this.vcnl4000.read(reg);
      if (verbose)
        System.out.println("(U8) I2C: Device " + toHex(VCNL4000_ADDRESS) + " returned " + toHex(result) + " from reg " + toHex(reg));
    }
    catch (Exception ex)
    { ex.printStackTrace(); }
    return result;
  }
  
  private int readS8(int reg) throws Exception
  {
    // "Reads a signed byte from the I2C device"
    int result = 0;
    try
    {
      result = this.vcnl4000.read(reg);
      if (result > 127)
        result -= 256;
      if (verbose)
        System.out.println("(S8) I2C: Device " + toHex(VCNL4000_ADDRESS) + " returned " + toHex(result) + " from reg " + toHex(reg));
    }
    catch (Exception ex)
    { ex.printStackTrace(); }
    return result;
  }
  
  private int readU16(int register) throws Exception
  {
    int hi = this.readU8(register);
    int lo = this.readU8(register + 1);
//  int result = (hi << 8) + lo;
    int result = (VCNL4000_ENDIANNESS == BIG_ENDIAN)? (hi << 8) + lo : (lo << 8) + hi; // Little endian for VCNL4000
    
    if (verbose)
      System.out.println("(U16) I2C: Device " + toHex(VCNL4000_ADDRESS) + " returned " + toHex(result) + " from reg " + toHex(register));
    return result;
  }

  private int readS16(int register) throws Exception
  {
    int hi = this.readS8(register);
    int lo = this.readU8(register + 1);
//  int result = (hi << 8) + lo;
    int result = (lo << 8) + hi; // Little endian
    if (verbose)
      System.out.println("(U16) I2C: Device " + toHex(VCNL4000_ADDRESS) + " returned " + toHex(result) + " from reg " + toHex(register));
    return result;
  }

  public int readProximity() throws Exception
  {
    int prox = 0;
    vcnl4000.write(VCNL4000_COMMAND, (byte)VCNL4000_MEASUREPROXIMITY);
    boolean keepTrying = true;
    while (keepTrying)
    {
      int cmd = this.readU8(VCNL4000_COMMAND);
      if (verbose)
        System.out.println("DBG: Proximity: " + (cmd & 0xFFFF) + ", " + cmd + " (" + VCNL4000_PROXIMITYREADY + ")");
      if (((cmd & 0xff) & VCNL4000_PROXIMITYREADY) != 0)
      {
        keepTrying = false;
        prox = this.readU16(VCNL4000_PROXIMITYDATA);
      }
      else
        waitfor(1);  // Wait 1ms
    }
    return prox;
  }
      
  private static String toHex(int i)
  {
    String s = Integer.toString(i, 16).toUpperCase();
    while (s.length() % 2 != 0)
      s = "0" + s;
    return "0x" + s;
  }
  
  private static void waitfor(long howMuch)
  {
    try { Thread.sleep(howMuch); } catch (InterruptedException ie) { ie.printStackTrace(); }
  }
  
  private static boolean go = true;
  
  public static void main(String[] args)
  {
    AdafruitVCNL4000 sensor = new AdafruitVCNL4000();
    int prox = 0;

    // Bonus : CPU Temperature
    try
    {
      System.out.println("CPU Temperature   :  " + SystemInfo.getCpuTemperature());
      System.out.println("CPU Core Voltage  :  " + SystemInfo.getCpuVoltage());
    }
    catch (InterruptedException ie)
    {
      ie.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }

    Runtime.getRuntime().addShutdownHook(new Thread()
                                         {
                                           public void run()
                                           {
                                             go = false;
                                             System.out.println("\nBye");
                                           }
                                         });
    int i = 0;
    while (go && i++ < 5)
    {
      try { prox = sensor.readProximity(); } 
      catch (Exception ex) 
      { 
        System.err.println(ex.getMessage()); 
        ex.printStackTrace();
      }
      System.out.println("Proximity: " + prox); //  + " unit?");
      try { Thread.sleep(100L); } catch (InterruptedException ex) {}
    }
  }
}
