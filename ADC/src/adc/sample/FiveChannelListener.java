package adc.sample;

import adc.ADCContext;
import adc.ADCListener;
import adc.ADCObserver;

import adc.utils.EscapeSeq;

import org.fusesource.jansi.AnsiConsole;

public class FiveChannelListener
{
  private final static boolean DEBUG = false;
  private final static String STR100 = "                                                                                                    ";
  
  private final static int DIGITAL_OPTION = 0;
  private final static int ANALOG_OPTION  = 1;
  
  private static int displayOption = ANALOG_OPTION;
  
  private ADCObserver.MCP3008_input_channels channel[] = null;
  
  public FiveChannelListener() throws Exception
  {
    channel = new ADCObserver.MCP3008_input_channels[] 
    {
      ADCObserver.MCP3008_input_channels.CH0,
      ADCObserver.MCP3008_input_channels.CH1,
      ADCObserver.MCP3008_input_channels.CH2,  
      ADCObserver.MCP3008_input_channels.CH3,  
      ADCObserver.MCP3008_input_channels.CH4  
    };
    final ADCObserver obs = new ADCObserver(channel); // Note: We could instantiate more than one observer (on several channels).
    
    final String[] channelColors = new String[] { EscapeSeq.ANSI_RED, EscapeSeq.ANSI_WHITE, EscapeSeq.ANSI_YELLOW, EscapeSeq.ANSI_GREEN, EscapeSeq.ANSI_BLUE };
    
    ADCContext.getInstance().addListener(new ADCListener()
       {
         @Override
         public void valueUpdated(ADCObserver.MCP3008_input_channels inputChannel, int newValue) 
         {
//         if (inputChannel.equals(channel))
           {
             int volume = (int)(newValue / 10.23); // [0, 1023] ~ [0x0000, 0x03FF] ~ [0&0, 0&1111111111]
             if (DEBUG)
               System.out.println("readAdc:" + Integer.toString(newValue) + 
                                               " (0x" + lpad(Integer.toString(newValue, 16).toUpperCase(), "0", 2) + 
                                               ", 0&" + lpad(Integer.toString(newValue, 2), "0", 8) + ")"); 
             if (displayOption == DIGITAL_OPTION)
               System.out.println("Ch " + Integer.toString(inputChannel.ch()) + "Volume:" + volume + "% (" + newValue + ")");
             else if (displayOption == ANALOG_OPTION)
             {
               String str = "";
               for (int i=0; i<volume; i++)
                 str += ".";
               int ch = inputChannel.ch();
               str = EscapeSeq.superpose(str, "Ch " + Integer.toString(ch) + ": " + Integer.toString(volume) + "%");
               AnsiConsole.out.println(EscapeSeq.ansiLocate(0, ch) + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT + STR100);
               AnsiConsole.out.println(EscapeSeq.ansiLocate(0, ch) + EscapeSeq.ansiSetTextAndBackgroundColor(EscapeSeq.ANSI_WHITE, channelColors[ch]) + EscapeSeq.ANSI_BOLD + str + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT);               
//             System.out.println(str);
             }
           }
         }
       });
    obs.start();         
    
    Runtime.getRuntime().addShutdownHook(new Thread()
       {
         public void run()
         {
           if (obs != null)
             obs.stop();
         }
       });    
  }
  
  public static void main(String[] args) throws Exception
  {
    if (displayOption == ANALOG_OPTION)
    {
      AnsiConsole.systemInstall();
      AnsiConsole.out.println(EscapeSeq.ANSI_CLS);
    }
    // Channels are hard-coded
    new FiveChannelListener();
  }

  private static String lpad(String str, String with, int len)
  {
    String s = str;
    while (s.length() < len)
      s = with + s;
    return s;
  }
}
