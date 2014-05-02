package adafruiti2c.sensor.main;

import adafruiti2c.sensor.AdafruitL3GD20;
import adafruiti2c.sensor.listener.SensorL3GD20Context;
import adafruiti2c.sensor.utils.L3GD20Dictionaries;

/*
 * Read real data,
 * and broadcast to a listener
 */
public class SampleL3GD20RealReader
{
  private boolean go = true;
  private AdafruitL3GD20 sensor;
  private double refX = 0, refY = 0, refZ = 0;
  
  public SampleL3GD20RealReader() throws Exception
  {
    sensor = new AdafruitL3GD20();
    sensor.setPowerMode(L3GD20Dictionaries.NORMAL);
    sensor.setFullScaleValue(L3GD20Dictionaries._250_DPS);
    sensor.setAxisXEnabled(true);
    sensor.setAxisYEnabled(true);
    sensor.setAxisZEnabled(true);

    sensor.init();
    sensor.calibrate();
  }
  
  public void start() throws Exception
  {
    long wait = 20L;
    double x = 0, y = 0, z = 0;
    while (go)
    {      
      double[] data = sensor.getCalOutValue();
      x = data[0];
      y = data[1];
      z = data[2];
      // Broadcast if needed
      if (Math.abs(x - refX) > 1 || Math.abs(y - refY) > 1 || Math.abs(z - refZ) > 1)
      {
        refX = x;
        refY = y;
        refZ = z;
        SensorL3GD20Context.getInstance().fireDataDetected(x, y, z);
      }    
//    System.out.printf("X:%.2f, Y:%.2f, Z:%.2f%n", x, y, z);
      try { Thread.sleep(wait); } catch (InterruptedException ex) {}
    }
  }
  
  public void stop()
  {
    this.go = false;
  }
}
