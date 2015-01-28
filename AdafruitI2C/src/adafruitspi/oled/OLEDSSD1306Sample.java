package adafruitspi.oled;

import adafruitspi.oled.img.ImgInterface;
import adafruitspi.oled.img.Java32x32;

import java.awt.Point;
import java.awt.Polygon;

public class OLEDSSD1306Sample
{
  @SuppressWarnings("oracle.jdeveloper.java.insufficient-catch-block")
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

    // Shape
    sb.clear();
    int[] x = new int[] { 64, 73, 50, 78, 55 };
    int[] y = new int[] {  1, 30, 12, 12, 30 };
    Polygon p = new Polygon(x, y, 5);
    sb.shape(p, true);
    oled.setBuffer(sb.getScreenBuffer());          
    oled.display();
    try { Thread.sleep(1000); } catch (Exception ex) {}
  
  // Centered text
    sb.clear();
    String txt = "Centered";
    int len = sb.strlen(txt);
    sb.text(txt, 64 - (len/2), 16);
    oled.setBuffer(sb.getScreenBuffer());          
    oled.display();
    try { Thread.sleep(1000); } catch (Exception ex) {}
  // sb.clear();
    txt = "A much longer string.";
    len = sb.strlen(txt);
    sb.text(txt, 64 - (len/2), 26);
    oled.setBuffer(sb.getScreenBuffer());          
    oled.display();
    try { Thread.sleep(1000); } catch (Exception ex) {}
  
  // Vertical marquee
    String[] txtA = new String[] {
      "Centered",
      "This is line one",
      "More text goes here",
      "Some crap follows", 
      "We're reaching the end",
      "* The End *"
    };
    len = 0;
    sb.clear();
    for (int t=0; t<80; t++)
    {
//    sb.clear();
      for (int i=0; i<txtA.length; i++)
      {
        len = sb.strlen(txtA[i]);
        sb.text(txtA[i], 64 - (len/2), (10 * (i+1)) - t);
        oled.setBuffer(sb.getScreenBuffer());          
        oled.display();
      }
//    try { Thread.sleep(100); } catch (Exception ex) {}
    }
    
    // Text Snake...
    String snake = "This text is displayed like a snake, waving across the screen...";
    char[] ca = snake.toCharArray();
    int strlen = sb.strlen(snake);
    // int i = 0;
    for (int i=0; i<strlen + 2; i++)
    {
      sb.clear();
      for (int c=0; c<ca.length; c++)
      {
        int strOffset = 0;
        if (c > 0)
        {
          String tmp = new String(ca, 0, c);
    //    System.out.println(tmp);
          strOffset = sb.strlen(tmp) + 2;
        }
        double virtualAngle = Math.PI * (((c - i) % 32) / 32d);
        int xpos = strOffset - i,
            ypos = 26 + (int)(16 * Math.sin(virtualAngle)); 
    //          System.out.println("Displaying " + ca[c] + " at " + x + ", " + y + ", i=" + i + ", strOffset=" + strOffset);
        sb.text(new String(new char[] { ca[c] }), xpos, ypos);             
      }
      oled.setBuffer(sb.getScreenBuffer());          
      oled.display();
//    try { Thread.sleep(75); } catch (Exception ex) {}
    }
    
    // A curve
    sb.clear();
    // Axis
    sb.line(0, 16, 128, 16);
    sb.line(2, 0, 2, 32);
    
    Point prev = null;
    for (int _x=0; _x<130; _x++)
    {
      double amplitude = 6 * Math.exp((double)(130 - _x) / (13d * 7.5d)); 
    //  System.out.println("X:" + x + ", ampl: " + (amplitude));
      int _y = 16 - (int)(amplitude * Math.cos(Math.toRadians(360 * _x / 16d)));
      sb.plot(_x + 2, _y);
      if (prev != null)
        sb.line(prev.x, prev.y, _x+2, _y);
      prev = new Point(_x+2, _y);
    }
    oled.setBuffer(sb.getScreenBuffer());          
    oled.display();
    try { Thread.sleep(1000); } catch (Exception ex) {}
        
    // A curve (porgressing)
    sb.clear();
    // Axis
    sb.line(0, 16, 128, 16);
    sb.line(2, 0, 2, 32);
    
    prev = null;
    for (int _x=0; _x<130; _x++)
    {
      double amplitude = 6 * Math.exp((double)(130 - _x) / (13d * 7.5d)); 
    //  System.out.println("X:" + x + ", ampl: " + (amplitude));
      int _y = 16 - (int)(amplitude * Math.cos(Math.toRadians(360 * _x / 16d)));
      sb.plot(_x + 2, _y);
      if (prev != null)
        sb.line(prev.x, prev.y, _x+2, _y);
      prev = new Point(_x+2, _y);
      oled.setBuffer(sb.getScreenBuffer());          
      oled.display();
//    try { Thread.sleep(75); } catch (Exception ex) {}
    }
    oled.setBuffer(sb.getScreenBuffer());          
    oled.display();
    try { Thread.sleep(1000); } catch (Exception ex) {}
    
    // Bouncing
    for (int _x=0; _x<130; _x++)
    {
      sb.clear();
      double amplitude = 6 * Math.exp((double)(130 - _x) / (13d * 7.5d)); 
    //  System.out.println("X:" + x + ", ampl: " + (amplitude));
      int _y = 32 - (int)(amplitude * Math.abs(Math.cos(Math.toRadians(180 * _x / 10d))));
      sb.plot(_x,   _y);
      sb.plot(_x+1, _y);
      sb.plot(_x+1, _y+1);
      sb.plot(_x,   _y+1);

      oled.setBuffer(sb.getScreenBuffer());          
      oled.display();
//    try { Thread.sleep(75); } catch (Exception ex) {}
    }
    oled.setBuffer(sb.getScreenBuffer());          
    oled.display();
    try { Thread.sleep(1000); } catch (Exception ex) {}
    
//  sb.dumpScreen();
    
    try { Thread.sleep(1000); } catch (Exception ex) {}
    
    sb.clear();
    oled.clear();
    sb.text("Bye-bye!", 36, 20);

    oled.setBuffer(sb.getScreenBuffer());
    oled.display();
    
    oled.shutdown();
  }
}
