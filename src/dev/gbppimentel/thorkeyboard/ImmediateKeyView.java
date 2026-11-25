package dev.gbppimentel.thorkeyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.TextView;

/** Keyboard-specific gestures: synchronous feedback, release-to-type, no framework tap delay. */
final class ImmediateKeyView extends TextView {
 final String label;
 final Runnable touchBegin;
 final boolean repeating;
 int pointer=-1;
 boolean tracking=false,longHandled=false;
 View previewHost;
 Drawable preview;
 final Runnable repeatTap=new Runnable(){public void run(){
  if(!tracking||!isAttachedToWindow())return;
  performClick();if(tracking)postDelayed(this,65);
 }};
 final Runnable hold=new Runnable(){public void run(){
  if(tracking&&isLongClickable())longHandled=performLongClick();
 }};
 ImmediateKeyView(Context context,String label,boolean repeating,Runnable touchBegin){
  super(context);this.label=label;this.repeating=repeating;this.touchBegin=touchBegin;
 }
 boolean inside(MotionEvent event,int index){return index>=0&&event.getX(index)>=0&&event.getY(index)>=0&&event.getX(index)<getWidth()&&event.getY(index)<getHeight();}
 void finishTouch(){
  tracking=false;pointer=-1;removeCallbacks(hold);removeCallbacks(repeatTap);setPressed(false);
  if(previewHost!=null&&preview!=null)previewHost.getOverlay().remove(preview);
  previewHost=null;preview=null;
 }
 @Override public boolean onTouchEvent(MotionEvent event){
  if(!isEnabled())return false;
  int action=event.getActionMasked();
  if(action==MotionEvent.ACTION_DOWN){
   finishTouch();longHandled=false;pointer=event.getPointerId(0);tracking=true;
   if(getParent()!=null)getParent().requestDisallowInterceptTouchEvent(true);
   touchBegin.run();setPressed(true);showPreview();
   if(repeating){performClick();if(tracking&&isAttachedToWindow())postDelayed(repeatTap,360);}
   else if(isLongClickable())postDelayed(hold,ViewConfiguration.getLongPressTimeout());
   return true;
  }
  if(action==MotionEvent.ACTION_MOVE){if(tracking&&!inside(event,event.findPointerIndex(pointer)))finishTouch();return true;}
  if(action==MotionEvent.ACTION_CANCEL){finishTouch();return true;}
  if(action==MotionEvent.ACTION_UP||action==MotionEvent.ACTION_POINTER_UP&&event.getPointerId(event.getActionIndex())==pointer){
   boolean click=tracking&&!longHandled&&!repeating&&inside(event,event.findPointerIndex(pointer));
   finishTouch();if(click)performClick();return true;
  }
  return true;
 }
 void showPreview(){
  // Draw inside the keyboard's existing window: no popup-window permissions or extra focus.
  if(label.codePointCount(0,label.length())!=1||label.equals("⌫"))return;
  View host=getRootView();if(host.getWidth()==0||host.getHeight()==0)return;
  float density=getResources().getDisplayMetrics().density;
  int width=Math.min(Math.max(getWidth(),Math.round(52*density)),Math.round(88*density));
  int height=Math.round(64*density),gap=Math.round(5*density);
  int[] keyAt=new int[2],hostAt=new int[2];getLocationOnScreen(keyAt);host.getLocationOnScreen(hostAt);
  int left=Math.max(0,Math.min(host.getWidth()-width,keyAt[0]-hostAt[0]+(getWidth()-width)/2));
  int top=Math.max(0,Math.min(host.getHeight()-height,keyAt[1]-hostAt[1]-height-gap));
  preview=new Preview(label,density,getResources().getDisplayMetrics().scaledDensity);
  preview.setBounds(left,top,left+width,top+height);previewHost=host;host.getOverlay().add(preview);
 }
 @Override protected void onDetachedFromWindow(){finishTouch();super.onDetachedFromWindow();}
 @Override protected void onWindowVisibilityChanged(int visibility){super.onWindowVisibilityChanged(visibility);if(visibility!=View.VISIBLE)finishTouch();}
 static final class Preview extends Drawable {
  final String letter;final float density,scaledDensity;final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
  Preview(String letter,float density,float scaledDensity){this.letter=letter;this.density=density;this.scaledDensity=scaledDensity;}
  @Override public void draw(Canvas canvas){
   Rect b=getBounds();float tail=7*density;RectF body=new RectF(b.left,b.top,b.right,b.bottom-tail);
   paint.setColor(0xff676767);paint.setStyle(Paint.Style.FILL);canvas.drawRoundRect(body,13*density,13*density,paint);
   Path point=new Path();point.moveTo(body.centerX()-tail,body.bottom-1);point.lineTo(body.centerX(),b.bottom);point.lineTo(body.centerX()+tail,body.bottom-1);point.close();canvas.drawPath(point,paint);
   paint.setColor(0xffffffff);paint.setTextSize(32*scaledDensity);paint.setTextAlign(Paint.Align.CENTER);
   canvas.drawText(letter,body.centerX(),body.centerY()-(paint.ascent()+paint.descent())/2,paint);
  }
  @Override public void setAlpha(int alpha){paint.setAlpha(alpha);invalidateSelf();}
  @Override public void setColorFilter(ColorFilter filter){paint.setColorFilter(filter);invalidateSelf();}
  @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
 }
}
