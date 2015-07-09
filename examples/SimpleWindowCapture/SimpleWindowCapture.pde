/*
 * This example captures the window of Internet Explorer.
 * Please launch IE before run this code.
 */

import net.kougaku_navi.WindowCapture.*;

WindowCapture wincap;

void setup() {
  size(500, 300);
  
  String window_class = "IEFrame"; // window class of Internet Explorer11
  String window_title = null;      // null means not specify the title.  
 
  wincap = new WindowCapture( window_class, window_title );
}

void draw() {
  background(50);
  
  PImage img = wincap.getImage();    
  if ( img!= null ) {
    image(img, 10, 10);
  }
}

