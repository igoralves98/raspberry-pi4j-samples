package sevensegdisplay.samples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.Calendar;
import java.util.GregorianCalendar;

import sevensegdisplay.SevenSegment;

public class ClockSample
{
  public static void main(String[] args) throws IOException
  {
    final SevenSegment segment = new SevenSegment(0x70, true);

    System.out.println("Press CTRL+C to exit");
    Runtime.getRuntime().addShutdownHook(new Thread()
                                         {
                                           public void run()
                                           {
                                             try
                                             {
                                               segment.clear();
                                               System.out.println("\nBye");
                                             }
                                             catch (IOException ioe)
                                             {
                                               ioe.printStackTrace();
                                             }
                                           }
                                         });

    // Continually update the time on a 4 char, 7-segment display
    while (true)
    {
      Calendar now = GregorianCalendar.getInstance();
      int hour   = now.get(Calendar.HOUR);
      int minute = now.get(Calendar.MINUTE);
      int second = now.get(Calendar.SECOND);
      // Set hours
      segment.writeDigit(0, (hour / 10));        // Tens
      segment.writeDigit(1, hour % 10);          // Ones
      // Set minutes
      segment.writeDigit(3, (minute / 10));      // Tens
      segment.writeDigit(4, minute % 10);        // Ones
      // Toggle color
      segment.setColon(second % 2 != 0);         // Toggle colon at 1Hz
      // Wait one second
      try { Thread.sleep(1000L); } catch (InterruptedException ie){}
    }
  }
}
