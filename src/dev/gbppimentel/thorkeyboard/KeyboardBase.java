package dev.gbppimentel.thorkeyboard;
import android.inputmethodservice.InputMethodService;import android.view.inputmethod.*;import android.content.*;import android.content.res.*;import android.hardware.display.DisplayManager;import android.os.*;import android.view.*;import android.widget.*;import android.graphics.PixelFormat;import android.graphics.Color;import android.graphics.drawable.GradientDrawable;
public class KeyboardBase extends InputMethodService {
 WindowManager overlayManager;View overlay;LinearLayout panel;boolean shift=false,symbols=false;Handler handler=new Handler();Runnable repeat;Context keyboardContext;View visibleKeyboard;boolean transitioning=false;int transitionToken=0;final java.util.ArrayList<View> retiring=new java.util.ArrayList<>();final java.util.HashMap<View,WindowManager> retiringManagers=new java.util.HashMap<>();
 int dp(Context c,float v){return Math.round(v*c.getResources().getDisplayMetrics().density);}
 public boolean onEvaluateFullscreenMode(){return false;}
 public boolean isFullscreenMode(){return false;}
 public void updateFullscreenMode(){setExtractViewShown(false);}
 public void onConfigureWindow(Window window,boolean fullscreen,boolean candidatesOnly){super.onConfigureWindow(window,false,candidatesOnly);window.setLayout(-1,-2);}
 public Resources getResources(){Resources r=super.getResources();Configuration c=new Configuration(r.getConfiguration());if(c.orientation!=Configuration.ORIENTATION_LANDSCAPE)return r;c.orientation=Configuration.ORIENTATION_PORTRAIT;return createConfigurationContext(c).getResources();}
 public View onCreateInputView(){return makePanel(this);}
 public void onStartInputView(EditorInfo info,boolean restarting){super.onStartInputView(info,restarting);handler.removeCallbacksAndMessages(null);hideOverlay();shift=false;symbols=false;handler.postDelayed(()->{
  Display current=getWindow().getWindow().getDecorView().getDisplay();
  int source=current==null?0:current.getDisplayId();
  DisplayManager dm=(DisplayManager)getSystemService(DISPLAY_SERVICE);
  int target=source==0&&dm.getDisplay(4)!=null?4:0;
  showKeyboardOnDisplay(source,target);
 },100);}
 // Keep the real IME attached to the editor; render touch controls on the other display.
 void keyboardDisplayChanged(int source,int target){}
 void showKeyboardOnDisplay(int source,int targetId){
  hideOverlay();
  Display target=((DisplayManager)getSystemService(DISPLAY_SERVICE)).getDisplay(targetId);
  if(target==null){
   setInputView(makePanel(this));getWindow().getWindow().setLayout(-1,-2);animateIn(visibleKeyboard);
   keyboardDisplayChanged(source,source);return;
  }
  try{
   Context host=KeyboardOverlayService.instance==null?this:KeyboardOverlayService.instance;
   Context c=host.createDisplayContext(target);
   overlayManager=(WindowManager)c.getSystemService(WINDOW_SERVICE);
   overlay=makePanel(c);
   int type=KeyboardOverlayService.instance==null?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
   WindowManager.LayoutParams p=new WindowManager.LayoutParams(-1,heightPixels(c),type,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
   p.setFitInsetsTypes(0);p.gravity=Gravity.BOTTOM;p.setTitle("Compact Keyboard");overlayManager.addView(overlay,p);
   TextView strip=new TextView(this);strip.setText(targetId==0?"typing on the top screen":"typing on the bottom screen");
   strip.setTextColor(0xffbbbbbb);strip.setBackgroundColor(0xff080808);strip.setGravity(Gravity.CENTER);
   strip.setLayoutParams(new ViewGroup.LayoutParams(-1,dp(this,26)));setInputView(strip);
   getWindow().getWindow().setLayout(-1,-2);keyboardDisplayChanged(source,targetId);animateIn(visibleKeyboard);
  }catch(Exception e){
   android.util.Log.w("CompactKeyboard","Cannot show keyboard on display "+targetId,e);
   hideOverlay();setInputView(makePanel(this));getWindow().getWindow().setLayout(-1,-2);animateIn(visibleKeyboard);
   keyboardDisplayChanged(source,source);
  }
 }

 int heightPixels(Context c){
  WindowManager wm=(WindowManager)c.getSystemService(WINDOW_SERVICE);Display display=wm.getDefaultDisplay();
  if(c==this&&getWindow()!=null&&getWindow().getWindow().getDecorView().getDisplay()!=null)display=getWindow().getWindow().getDecorView().getDisplay();
  android.util.DisplayMetrics metrics=new android.util.DisplayMetrics();display.getRealMetrics(metrics);
  int desired=dp(c,height());if(display.getDisplayId()==4)desired=Math.max(desired,Math.round(metrics.heightPixels*.60f));
  return Math.min(desired,Math.round(metrics.heightPixels*.88f));
 }
 void animateIn(View view){if(view==null)return;view.animate().cancel();view.setPivotX(view.getWidth()/2f);view.setPivotY(view.getHeight());view.setAlpha(0f);view.setScaleX(.97f);view.setScaleY(.94f);view.setTranslationY(dp(view.getContext(),24));view.animate().alpha(1f).scaleX(1f).scaleY(1f).translationY(0).setDuration(210).setInterpolator(new android.view.animation.DecelerateInterpolator()).start();}
 void animateAway(Runnable after){
  if(transitioning)return;View view=visibleKeyboard;if(view==null){after.run();return;}
  transitioning=true;int token=++transitionToken;view.animate().cancel();view.setPivotX(view.getWidth()/2f);view.setPivotY(view.getHeight());
  view.animate().alpha(0f).scaleX(.97f).scaleY(.94f).translationY(dp(view.getContext(),28)).setDuration(150).setInterpolator(new android.view.animation.AccelerateInterpolator()).withEndAction(()->{if(token!=transitionToken)return;transitioning=false;after.run();}).start();
 }
 void dismissKeyboard(){animateAway(()->requestHideSelf(0));}
 int height(){return getSharedPreferences("tools",0).getInt("keyboard_height",230);}
 android.graphics.drawable.Drawable keyBackground(Context c,boolean selected){
  // Touch state has no entrance/exit animation or ripple tail.
  android.graphics.drawable.StateListDrawable surface=new android.graphics.drawable.StateListDrawable();
  surface.addState(new int[]{android.R.attr.state_pressed},background(0xff707070,dp(c,10)));
  surface.addState(new int[]{android.R.attr.state_selected},background(0xff484848,dp(c,10)));
  surface.addState(new int[]{},background(selected?0xff484848:0xff242424,dp(c,10)));
  surface.setEnterFadeDuration(0);surface.setExitFadeDuration(0);
  return surface;
 }

 GradientDrawable background(int color,int radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(radius);return g;}
 View makePanel(Context c){keyboardContext=c;panel=new LinearLayout(c);panel.setOrientation(1);panel.setPadding(dp(c,5),dp(c,4),dp(c,5),dp(c,5));panel.setBackgroundColor(0xff080808);panel.setLayoutParams(new android.view.ViewGroup.LayoutParams(-1,heightPixels(c)));visibleKeyboard=panel;fill();return panel;}
 boolean capsLockEnabled(){return false;}
 boolean splitKeyboard(){return false;}
 void fill(){Context c=keyboardContext;panel.removeAllViews();LinearLayout toolbar=new LinearLayout(c);TextView title=new TextView(c);title.setText("Thor Compact");title.setTextColor(0xffbbbbbb);title.setTextSize(12);title.setGravity(Gravity.CENTER_VERTICAL);title.setPadding(dp(c,8),0,0,0);toolbar.addView(title,new LinearLayout.LayoutParams(0,-1,1));toolbar.addView(key(c,"Hide",()->requestHideSelf(0),false),new LinearLayout.LayoutParams(dp(c,65),-1));panel.addView(toolbar,new LinearLayout.LayoutParams(-1,dp(c,27)));
  String[] rows=symbols?new String[]{"1234567890","!@#$%^&*()","-_=+[]{}\\|",";:'\",.<>/?"}:new String[]{"1234567890","qwertyuiop","asdfghjkl","zxcvbnm"};
  if(splitKeyboard()){fillSplitRows(c,rows);return;}
  for(int j=0;j<rows.length;j++){LinearLayout row=new LinearLayout(c);if(j==2&&!symbols)row.setPadding(dp(c,14),0,dp(c,14),0);if(j==3&&!symbols)row.addView(key(c,shift?"SHIFT":"Shift",()->{shift=!shift;fill();},false),params(c,1.4f));
   for(char ch:rows[j].toCharArray()){final String value=String.valueOf((shift||capsLockEnabled())?Character.toUpperCase(ch):ch);row.addView(key(c,value,()->{InputConnection ic=getCurrentInputConnection();if(ic!=null)ic.commitText(value,1);if(shift&&!capsLockEnabled()){shift=false;fill();}},false),params(c,1));}
   if(j==3&&!symbols)row.addView(key(c,"⌫",()->backspace(),true),params(c,1.4f));panel.addView(row,new LinearLayout.LayoutParams(-1,0,1));}
  LinearLayout bottom=new LinearLayout(c);bottom.addView(key(c,symbols?"ABC":"?123",()->{symbols=!symbols;fill();},false),params(c,1.25f));bottom.addView(key(c,",",()->text(","),false),params(c,1));bottom.addView(key(c,"Space",()->text(" "),false),params(c,4));bottom.addView(key(c,".",()->text("."),false),params(c,1));bottom.addView(key(c,symbols?"⌫":"Enter",()->{if(symbols)backspace();else enter();},symbols),params(c,1.8f));panel.addView(bottom,new LinearLayout.LayoutParams(-1,0,1));
 }
 // Equal thumb zones with a shared 20% center gap keep the split aligned across rows.
 LinearLayout splitRow(Context c,LinearLayout left,LinearLayout right){
  LinearLayout row=new LinearLayout(c);
  row.addView(left,new LinearLayout.LayoutParams(0,-1,2));
  View gap=new View(c);gap.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
  row.addView(gap,new LinearLayout.LayoutParams(0,-1,1));
  row.addView(right,new LinearLayout.LayoutParams(0,-1,2));return row;
 }
 void addSplitCharacter(Context c,LinearLayout row,char ch){
  final String value=String.valueOf((shift||capsLockEnabled())?Character.toUpperCase(ch):ch);
  row.addView(key(c,value,()->{InputConnection ic=getCurrentInputConnection();if(ic!=null)ic.commitText(value,1);if(shift&&!capsLockEnabled()){shift=false;fill();}},false),params(c,1));
 }
 void fillSplitRows(Context c,String[] rows){
  for(int j=0;j<rows.length;j++){
   LinearLayout left=new LinearLayout(c),right=new LinearLayout(c);
   if(j==3&&!symbols)left.addView(key(c,shift?"SHIFT":"Shift",()->{shift=!shift;fill();},false),params(c,1.4f));
   int midpoint=j==3&&!symbols?4:5;
   for(int i=0;i<rows[j].length();i++)addSplitCharacter(c,i<midpoint?left:right,rows[j].charAt(i));
   if(j==3&&!symbols)right.addView(key(c,"⌫",()->backspace(),true),params(c,1.4f));
   panel.addView(splitRow(c,left,right),new LinearLayout.LayoutParams(-1,0,1));
  }
  LinearLayout left=new LinearLayout(c),right=new LinearLayout(c);
  left.addView(key(c,symbols?"ABC":"?123",()->{symbols=!symbols;fill();},false),params(c,1.25f));
  left.addView(key(c,",",()->text(","),false),params(c,1));
  left.addView(key(c,"Space",()->text(" "),false),params(c,2.5f));
  right.addView(key(c,"Space",()->text(" "),false),params(c,2.5f));
  right.addView(key(c,".",()->text("."),false),params(c,1));
  right.addView(key(c,symbols?"⌫":"Enter",()->{if(symbols)backspace();else enter();},symbols),params(c,1.8f));
  panel.addView(splitRow(c,left,right),new LinearLayout.LayoutParams(-1,0,1));
 }
 LinearLayout.LayoutParams params(Context c,float weight){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-1,weight);p.setMargins(dp(c,2),dp(c,2),dp(c,2),dp(c,2));return p;}
 void onTouchTyping(){}
 TextView key(Context c,String label,Runnable action,boolean repeating){
  TextView b=new ImmediateKeyView(c,label,repeating,()->onTouchTyping());
  b.setText(label);b.setTextColor(0xfff5f5f5);b.setTextSize(label.length()>2?12:17);
  b.setGravity(Gravity.CENTER);b.setIncludeFontPadding(false);b.setPadding(0,0,0,0);
  b.setBackground(keyBackground(c,false));b.setOnClickListener(v->action.run());return b;
 }

 void text(String s){InputConnection ic=getCurrentInputConnection();if(ic!=null)ic.commitText(s,1);}
 void backspace(){InputConnection ic=getCurrentInputConnection();if(ic!=null)ic.deleteSurroundingText(1,0);}
 void enter(){InputConnection ic=getCurrentInputConnection();if(ic==null)return;EditorInfo e=getCurrentInputEditorInfo();int action=e==null?0:e.imeOptions&EditorInfo.IME_MASK_ACTION;if(action!=EditorInfo.IME_ACTION_NONE&&action!=EditorInfo.IME_ACTION_UNSPECIFIED)ic.performEditorAction(action);else ic.commitText("\n",1);}
 void hideOverlay(){
  ++transitionToken;transitioning=false;
  if(visibleKeyboard!=null){visibleKeyboard.animate().cancel();visibleKeyboard=null;}
  if(overlay!=null){View old=overlay;WindowManager manager=overlayManager;overlay=null;
   retiring.add(old);retiringManagers.put(old,manager);
   old.animate().cancel();old.animate().alpha(0).translationY(dp(old.getContext(),24)).scaleY(.94f).setDuration(140).withEndAction(()->removeRetired(old)).start();
  }
  if(repeat!=null)handler.removeCallbacks(repeat);
 }
 void removeRetired(View view){WindowManager manager=retiringManagers.remove(view);retiring.remove(view);if(manager!=null)try{manager.removeViewImmediate(view);}catch(Exception ignored){}}

 public void onFinishInputView(boolean finishing){handler.removeCallbacksAndMessages(null);hideOverlay();super.onFinishInputView(finishing);}
 public void onDestroy(){handler.removeCallbacksAndMessages(null);hideOverlay();for(View v:new java.util.ArrayList<View>(retiring)){v.animate().cancel();removeRetired(v);}super.onDestroy();}
}
