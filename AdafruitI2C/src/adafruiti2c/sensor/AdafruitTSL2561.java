package adafruiti2c.sensor;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/*
 * Light Sensor (I2C)
 */
public class AdafruitTSL2561
{
  public final static int TSL2561_ADDRESS = 0x39;

  public final static int TSL2561_ADDRESS_LOW   = 0x29;
  public final static int TSL2561_ADDRESS_FLOAT = 0x39;
  public final static int TSL2561_ADDRESS_HIGH  = 0x49;

  public final static int TSL2561_COMMAND_BIT      = 0x80;
  public final static int TSL2561_WORD_BIT         = 0x20;
  public final static int TSL2561_CONTROL_POWERON  = 0x03;
  public final static int TSL2561_CONTROL_POWEROFF = 0x00;

  public final static int TSL2561_REGISTER_CONTROL    = 0x00;
  public final static int TSL2561_REGISTER_TIMING     = 0x01;
  public final static int TSL2561_REGISTER_CHAN0_LOW  = 0x0C;
  public final static int TSL2561_REGISTER_CHAN0_HIGH = 0x0D;
  public final static int TSL2561_REGISTER_CHAN1_LOW  = 0x0E;
  public final static int TSL2561_REGISTER_CHAN1_HIGH = 0x0F;
  public final static int TSL2561_REGISTER_ID         = 0x0A;

  public final static int TSL2561_GAIN_1X  = 0x00;
  public final static int TSL2561_GAIN_16X = 0x10;

  public final static int TSL2561_INTEGRATIONTIME_13MS  = 0x00; // rather 13.7ms
  public final static int TSL2561_INTEGRATIONTIME_101MS = 0x01;
  public final static int TSL2561_INTEGRATIONTIME_402MS = 0x02;
  
  private static boolean verbose = false;
  private int gain        = TSL2561_GAIN_1X;
  private int integration = TSL2561_INTEGRATIONTIME_402MS;
  private long pause = 800L;

  private I2CBus bus;
  private I2CDevice tsl2561;

  public AdafruitTSL2561()
  {
    this(TSL2561_ADDRESS);
  }

  public AdafruitTSL2561(int address)
  {
    try
    {
      // Get i2c bus
      bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends onthe RasPI version
      if (verbose)
      {
        System.out.println("Connected to bus. OK.");
      }

      // Get device itself
      tsl2561 = bus.getDevice(address);
      if (verbose)
      {
        System.out.println("Connected to device. OK.");
      }
      turnOn();
    }
    catch (IOException e)
    {
      System.err.println(e.getMessage());
    }
  }
  
  public void turnOn() throws IOException
  {
    tsl2561.write(TSL2561_COMMAND_BIT, (byte)TSL2561_CONTROL_POWERON);    
  }

  public void turnOff() throws IOException
  {
    tsl2561.write(TSL2561_COMMAND_BIT, (byte)TSL2561_CONTROL_POWEROFF);    
  }

  public void setGain() throws IOException
  {
    setGain(TSL2561_GAIN_1X);
  }
  public void setGain(int gain) throws IOException
  {
    setGain(gain, TSL2561_INTEGRATIONTIME_402MS);
  }
  public void setGain(int gain, int integration) throws IOException
  {
    if (gain != TSL2561_GAIN_1X && gain != TSL2561_GAIN_16X)
      throw new IllegalArgumentException("Bad  gain value [" + gain + "]");
    
    if (gain != this.gain || integration != this.integration)
    {
        tsl2561.write(TSL2561_COMMAND_BIT | TSL2561_REGISTER_TIMING, (byte)(gain | integration)); 
        if (verbose)
          System.out.println("Setting low gain");
      this.gain = gain;
      this.integration = integration;
      waitfor(pause); // pause for integration (pause must be bigger than integration time)
    }
  }

  /*
   * Reads visible+IR diode from the I2C device
   */
  public int readFull() throws Exception
  {
    int reg = TSL2561_COMMAND_BIT | TSL2561_REGISTER_CHAN0_LOW;
    return readU16Rev(reg);
  }

  /*
   * Reads IR only diode from the I2C device
   */
  public int readIR() throws Exception
  {
    int reg = TSL2561_COMMAND_BIT | TSL2561_REGISTER_CHAN1_LOW;
    return readU16Rev(reg);
  }

  public double readLux() throws Exception
  {
    int ambient = this.readFull();
    int IR      = this.readIR();

    if (ambient >= 0xffff || IR >= 0xffff) // value(s) exeed(s) datarange
      throw new RuntimeException("Gain too high. Values exceed range.");

    if (this.gain == TSL2561_GAIN_1X)
    {
      ambient *= 16;    // scale 1x to 16x
      IR *= 16;         // scale 1x to 16x
    }                
    double ratio = (IR / (float)ambient);

    if (verbose)
    {
      System.out.println("IR Result" + IR);
      System.out.println("Ambient Result" + ambient);
    }
    double lux = 0d;
    if ((ratio >= 0) && (ratio <= 0.52))
      lux = (0.0315 * ambient) - (0.0593 * ambient * (Math.pow(ratio, 1.4)));
    else if (ratio <= 0.65)
      lux = (0.0229 * ambient) - (0.0291 * IR);
    else if (ratio <= 0.80)
      lux = (0.0157 * ambient) - (0.018 * IR);
    else if (ratio <= 1.3)
      lux = (0.00338 * ambient) - (0.0026 * IR);
    else if (ratio > 1.3)
      lux = 0;

    return lux;
  }  
  
  /*
   * Read an unsigned byte from the I2C device
   */
  private int readU8(int reg) throws Exception
  {
    int result = 0;
    try
    {
      result = this.tsl2561.read(reg);
      if (verbose)
      {
        System.out.println("I2C: Device " + TSL2561_ADDRESS + " returned " + result + " from reg " + reg);
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return result;
  }

  /*
   * Reads a signed byte from the I2C device
   */
  private int readS8(int reg) throws Exception
  {
    int result = 0;
    try
    {
      result = this.tsl2561.read(reg);
      if (result > 127)
      {
        result -= 256;
      }
      if (verbose)
      {
        System.out.println("I2C: Device " + TSL2561_ADDRESS + " returned " + result + " from reg " + reg);
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return result;
  }

  private int readU16(int register) throws Exception
  {
    int hi = this.readU8(register);
    int lo = this.readU8(register + 1);
    return (hi << 8) + lo;
  }

  private int readS16(int register) throws Exception
  {
    int hi = this.readS8(register);
    int lo = this.readU8(register + 1);
    return (hi << 8) + lo;
  }

  private int readU16Rev(int register) throws Exception
  {
    int lo = this.readU8(register);
    int hi = this.readU8(register + 1);
    return (hi << 8) + lo;
  }
  
  private static void waitfor(long howMuch)
  {
    try
    {
      Thread.sleep(howMuch);
    }
    catch (InterruptedException ie)
    {
      ie.printStackTrace();
    }
  }

  public static void main(String[] args)
  {
    final NumberFormat NF = new DecimalFormat("##00.00");
    AdafruitTSL2561 sensor = new AdafruitTSL2561();
    double lux = 0;

    try
    {
      lux = sensor.readLux();
      System.out.println("Lux: " + NF.format(lux) + " Lux");
      sensor.turnOff();
    }
    catch (Exception ex)
    {
      System.err.println(ex.getMessage());
      ex.printStackTrace();
    }
  }
}
