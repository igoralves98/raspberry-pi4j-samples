package raspisamples.servo;

import adafruiti2c.servo.AdafruitPCA9685;

/*
 * Standard, using I2C and the PCA9685 servo board
 */
public class StandardServo
{
  private static void waitfor(long howMuch)
  {
    try { Thread.sleep(howMuch); } catch (InterruptedException ie) { ie.printStackTrace(); }
  }

  private int servo = -1;
  private final static int SERVO_MIN = 122; 
  private final static int SERVO_MAX = 615; 
  private final static int DIFF = SERVO_MAX - SERVO_MIN;

  private AdafruitPCA9685 servoBoard = new AdafruitPCA9685();

  public StandardServo(int channel)
  {
    int freq = 60;
    servoBoard.setPWMFreq(freq); // Set frequency in Hz
    
    this.servo = channel;
    System.out.println("Channel " + channel + " all set. Min:" + SERVO_MIN + ", Max:" + SERVO_MAX + ", diff:" + DIFF);    
  }
  
  public void setAngle(float f)
  {
    int pwm = degreeToPWM(SERVO_MIN, SERVO_MAX, f);
 // System.out.println(f + " degrees (" + pwm + ")");
    servoBoard.setPWM(servo, 0, pwm);    
  }

  public void setPWM(int pwm)
  {
    servoBoard.setPWM(servo, 0, pwm);    
  }
  
  public void stop() // Set to 0
  {
    servoBoard.setPWM(servo, 0, 0);  
  }
  /*
   * deg in [-90..90]
   */
  private static int degreeToPWM(int min, int max, float deg)
  {
    int diff = max - min;
    float oneDeg = diff / 180f;
    return Math.round(min + ((deg + 90) * oneDeg));
  }   
  
  public static void main(String[] args)
  {
    StandardServo ss = new StandardServo(14);
    try
    {
      ss.stop();
      waitfor(2000);
      System.out.println("Let's go, 1 by 1");
      for (int i=SERVO_MIN; i<=SERVO_MAX; i++)
      {
        System.out.println("i=" + i + ", " + (-90f + (((float)(i - SERVO_MIN) / (float)DIFF) * 180f)));
        ss.setPWM(i);
        waitfor(10);
      } 
      for (int i=SERVO_MAX; i>=SERVO_MIN; i--)
      {
        System.out.println("i=" + i + ", " + (-90f + (((float)(i - SERVO_MIN) / (float)DIFF) * 180f)));
        ss.setPWM(i);
        waitfor(10);
      } 
      ss.stop();
      waitfor(2000);
      System.out.println("Let's go, 1 deg by 1 deg");
      for (int i=SERVO_MIN; i<=SERVO_MAX; i+=(DIFF / 180))
      {
        System.out.println("i=" + i + ", " + Math.round(-90f + (((float)(i - SERVO_MIN) / (float)DIFF) * 180f)));
        ss.setPWM(i);
        waitfor(10);
      } 
      for (int i=SERVO_MAX; i>=SERVO_MIN; i-=(DIFF / 180))
      {
        System.out.println("i=" + i + ", " + Math.round(-90f + (((float)(i - SERVO_MIN) / (float)DIFF) * 180f)));
        ss.setPWM(i);
        waitfor(10);
      } 
      ss.stop();
      waitfor(2000);
      
      float[] degValues = { -10, 0, -90, 45, -30, 90, 10, 20, 30, 40, 50, 60, 70, 80, 90, 0 };
      for (float f : degValues)
      {
        ss.setAngle(f);
        waitfor(1500);
      }
    }
    finally
    {
      ss.stop();
    }
    
    System.out.println("Done.");
  }
}
