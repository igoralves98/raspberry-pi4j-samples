package adafruiti2c.sensor.listener;

import java.util.EventListener;
import ocss.nmea.api.NMEAEvent;

public abstract class AdafruitBMP180Listener implements EventListener
{
  public void dataDetected(NMEAEvent e) {}
}
