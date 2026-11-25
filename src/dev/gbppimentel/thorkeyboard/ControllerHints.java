package dev.gbppimentel.thorkeyboard;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ReplacementSpan;

/** Vector button badges stay crisp on either Thor display and follow text sizing. */
final class ControllerHints {
 static CharSequence format(String source){
  SpannableStringBuilder result=new SpannableStringBuilder();
  int at=0;
  while(at<source.length()){
   int open=source.indexOf('[',at);
   if(open<0){result.append(source.substring(at));break;}
   result.append(source.substring(at,open));int close=source.indexOf(']',open);
   if(close<0){result.append(source.substring(open));break;}
   int start=result.length();String label=source.substring(open+1,close);
   result.append(label);result.setSpan(new Badge(label),start,result.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);at=close+1;
   if(at<source.length()&&source.charAt(at)==' '){while(at<source.length()&&source.charAt(at)==' ')at++;if(at<source.length()){int gap=result.length();result.append("\uFFFC");result.setSpan(new Gap(),gap,gap+1,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);}}
  }
  return result;
 }
 static final class Gap extends ReplacementSpan {
  public int getSize(Paint p,CharSequence text,int start,int end,Paint.FontMetricsInt fm){return (int)Math.ceil(p.getTextSize()*.8f);}
  public void draw(Canvas c,CharSequence text,int start,int end,float x,int top,int y,int bottom,Paint p){}
 }
 static final class SelectionBadge extends android.graphics.drawable.Drawable {
  final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
  final Badge badge=new Badge("A");
  SelectionBadge(android.content.Context c){paint.setTextSize(7*c.getResources().getDisplayMetrics().density);}
  public void draw(Canvas canvas){float size=paint.getTextSize();badge.draw(canvas,"",0,0,getBounds().right-badge.width(paint)-size*.45f,0,(int)(getBounds().top+size*1.65f),getBounds().bottom,paint);}
  public void setAlpha(int alpha){}
  public void setColorFilter(android.graphics.ColorFilter filter){}
  public int getOpacity(){return android.graphics.PixelFormat.TRANSLUCENT;}
 }
 static final class Badge extends ReplacementSpan {
  final String label;
  Badge(String label){this.label=label;}
  boolean round(){return label.length()==1||label.equals("L3")||label.equals("R3")||label.equals("LS");}
  int color(){switch(label){case "X":return 0xff246cc0;case "Y":return 0xff23804a;case "A":return 0xffc43846;case "B":return 0xffa67c00;case "L1":case "R1":return 0xff505050;case "L2":case "R2":return 0xff6855a1;default:return 0xff454545;}}
  float width(Paint p){if(label.equals("D+"))return p.getTextSize()*1.1f;if(label.equals("NAV_SLASH"))return p.getTextSize()*1.4f;if(label.equals("BACKSPACE"))return p.getTextSize()*2.3f;if(label.startsWith("SHIFT_")||label.equals("CAPS"))return p.getTextSize()*2f;return round()?p.getTextSize()*1.65f:Math.max(p.getTextSize()*2,p.measureText(label)*.84f+p.getTextSize()*.85f);}
  public int getSize(Paint p,CharSequence text,int start,int end,Paint.FontMetricsInt fm){
   if(fm!=null){p.getFontMetricsInt(fm);float center=(p.ascent()+p.descent())/2;fm.ascent=(int)Math.floor(center-p.getTextSize()*.88f);fm.descent=(int)Math.ceil(center+p.getTextSize()*.88f);fm.top=fm.ascent;fm.bottom=fm.descent;}
   return (int)Math.ceil(width(p));
  }
  public void draw(Canvas c,CharSequence text,int start,int end,float x,int top,int y,int bottom,Paint original){
   Paint p=new Paint(original);p.setAntiAlias(true);float size=p.getTextSize(),h=size*1.65f,w=width(p),cy=y+(p.ascent()+p.descent())/2;
   if(label.equals("NAV_SLASH")){p.setColor(0xffbbbbbb);p.setTextAlign(Paint.Align.CENTER);c.drawText("/",x+w/2,cy-(p.ascent()+p.descent())/2,p);return;}
   if(label.equals("BACKSPACE")){
    // Clean eraser silhouette: distinct from navigation arrows and the X badge.
    p.setColor(0xffffffff);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(size*.115f);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);
    android.graphics.Path eraser=new android.graphics.Path();
    eraser.moveTo(x+size*.15f,cy+size*.10f);
    eraser.lineTo(x+size*1.18f,cy-size*.78f);
    eraser.quadTo(x+size*1.28f,cy-size*.86f,x+size*1.38f,cy-size*.76f);
    eraser.lineTo(x+size*2.10f,cy-size*.04f);
    eraser.quadTo(x+size*2.20f,cy+size*.06f,x+size*2.09f,cy+size*.16f);
    eraser.lineTo(x+size*1.42f,cy+size*.75f);
    eraser.lineTo(x+size*.80f,cy+size*.75f);
    eraser.close();c.drawPath(eraser,p);
    c.drawLine(x+size*.67f,cy-size*.34f,x+size*1.62f,cy+size*.57f,p);return;
   }
   if(label.startsWith("SHIFT_")||label.equals("CAPS")){
    boolean active=!label.equals("SHIFT_OFF");p.setColor(active?0xffffffff:0xffcccccc);
    float left=x+size*.16f,right=x+w-size*.16f,tip=cy-size*.8f,shoulder=cy-size*.10f,base=cy+size*.55f;
    android.graphics.Path arrow=new android.graphics.Path();arrow.moveTo(x+w/2,tip);arrow.lineTo(right,shoulder);arrow.lineTo(x+w*.68f,shoulder);arrow.lineTo(x+w*.68f,base);arrow.lineTo(x+w*.32f,base);arrow.lineTo(x+w*.32f,shoulder);arrow.lineTo(left,shoulder);arrow.close();
    p.setStyle(active?Paint.Style.FILL:Paint.Style.STROKE);p.setStrokeWidth(size*.12f);p.setStrokeJoin(Paint.Join.ROUND);c.drawPath(arrow,p);
    if(label.equals("CAPS")){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(size*.14f);c.drawLine(x+w*.28f,cy+size*.82f,x+w*.72f,cy+size*.82f,p);}return;
   }
   if(label.equals("D+")){
    p.setColor(0xffffffff);p.setStyle(Paint.Style.FILL);float unit=size*.22f;
    c.drawRoundRect(new RectF(x+w/2-unit,cy-unit*2.5f,x+w/2+unit,cy+unit*2.5f),unit*.25f,unit*.25f,p);
    c.drawRoundRect(new RectF(x+w/2-unit*2.5f,cy-unit,x+w/2+unit*2.5f,cy+unit),unit*.25f,unit*.25f,p);return;
   }
   float stroke=size*.11f,inset=stroke/2;RectF box=new RectF(x+inset,cy-h/2+inset,x+w-inset,cy+h/2-inset);
   float radius=round()?h/2:size*.5f;
   p.setColor(color());p.setStyle(Paint.Style.FILL);c.drawRoundRect(box,radius,radius,p);
   p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(stroke);p.setColor(0xffffffff);c.drawRoundRect(box,radius,radius,p);

   p.setStyle(Paint.Style.FILL);p.setColor(0xffffffff);p.setTypeface(Typeface.create(Typeface.SANS_SERIF,Typeface.BOLD));p.setTextSize(size*(label.length()>2?.66f:.84f));p.setTextAlign(Paint.Align.CENTER);
   c.drawText(label,x+w/2,cy-(p.ascent()+p.descent())/2,p);
  }
 }
}
