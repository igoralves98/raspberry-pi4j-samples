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
  
  public static void main(String[] args)
    throws InterruptedException
  {
    System.out.println("GPIO Control - Range Sensor HC-SR04.");

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
    System.out.println("Looping until the distance is less than 10 cm");
    while (go)
    {
      double start = 0d, end = 0d;
      trigPin.high();
      try { Thread.sleep(0, 10000); } catch (Exception ex) { ex.printStackTrace(); } // 10 microsec to trigger the module  (8 ultrasound bursts at 40 kHz) 
      trigPin.low();
      
      // Wait for the signal to return
      while (echoPin.isLow())
        start = System.nanoTime();
      // There it is
      while (echoPin.isHigh())
        end = System.nanoTime();
      
      double pulseDuration = (end - start) / 1000000000d; // in seconds
      double distance = pulseDuration * DIST_FACT;
      System.out.println("Distance: " + DF22.format(distance) + " cm."); // + " (" + pulseDuration + " = " + end + " - " + start + ")");
      if (distance < 10)
        go = false;
      else
        try { Thread.sleep(1000L); } catch (Exception ex) {}
    }
    System.out.println("Done.");
    trigPin.low(); // Off

    gpio.shutdown();
  }
}
