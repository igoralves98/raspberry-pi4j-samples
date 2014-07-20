package rangesensor;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import java.text.DecimalFormat;
import java.text.Format;

/**
 * @see https://www.modmypi.com/blog/hc-sr04-ultrasonic-range-sensor-on-the-raspberry-pi
 * 
 */
public class HC_SR04
{
  private final static Format DF22 = new DecimalFormat("#0.00");
  private final static double SOUND_SPEED = 34300;           // in cm, 343 m/s
  private final static double DIST_FACT   = SOUND_SPEED / 2; // round trip
  private final static int MIN_DIST = 5;
  
  public static void main(String[] args)
    throws InterruptedException
  {
    System.out.println("GPIO Control - Range Sensor HC-SR04.");
    System.out.println("Will stop is distance is smaller than " + MIN_DIST + " cm");

    // create gpio controller
    final GpioController gpio = GpioFactory.getInstance();

    final GpioPinDigitalOutput trigPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "Trig", PinState.LOW);
    final GpioPinDigitalInput  echoPin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_05, "Echo");

    Runtime.getRuntime().addShutdownHook(new Thread()
    {
      public void run()
      {
        System.out.println("Oops!");
        gpio.shutdown();
        System.out.println("Exiting nicely.");
      }       
    });
    
    System.out.println("Waiting for the sensor to be ready (2s)...");
    Thread.sleep(2000);

    boolean go = true;
    System.out.println("Looping until the distance is less than " + MIN_DIST + " cm");
    while (go)
    {
      double start = 0d, end = 0d;
      trigPin.high();
      // 10 microsec to trigger the module  (8 ultrasound bursts at 40 kHz) 
      // https://www.dropbox.com/s/615w1321sg9epjj/hc-sr04-ultrasound-timing-diagram.png
      try { Thread.sleep(0, 10000); } catch (Exception ex) { ex.printStackTrace(); } 
      trigPin.low();
      
      // Wait for the signal to return
      while (echoPin.isLow())
        start = System.nanoTime();
      // There it is
      while (echoPin.isHigh())
        end = System.nanoTime();
      
      if (end > 0 && start > 0)
      {
        double pulseDuration = (end - start) / 1000000000d; // in seconds
        double distance = pulseDuration * DIST_FACT;
        if (distance < 1000) // Less than 10 meters
          System.out.println("Distance: " + DF22.format(distance) + " cm."); // + " (" + pulseDuration + " = " + end + " - " + start + ")");
        if (distance > 0 && distance < MIN_DIST)
          go = false;
        else
        {
          if (distance < 0)
            System.out.println("Dist:" + distance + ", start:" + start + ", end:" + end);
          try { Thread.sleep(1000L); } catch (Exception ex) {}
        }
      }
      else
      {
        System.out.println("Hiccup!");
        try { Thread.sleep(2000L); } catch (Exception ex) {}
      }
    }
    System.out.println("Done.");
    trigPin.low(); // Off

    gpio.shutdown();
  }
}
