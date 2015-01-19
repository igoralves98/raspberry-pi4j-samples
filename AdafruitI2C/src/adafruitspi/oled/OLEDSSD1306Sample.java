package adafruitspi.oled;

import adafruitspi.oled.img.ImgInterface;
import adafruitspi.oled.img.Java32x32;

public class OLEDSSD1306Sample
{
  public static void main(String[] args)
  {
    AdafruitSSD1306 oled = new AdafruitSSD1306();

    oled.begin();
    oled.clear();
//  oled.display();
    ScreenBuffer sb = new ScreenBuffer(128, 32);
    sb.clear();
    
    if (false)
    {
      sb.text("ScreenBuffer", 2, 8);
      sb.text("128 x 32 for OLED", 2, 17);
      sb.line(0, 19, 131, 19);
      sb.line(0, 32, 125, 19);
    }
    
    ImgInterface img = new Java32x32();
    sb.image(img, 0, 0);
    sb.text("I speak Java!", 36, 20);

    oled.setBuffer(sb.getScreenBuffer());
    oled.display();

    try { Thread.sleep(5000); } catch (Exception ex) {}

    // Marquee
    for (int i=0; i<128; i++)
    {
      oled.clear();
      sb.image(img, 0 - i, 0);
      sb.text("I speak Java!.......", 36 - i, 20);

      oled.setBuffer(sb.getScreenBuffer());
      oled.display();
//    try { Thread.sleep(250); } catch (Exception ex) {}
    }
    
    // Circles
    sb.clear();
    sb.circle(64, 16, 15);
    oled.setBuffer(sb.getScreenBuffer());          
    oled.display();
    try { Thread.sleep(1000); } catch (Exception ex) {}
    
    sb.circle(74, 16, 10);
    oled.setBuffer(sb.getScreenBuffer());          
    oled.display();
    try { Thread.sleep(1000); } catch (Exception ex) {}
    
    sb.circle(80, 16,  5);
    oled.setBuffer(sb.getScreenBuffer());          
    oled.display();
    try { Thread.sleep(1000); } catch (Exception ex) {}          

    // Lines
    sb.clear();
    sb.line(1, 1, 126, 30);
    oled.setBuffer(sb.getScreenBuffer());          
    oled.display();
    try { Thread.sleep(1000); } catch (Exception ex) {}
    
    sb.line(126, 1, 1, 30);
    oled.setBuffer(sb.getScreenBuffer());          
    oled.display();
    try { Thread.sleep(1000); } catch (Exception ex) {}
    
    sb.line(1, 25, 120, 10);
    oled.setBuffer(sb.getScreenBuffer());          
    oled.display();
    try { Thread.sleep(1000); } catch (Exception ex) {}
    
    sb.line(10, 5, 10, 30);
    oled.setBuffer(sb.getScreenBuffer());          
    oled.display();
    try { Thread.sleep(1000); } catch (Exception ex) {}
    
    sb.line(1, 5, 120, 5);
    oled.setBuffer(sb.getScreenBuffer());          
    oled.display();
    try { Thread.sleep(1000); } catch (Exception ex) {}          

    // Rectangles
    sb.clear();
    sb.rectangle(5, 10, 100, 25);
    oled.setBuffer(sb.getScreenBuffer());          
    oled.display();
    try { Thread.sleep(1000); } catch (Exception ex) {}

    sb.rectangle(15, 3, 50, 30);
    oled.setBuffer(sb.getScreenBuffer());          
    oled.display();
    try { Thread.sleep(1000); } catch (Exception ex) {}

    // Arc & plot
    sb.clear();
    sb.arc(64, 16, 10, 20, 90);
    sb.plot(64, 16);
    oled.setBuffer(sb.getScreenBuffer());          
    oled.display();
    try { Thread.sleep(1000); } catch (Exception ex) {}
    
    sb.clear();
    oled.clear();
    sb.text("Bye-bye!", 36, 20);

    oled.setBuffer(sb.getScreenBuffer());
    oled.display();
    
    oled.shutdown();
  }
}
