package raspisamples;

import adafruitspi.oled.AdafruitSSD1306;

import adafruitspi.oled.ScreenBuffer;

import com.pi4j.io.gpio.RaspiPin;

import phonekeyboard3x4.KeyboardController;

public class OLEDScreenAndKeypad
{
  private KeyboardController kbc;
  private AdafruitSSD1306 oled;
  private ScreenBuffer sb;
  
  public OLEDScreenAndKeypad()
  {
    kbc = new KeyboardController();

    oled = new AdafruitSSD1306(RaspiPin.GPIO_12, RaspiPin.GPIO_13, RaspiPin.GPIO_14, RaspiPin.GPIO_15, RaspiPin.GPIO_16);
    oled.begin();
    oled.clear();

    sb = new ScreenBuffer(128, 32);
    sb.clear();

    oled.setBuffer(sb.getScreenBuffer());
    oled.display();
  }
  
  public void display(String txt)
  {
    sb.text(txt, 2, 17);
    oled.setBuffer(sb.getScreenBuffer());
    oled.display();
  }
  
  public void userInput()
  {
    StringBuffer charBuff = new StringBuffer();
    boolean go = true;
    while (go)
    {
      char c = kbc.getKey();    
      System.out.println("At " + System.currentTimeMillis() + ", Char: " + c);
      if (c == '#')
        go = false;
      else if (c == '*')
      {
        charBuff = new StringBuffer();
        reset();
      }
      else
        charBuff.append(c);
      display(charBuff.toString());
      try { Thread.sleep(200L); } catch (Exception ex) {}
    }
    reset();
    display("Bye-bye");
    System.out.println("Bye");
    kbc.shutdown();
    try { Thread.sleep(1000L); } catch (Exception ex) {}
    reset();
  }

  public void reset()
  {
    sb.clear();
    oled.clear();
    oled.setBuffer(sb.getScreenBuffer());
    oled.display();    
  }
  
  public static void main(String[] args)
  {
    System.out.println("Hit * to reset");
    System.out.println("Hit # to exit");
    OLEDScreenAndKeypad ui = new OLEDScreenAndKeypad();
    ui.userInput();
  }
}
