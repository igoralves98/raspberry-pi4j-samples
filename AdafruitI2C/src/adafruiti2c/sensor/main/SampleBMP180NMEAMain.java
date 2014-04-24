package adafruiti2c.sensor.main;

import adafruiti2c.sensor.nmea.AdafruitBMP180Reader;

import nmea.server.ctx.NMEAContext;

import ocss.nmea.api.NMEAEvent;
import ocss.nmea.api.NMEAListener;

/*
 * This one uses the listeners already existing in OlivSoft
 * (namely the NMEA Console)
 *
 * @see AdafruitBMP180Reader
 * @see NMEAContext
 */
public class SampleBMP180NMEAMain
{
  private final AdafruitBMP180Reader sensorReader = new AdafruitBMP180Reader();
  
  public SampleBMP180NMEAMain()
  {
    NMEAContext.getInstance().addNMEAListener(new NMEAListener()
    {
      @Override
      public void dataDetected(NMEAEvent event)
      {
        System.out.println("Pure NMEA:" + event.getContent());
      }
    });
  }
  
  public void start()
  {
    sensorReader.startReading();
  }
  
  public void stop()
  {
    sensorReader.stopReading();
    synchronized (Thread.currentThread())
    {
      System.out.println("... notifying main.");
      Thread.currentThread().notify();
    }
  }

  public static void main(String[] args)
  {
    final SampleBMP180NMEAMain reader = new SampleBMP180NMEAMain();
    Thread worker = new Thread("Reader")
      {
        public void run()
        {
          reader.start();
        }
      };
    Runtime.getRuntime().addShutdownHook(new Thread("Hook")
      {
        public void run()
        {
          System.out.println();            
          reader.stop();
          // Wait for everything to shutdown, for the example...
          try { Thread.sleep(2000L); } catch (InterruptedException ie) {}
        }
      });
    
    worker.start();
    synchronized (Thread.currentThread())
    {
      try 
      { 
        Thread.currentThread().wait(); 
      }
      catch (InterruptedException ie)
      {
        ie.printStackTrace();
      }
    }
  }
}
