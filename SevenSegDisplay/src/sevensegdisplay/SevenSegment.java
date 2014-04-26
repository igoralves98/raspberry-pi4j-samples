package sevensegdisplay;

import java.io.IOException;

public class SevenSegment
{
  private LEDBackPack display = null;
  private final int[] digits = { 0x3F, 0x06, 0x5B, 0x4F, 0x66, 0x6D, 0x7D, 0x07, 0x7F, 0x6F, // 0..9
                                 0x77, 0x7C, 0x39, 0x5E, 0x79, 0x71 };                       // A..F

  public SevenSegment()
  {
    display = new LEDBackPack(0x70);
  }
  public SevenSegment(int addr)
  {
    display = new LEDBackPack(addr, false);
  }
  public SevenSegment(int addr, boolean b)
  {
    display = new LEDBackPack(addr, b);
  }

  /*
   * Sets a digit using the raw 16-bit value
   */
  public void writeDigitRaw(int charNumber, int value) throws IOException
  {
    if (charNumber > 7)
      return;
    // Set the appropriate digit
    this.display.setBufferRow(charNumber, value);
  }

  /*
   * Sets a single decimal or hexademical value (0..9 and A..F)
   */
  public void writeDigit(int charNumber, int value) throws IOException 
  {
    writeDigit(charNumber, value, false);
  }
  public void writeDigit(int charNumber, int value, boolean dot) throws IOException 
  {
    if (charNumber > 7)
      return;
    if (value > 0xF)
      return;
    // Set the appropriate digit
    this.display.setBufferRow(charNumber, digits[value] | (dot?0x1 << 7:0x0));
  }
  
  /*
   * Enables or disables the colon character
   */
  public void setColon() throws IOException
  {
    setColon(true);
  }
  public void setColon(boolean state) throws IOException
  {
    // Warning: This function assumes that the colon is character '2',
    // which is the case on 4 char displays, but may need to be modified
    // if another display type is used
    if (state)
      this.display.setBufferRow(2, 0xFFFF);
    else
      this.display.setBufferRow(2, 0);
  }
  
  public void clear() throws IOException
  {
    this.display.clear();
  }
}
