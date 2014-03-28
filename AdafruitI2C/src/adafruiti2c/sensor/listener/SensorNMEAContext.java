package adafruiti2c.sensor.listener;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

import ocss.nmea.api.NMEAEvent;

public class SensorNMEAContext implements Serializable
{
  private static SensorNMEAContext context = null;
  private transient List<AdafruitBMP180Listener> sensorReaderListeners = null;
  
  private SensorNMEAContext()
  {
    sensorReaderListeners = new ArrayList<AdafruitBMP180Listener>();
  }
  
  public static synchronized SensorNMEAContext getInstance()
  {
    if (context == null)
      context = new SensorNMEAContext();    
    return context;
  }

  public List<AdafruitBMP180Listener> getReaderListeners()
  {
    return sensorReaderListeners;
  }    

  public synchronized void addReaderListener(AdafruitBMP180Listener l)
  {
    if (!sensorReaderListeners.contains(l))
    {
      sensorReaderListeners.add(l);
    }
  }

  public synchronized void removeReaderListener(AdafruitBMP180Listener l)
  {
    sensorReaderListeners.remove(l);
  }

  public void fireDataDetected(NMEAEvent event)
  {
    for (AdafruitBMP180Listener l : sensorReaderListeners)
    {
      l.dataDetected(event);
    }
  }
}
