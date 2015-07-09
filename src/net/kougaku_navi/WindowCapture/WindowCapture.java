/*
Copyright (c) 2015, Sunao Hashimoto
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, 
  this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, 
  this list of conditions and the following disclaimer in the documentation 
  and/or other materials provided with the distribution.
 * Neither the name of the kougaku-navi nor the names of its contributors 
  may be used to endorse or promote products derived from this software 
  without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/


/* --------------------------------------------
jna-4.1.0.jar and jna-platform-4.1.0.jar are required.
Get them here: https://github.com/twall/jna

References:
http://stackoverflow.com/questions/21962086/java-jna-findwindow-error-looking-up-function-findwindow-the-specified-pr
http://stackoverflow.com/questions/4433994/java-window-image
http://p.booklog.jp/book/14945/page/169190

-------------------------------------------- */

package net.kougaku_navi.WindowCapture;

import java.awt.image.BufferedImage;

import processing.core.PImage;

import com.sun.jna.Memory;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HBITMAP;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.platform.win32.WinGDI.BITMAPINFO;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinUser;


public class WindowCapture {

//	public int width;
//	public int height;
	private int width;
	private int height;
	
	private HBITMAP hBitmap;
	private Memory buffer;
	private HDC hdcWindow;
	private HDC hdcMemDC;
	private BufferedImage image;
	private BITMAPINFO bmi;
	private HWND hWnd;

	public WindowCapture(String window_class, String window_title) {
		hWnd = User32.INSTANCE.FindWindow(window_class, window_title);
		if ( hWnd != null ) {
			User32.INSTANCE.ShowWindow( hWnd, WinUser.SW_RESTORE );		
		}
	}	
	
	public PImage getImage() {
		if ( hWnd == null ) {
			//return new PImage(0,0);	
			return null;
		}
		
		RECT bounds = new RECT();
		User32Extra.INSTANCE.GetClientRect(hWnd, bounds);

		width = bounds.right - bounds.left;
		height = bounds.bottom - bounds.top;
		
		if ( width<=0 || height<=0 ) return null;
		
		bmi = new BITMAPINFO();
		bmi.bmiHeader.biWidth = width;
		bmi.bmiHeader.biHeight = -height;
		bmi.bmiHeader.biPlanes = 1;
		bmi.bmiHeader.biBitCount = 32;
		bmi.bmiHeader.biCompression = WinGDI.BI_RGB;

		hdcWindow = User32.INSTANCE.GetDC(hWnd);
		hdcMemDC = GDI32.INSTANCE.CreateCompatibleDC(hdcWindow);
		hBitmap = GDI32.INSTANCE.CreateCompatibleBitmap(hdcWindow, width, height);

		HANDLE hOld = GDI32.INSTANCE.SelectObject(hdcMemDC, hBitmap);
		GDI32Extra.INSTANCE.BitBlt(hdcMemDC, 0, 0, width, height, hdcWindow, 0, 0, WinGDIExtra.SRCCOPY);
		GDI32.INSTANCE.SelectObject(hdcMemDC, hOld);
		GDI32.INSTANCE.DeleteDC(hdcMemDC);
		
		buffer = new Memory(width * height * 4);
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		GDI32.INSTANCE.GetDIBits(hdcWindow, hBitmap, 0, height, buffer, bmi, WinGDI.DIB_RGB_COLORS);
		image.setRGB(0, 0, width, height, buffer.getIntArray(0, width * height), 0, width);

		GDI32.INSTANCE.DeleteObject(hBitmap);
		User32.INSTANCE.ReleaseDC(hWnd, hdcWindow);		
		
		PImage img = new PImage(image);
		return img;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

}
