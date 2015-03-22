# Disc Images #

### [OlivSoft 2014-06-01](http://www.lediouris.net/RaspberryPI/Disc.Images/fresh.oliv.soft.2014.06.01.img.zip) ###

Based on the latest (to date) NOOBS, contains a fully operational OS, with various utilities, like ftp server, dhcp server,
NodeJS, WiringPI, PI4J, tighvncserver, and more.

It starts in Ad-Hoc network mode. This can be easily changed afterwards. Type `sudo ./switch` at the prompt.

You just plug in the SD card, turn the power on, and - even without a screen or a keyboard - you will see (from other machines) an ad-hoc network named "`RPiOlivOne`". You should not be prompted for a password to join the network, but in case you are it is "`merde`".

The network can take a while (5 minutes?...) to be available, but it shows up.

Log in with ssh (from another machine) as `pi/pi`. The address is `192.168.1.1`.

You can start vncserver (by typing "`vncserver`" at the prompt), and log in using a VNC client, `192.168.1.1:1/welcome` .

---

  * To change the name of the ad-hoc network, edit the file named `/etc/network/interfaces.ad-hoc`

---

You can also get it from a bash shell:
```
 Prompt> wget http://www.lediouris.net/RaspberryPI/Disc.Images/fresh.oliv.soft.2014.06.01.img.zip
```