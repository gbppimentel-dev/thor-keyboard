package dev.gbppimentel.thorkeyboard;
import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
/** Optional display-overlay host; does not inspect app contents or manage focus. */
public final class KeyboardOverlayService extends AccessibilityService {
 static KeyboardOverlayService instance;
 @Override protected void onServiceConnected(){instance=this;}
 @Override public void onAccessibilityEvent(AccessibilityEvent event){}
 @Override public void onInterrupt(){}
 @Override public void onDestroy(){if(instance==this)instance=null;super.onDestroy();}
}
