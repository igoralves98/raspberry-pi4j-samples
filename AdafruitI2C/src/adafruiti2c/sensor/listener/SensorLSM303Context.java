package adafruiti2c.sensor.listener;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

public class SensorLSM303Context implements Serializable
{
  private static SensorLSM303Context context = null;
  private transient List<AdafruitLSM303Listener> sensorReaderListeners = null;
  
  private SensorLSM303Context()
  {
    sensorReaderListeners = new ArrayList<AdafruitLSM303Listener>();
  }
  
  public static synchronized SensorLSM303Context getInstance()
  {
    if (context == null)
      context = new SensorLSM303Context();    
    return context;
  }

  public List<AdafruitLSM303Listener> getReaderListeners()
  {
    return sensorReaderListeners;
  }    

  public synchronized void addReaderListener(AdafruitLSM303Listener l)
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

  public void fireDataDetected(int accX, int accY, int accZ, int magX, int magY, int magZ, float heading)
  {
    for (AdafruitLSM303Listener l : sensorReaderListeners)
    {
      l.dataDetected(accX, accY, accZ, magX, magY, magZ, heading);
    }
  }

  public void fireClose()
  {
    for (AdafruitLSM303Listener l : sensorReaderListeners)
    {
      l.close();
    }
  }
}
