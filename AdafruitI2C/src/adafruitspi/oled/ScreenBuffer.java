package adafruitspi.oled;

import adafruitspi.oled.img.ImgInterface;
import adafruitspi.oled.utils.CharacterMatrixes;

import java.awt.Point;

public class ScreenBuffer
{
  private int w = 128, h = 32;
  private int[] screenBuffer = null;
  private char[][] screenMatrix = null;
  
  public ScreenBuffer(int w, int h)
  {
    super();
    this.w = w;
    this.h = h;
    this.screenBuffer = new int[w * (h / 8)];
    this.screenMatrix = new char[h][w]; // h lines, w columns
  }
  
  public void clear()
  {
    for (int i=0; i<this.h; i++)
    {
      for (int j=0; j<this.w; j++)
        screenMatrix[i][j] = ' ';
    }
    for (int i=0; i<this.screenBuffer.length; i++)
      this.screenBuffer[i] = 0;
  }
  
  /**
   * Generate and return the screenbuffer from the screenmatrix
   * @return the buffer to display on the OLED
   */
  public int[] getScreenBuffer()
  {
    for (int line=0; line<(this.h / 8); line++)
    {
      for (int col=0; col<this.w; col++)
      {
        int bmVal = 0;
        for (int b=0; b<8; b++)
        {
          if (screenMatrix[(line * 8) + b][col] == 'X')
            bmVal |= (1 << b);
        }
  //    System.out.println(lpad(Integer.toHexString(bmVal), "0", 2) + ", " + lpad(Integer.toBinaryString(bmVal), "0", 8));
        this.screenBuffer[(this.w * (line)) + col] = bmVal;        
      }
    }
    return this.screenBuffer;
  }
  
  /**
   * Draw a text on the screenMatrix
   * 
   * @param txt Character String to display
   * @param xPx Bottom left X origin in Pixels (top left is 0,0)
   * @param yPx Bottom left Y origin in Pixels (top left is 0,0)
   */
  public void text(String txt, int xPx, int yPx)
  {
    int xProgress = xPx;
    for (int i=0; i<txt.length(); i++) // For each character of the string to display
    {
      String c = new String(new char[] { txt.charAt(i) });
      if (CharacterMatrixes.characters.containsKey(c))
      {
        String[] matrix = CharacterMatrixes.characters.get(c);
        for (int x=0; x<matrix[0].length(); x++) // Each COLUMN of the matrix
        {
          char[] verticalBitmap = new char[CharacterMatrixes.FONT_SIZE];
          for (int y=0; y<matrix.length; y++) // Each LINE of the matrix
            verticalBitmap[y] = matrix[y].charAt(x);
          // Write in the scren matrix
       // screenMatrix[line][col]
          for (int y=0; y<CharacterMatrixes.FONT_SIZE; y++)
          {
            int l = (y + yPx - (CharacterMatrixes.FONT_SIZE - 1));
            if (l >= 0 && l < this.h && xProgress >= 0 && xProgress < this.w)
              screenMatrix[l][xProgress] = verticalBitmap[y];
          }
          xProgress++;
        }
      }
      else
      {
        System.out.println("Character not found for the OLED [" + c + "]");
      }
    }
    // Display
//    for (int l=0; l<this.h; l++)
//    {
//      System.out.println(new String(screenMatrix[l]));
//    }
  }

  // TODO Shapes (polygon, arc, oval)
  public void plot(int x, int y)
  {
    if (x >= 0 && x < this.w && y >= 0 && y < this.h)
      screenMatrix[y][x] = 'X';
  }
  public void unplot(int x, int y)
  {
    if (x >= 0 && x < this.w && y >= 0 && y < this.h)
      screenMatrix[y][x] = ' ';
  }

  public void line(int fromx, int fromy, int tox, int toy)
  {
    int deltaX = (tox - fromx);
    int deltaY = (toy - fromy);
    if (deltaX == 0 && deltaY == 0)
      return;
    if (deltaX == 0)
    {
      for (int y=Math.min(fromy, toy); y<=Math.max(toy, fromy); y++)
      {
        if (fromx >= 0 && fromx < this.w && y >= 0 && y < this.h)
          screenMatrix[y][fromx] = 'X';
      }
    }
    else if (deltaY == 0)
    {
      for (int x=Math.min(fromx, tox); x<=Math.max(tox, fromx); x++)
      {
        if (x >= 0 && x < this.w && fromy >= 0 && fromy < this.h)
          screenMatrix[fromy][x] = 'X';
      }
    }
    else // if (Math.abs(deltaX) > Math.abs(deltaY))
    {
      if (deltaX < 0)
      {
        int X = fromx;
        int Y = fromy;
        fromx = tox;
        tox = X;
        fromy = toy;
        toy = Y;
        deltaX = (tox - fromx);
        deltaY = (toy - fromy);
      }
      double coeffDir = (double)deltaY / (double)deltaX;
//    if (fromx < tox)
      {
        for (int x=0; x<=deltaX; x++)
        {
          int y = fromy + (int)(Math.round(x * coeffDir));
          int _x = x + fromx;
          if (_x >= 0 && _x < this.w && y >= 0 && y < this.h)
            screenMatrix[y][_x] = 'X';
        }
      }
    }
  }
  
  public void rectangle(int tlX, int tlY, int brX, int brY)
  {
    line(tlX, tlY, tlX, brY); 
    line(tlX, brY, brX, brY); 
    line(brX, brY, brX, tlY); 
    line(brX, tlY, tlX, tlY); 
  }
  
  public void circle(int centerX, int centerY, int radius)
  {
    arc(centerX, centerY, radius, 0, 360);
  }
  
  public void arc(int centerX, int centerY, int radius, int fromDeg, int toDeg)
  {
    Point prevPt = null;
    for (int i=fromDeg; i<=toDeg; i++)
    {
      int x = centerX + (int)Math.round(radius * Math.sin(Math.toRadians(i)));
      int y = centerY + (int)Math.round(radius * Math.cos(Math.toRadians(i)));
      Point pt = new Point(x, y);
      if (x >= 0 && x < this.w && y >= 0 && y < this.h)
      {
        screenMatrix[y][x] = 'X';
        prevPt = pt;
      } 
      else
        prevPt = null;
      
    }
  }
  
  public void image(ImgInterface img, int topLeftX, int topLeftY)
  {
    int w = img.getW();
    int h = img.getH(); // Assume h % 8 = 0
    int[] imgBuf = img.getImgBuffer();
    for (int col=0; col<w; col++)
    {
      for (int row=0; row<(h / 8); row++)
      {
        String bitMapCol = lpad(Integer.toBinaryString(imgBuf[col + (w * row)]), "0", 8).replace('0', ' ').replace('1', 'X');
        // Write in the scren matrix
        // screenMatrix[line][col]
        for (int y=0; y<8; y++)
        {
          int l = (topLeftY + (7 - y) + (row * 8));
          if (l >= 0 && l < this.h && (col + topLeftX) >= 0 && (col +topLeftX) < this.w)
            screenMatrix[l][(col + topLeftX)] = bitMapCol.charAt(y);
        }      
      }
    }
  }
  
  private static String lpad(String str, String with, int len)
  {
    String s = str;
    while (s.length() < len)
      s = with + s;
    return s;
  }
}
