package adc.sample;

import adc.ADCContext;
import adc.ADCListener;
import adc.ADCObserver;

public class SampleMain
{
  private final static boolean DEBUG = false;
  private ADCObserver.MCP3008_input_channels channel = null;
  
  public SampleMain(int ch) throws Exception
  {
    channel = findChannel(ch);
    final ADCObserver obs = new ADCObserver(channel); // Note: We could instantiate more than one observer (on several channels).
    ADCContext.getInstance().addListener(new ADCListener()
       {
         @Override
         public void valueUpdated(ADCObserver.MCP3008_input_channels inputChannel, int newValue) 
         {
           if (inputChannel.equals(channel))
           {
             int volume = (int)(newValue / 10.23); // [0, 1023] ~ [0x0000, 0x03FF] ~ [0&0, 0&1111111111]
             if (DEBUG)
               System.out.println("readAdc:" + Integer.toString(newValue) + 
                                               " (0x" + lpad(Integer.toString(newValue, 16).toUpperCase(), "0", 2) + 
                                               ", 0&" + lpad(Integer.toString(newValue, 2), "0", 8) + ")");        
             System.out.println("Volume:" + volume + "% (" + newValue + ")");
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
    int channel = 0;
    if (args.length > 0)
      channel = Integer.parseInt(args[0]);
    new SampleMain(channel);
  }

  private static ADCObserver.MCP3008_input_channels findChannel(int ch) throws IllegalArgumentException
  {
    ADCObserver.MCP3008_input_channels channel = null;
    switch (ch)
    {
      case 0:
        channel = ADCObserver.MCP3008_input_channels.CH0;
        break;
      case 1:
        channel = ADCObserver.MCP3008_input_channels.CH1;
        break;
      case 2:
        channel = ADCObserver.MCP3008_input_channels.CH2;
        break;
      case 3:
        channel = ADCObserver.MCP3008_input_channels.CH3;
        break;
      case 4:
        channel = ADCObserver.MCP3008_input_channels.CH4;
        break;
      case 5:
        channel = ADCObserver.MCP3008_input_channels.CH5;
        break;
      case 6:
        channel = ADCObserver.MCP3008_input_channels.CH6;
        break;
      case 7:
        channel = ADCObserver.MCP3008_input_channels.CH7;
        break;
      default:
        throw new IllegalArgumentException("No channel " + Integer.toString(ch));
    }
    return channel;
  }
  
  private static String lpad(String str, String with, int len)
  {
    String s = str;
    while (s.length() < len)
      s = with + s;
    return s;
  }
}
