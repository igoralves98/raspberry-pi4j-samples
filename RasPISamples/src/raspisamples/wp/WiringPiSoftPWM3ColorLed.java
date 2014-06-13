package raspisamples.wp;

import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.SoftPwm;

public class WiringPiSoftPWM3ColorLed
{

  public static void main(String[] args)
    throws InterruptedException
  {

    // initialize wiringPi library
    com.pi4j.wiringpi.Gpio.wiringPiSetup();

    int pinAddress_00 = RaspiPin.GPIO_00.getAddress(); 
    int pinAddress_01 = RaspiPin.GPIO_01.getAddress(); 
    int pinAddress_02 = RaspiPin.GPIO_02.getAddress(); 
    // create soft-pwm pins (min=0 ; max=100)

    SoftPwm.softPwmCreate(pinAddress_00, 0, 100); 
    SoftPwm.softPwmCreate(pinAddress_01, 0, 100); 
    SoftPwm.softPwmCreate(pinAddress_02, 0, 100); 

    // continuous loop
//  while (true)
    {
      // fade LED to fully ON
      for (int i = 0; i <= 100; i++)
      {
        SoftPwm.softPwmWrite(pinAddress_00, i);
        Thread.sleep(5);
      }

      // fade LED to fully OFF
      for (int i = 100; i >= 0; i--)
      {
        SoftPwm.softPwmWrite(pinAddress_00, i);
        Thread.sleep(5);
      }
      // fade LED to fully ON
      for (int i = 0; i <= 100; i++)
      {
        SoftPwm.softPwmWrite(pinAddress_01, i);
        Thread.sleep(5);
      }

      // fade LED to fully OFF
      for (int i = 100; i >= 0; i--)
      {
        SoftPwm.softPwmWrite(pinAddress_01, i);
        Thread.sleep(5);
      }
      // fade LED to fully ON
      for (int i = 0; i <= 100; i++)
      {
        SoftPwm.softPwmWrite(pinAddress_02, i);
        Thread.sleep(5);
      }

      // fade LED to fully OFF
      for (int i = 100; i >= 0; i--)
      {
        SoftPwm.softPwmWrite(pinAddress_02, i);
        Thread.sleep(5);
      }
    }
    
    // All spectrum
    for (int a = 0; a <= 100; a++)
    {
      SoftPwm.softPwmWrite(pinAddress_00, a);
//    Thread.sleep(5);
      for (int b = 0; b <= 100; b++)
      {
        SoftPwm.softPwmWrite(pinAddress_01, b);
//      Thread.sleep(5);
        for (int c = 0; c <= 100; c++)
        {
          SoftPwm.softPwmWrite(pinAddress_02, c);
          Thread.sleep(5);
        }
        for (int c = 100; c >= 0; c--)
        {
          SoftPwm.softPwmWrite(pinAddress_02, c);
          Thread.sleep(5);
        }
      }
      for (int b = 100; b >= 0; b--)
      {
        SoftPwm.softPwmWrite(pinAddress_01, b);
//      Thread.sleep(5);
        for (int c = 0; c <= 100; c++)
        {
          SoftPwm.softPwmWrite(pinAddress_02, c);
          Thread.sleep(5);
        }
        for (int c = 100; c >= 0; c--)
        {
          SoftPwm.softPwmWrite(pinAddress_02, c);
          Thread.sleep(5);
        }
      }
    }
    for (int a = 100; a >= 0; a--)
    {
      SoftPwm.softPwmWrite(pinAddress_00, a);
//    Thread.sleep(5);
      for (int b = 0; b <= 100; b++)
      {
        SoftPwm.softPwmWrite(pinAddress_01, b);
//      Thread.sleep(5);
        for (int c = 0; c <= 100; c++)
        {
          SoftPwm.softPwmWrite(pinAddress_02, c);
          Thread.sleep(5);
        }
        for (int c = 100; c >= 0; c--)
        {
          SoftPwm.softPwmWrite(pinAddress_02, c);
          Thread.sleep(5);
        }
      }
      for (int b = 100; b >= 0; b--)
      {
        SoftPwm.softPwmWrite(pinAddress_01, b);
//      Thread.sleep(5);
        for (int c = 0; c <= 100; c++)
        {
          SoftPwm.softPwmWrite(pinAddress_02, c);
          Thread.sleep(5);
        }
        for (int c = 100; c >= 0; c--)
        {
          SoftPwm.softPwmWrite(pinAddress_02, c);
          Thread.sleep(5);
        }
      }
    }
  }
}
