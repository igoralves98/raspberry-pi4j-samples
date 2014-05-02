package adafruiti2c.sensor.listener;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

public class SensorL3GD20Context implements Serializable
{
  private static SensorL3GD20Context context = null;
  private transient List<AdafruitL3GD20Listener> sensorReaderListeners = null;
  
  private SensorL3GD20Context()
  {
    sensorReaderListeners = new ArrayList<AdafruitL3GD20Listener>();
  }
  
  public static synchronized SensorL3GD20Context getInstance()
  {
    if (context == null)
      context = new SensorL3GD20Context();    
    return context;
  }

  public List<AdafruitL3GD20Listener> getReaderListeners()
  {
    return sensorReaderListeners;
  }    

  public synchronized void addReaderListener(AdafruitL3GD20Listener l)
  {
    if (!sensorReaderListeners.contains(l))
    {
      sensorReaderListeners.add(l);
    }
  }

  public synchronized void removeReaderListener(AdafruitL3GD20Listener l)
  {
    sensorReaderListeners.remove(l);
  }

  public void fireDataDetected(double x, double y, double z)
  {
    for (AdafruitL3GD20Listener l : sensorReaderListeners)
    {
      l.dataDetected(x, y, z);
    }
  }

  public void fireClose()
  {
    for (AdafruitL3GD20Listener l : sensorReaderListeners)
    {
      l.close();
    }
  }
}
