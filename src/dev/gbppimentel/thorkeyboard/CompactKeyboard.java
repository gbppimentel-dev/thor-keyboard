package dev.gbppimentel.thorkeyboard;
import android.content.*;import android.hardware.display.DisplayManager;import android.os.*;import android.view.*;import android.view.inputmethod.*;import android.widget.*;import android.graphics.PixelFormat;import android.graphics.drawable.GradientDrawable;import java.util.*;

/** Standalone controller-aware keyboard. Extracted from Thor Tools 1.28. */
public class CompactKeyboard extends KeyboardBase {
 KeyboardSounds sounds;boolean activationSoundPlayed=false,controllerMode=true;
 public void onCreate(){super.onCreate();sounds=new KeyboardSounds(this);}
 void cue(String action){if(sounds!=null)sounds.play(action);}
 boolean capsLockEnabled(){return caps;}
 boolean splitKeyboard(){
  // Use the rendering context, not the editor display or the previous swap target.
  Display display=((WindowManager)keyboardContext.getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
  if(keyboardContext==this&&getWindow()!=null&&getWindow().getWindow().getDecorView().getDisplay()!=null)display=getWindow().getWindow().getDecorView().getDisplay();
  return display.getDisplayId()==0;
 }

 void toggleSymbols(){symbols=!symbols;fill();cue("symbols");}
 void backspace(){super.backspace();cue("delete");}
 void enter(){super.enter();cue("enter");}
 void text(String value){super.text(value);if(value.equals(" "))cue("space");}
 void dismissKeyboard(){if(transitioning)return;cue("hide");super.dismissKeyboard();}

 final ArrayList<TextView> keys=new ArrayList<>(); final HashMap<TextView,Runnable> actions=new HashMap<>();
 final HashMap<Integer,TextView> controllerKeys=new HashMap<>();
 final HashSet<Integer> consumed=new HashSet<>(); boolean collecting=true,shown=false,caps=false;
 int selected=10,sourceDisplay=0,currentDisplay=4,stickX=0,stickY=0,cursorX=0;long nextMove=0,nextCursor=0;
 public void onStartInput(EditorInfo info,boolean restarting){super.onStartInput(info,restarting);if(!restarting){caps=false;selected=10;}}
 public void onStartInputView(EditorInfo info,boolean restarting){shown=true;super.onStartInputView(info,restarting);}
 void keyboardDisplayChanged(int source,int target){sourceDisplay=source;currentDisplay=target;}


 void fill(){keys.clear();actions.clear();controllerKeys.clear();collecting=true;super.fill();collecting=false;panel.removeViewAt(0);Context c=keyboardContext;
  boolean split=splitKeyboard();LinearLayout bar=new LinearLayout(c),leftBar=new LinearLayout(c),rightBar=new LinearLayout(c);
  String[] labels={"[L3]  Paste","[R1]  Swap Screen","[B]  Hide"};Runnable[] run={()->paste(),()->swap(),()->dismissKeyboard()};
  for(int i=0;i<labels.length;i++){
   LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-1,1);p.setMargins(dp(c,2),0,dp(c,2),0);
   TextView button=super.key(c,"",run[i],false);button.setSoundEffectsEnabled(false);button.setText(ControllerHints.format(labels[i]));button.setTextColor(0xffffffff);button.setTextSize(11);
   (split?(i<2?leftBar:rightBar):bar).addView(button,p);
   controllerKeys.put(new int[]{KeyEvent.KEYCODE_BUTTON_THUMBL,KeyEvent.KEYCODE_BUTTON_R1,KeyEvent.KEYCODE_BUTTON_B}[i],button);
  }
  (split?rightBar:bar).addView(hintRow(c,"[D+][NAV_SLASH][LS]  Move"),new LinearLayout.LayoutParams(0,-1,1));
  panel.addView(split?splitRow(c,leftBar,rightBar):bar,0,new LinearLayout.LayoutParams(-1,dp(c,29)));

  panel.post(()->paint());
 }
 void paste(){if(getCurrentInputConnection()!=null&&getCurrentInputConnection().performContextMenuAction(android.R.id.paste))cue("paste");}
 TextView hintRow(Context c,String text){TextView v=new TextView(c);v.setText(ControllerHints.format(text));v.setTextColor(0xffffffff);v.setTextSize(10);v.setGravity(Gravity.CENTER);v.setSingleLine();v.setIncludeFontPadding(false);v.setAutoSizeTextTypeUniformWithConfiguration(8,10,1,android.util.TypedValue.COMPLEX_UNIT_SP);return v;}
 void toggleShift(){if(caps){caps=false;shift=false;}else shift=!shift;fill();cue(shift?"shift_on":"shift_off");}
 void toggleCaps(){caps=!caps;shift=caps;fill();cue(caps?"caps":"shift_off");}
 boolean isShift(String label){return label.equals("Shift")||label.equals("SHIFT");}
 TextView key(Context c,String label,Runnable action,boolean repeating){
  final TextView[] holder=new TextView[1];
  Runnable wrapped=()->{int i=keys.indexOf(holder[0]);if(i>=0)selected=i;
   if(isShift(label))toggleShift();
   else if(label.equals("ABC")||label.equals("?123"))toggleSymbols();
   else action.run();paint();};
  TextView v=super.key(c,label,wrapped,repeating);holder[0]=v;
  String face=null;
  if(isShift(label)){face="[Y] ["+(caps?"CAPS":shift?"SHIFT_ON":"SHIFT_OFF")+"]";v.setContentDescription(caps?"Caps Lock on. Tap Y or Shift to turn off; hold to toggle Caps Lock.":shift?"Shift on for next letter. Hold Y or Shift for Caps Lock.":"Shift off. Tap Y or Shift for one uppercase letter; hold for Caps Lock.");}
  else if(label.equals("Space"))face="[R3]  Space";
  else if(label.equals("Enter"))face="[START]  Enter";
  else if(label.equals("⌫"))face="[X]  [BACKSPACE]";
  else if(label.equals("?123")||label.equals("ABC"))face="[L1]  "+label;
  if(isShift(label))controllerKeys.put(KeyEvent.KEYCODE_BUTTON_Y,v);
  else if(label.equals("Space"))controllerKeys.put(KeyEvent.KEYCODE_BUTTON_THUMBR,v);
  else if(label.equals("Enter"))controllerKeys.put(KeyEvent.KEYCODE_BUTTON_START,v);
  else if(label.equals("⌫"))controllerKeys.put(KeyEvent.KEYCODE_BUTTON_X,v);
  else if(label.equals("?123")||label.equals("ABC"))controllerKeys.put(KeyEvent.KEYCODE_BUTTON_L1,v);
  if(face!=null){v.setSoundEffectsEnabled(false);v.setText(ControllerHints.format(face));v.setTextSize(12);v.setSingleLine();v.setAutoSizeTextTypeUniformWithConfiguration(9,12,1,android.util.TypedValue.COMPLEX_UNIT_SP);}
  if(collecting&&!label.equals("Hide")){keys.add(v);actions.put(v,wrapped);}
  if(isShift(label))v.setOnLongClickListener(x->{toggleCaps();return true;});return v;
 }
 void onTouchTyping(){controllerMode=false;paint();}
 void paint(){
  if(keys.isEmpty())return;selected=Math.max(0,Math.min(keys.size()-1,selected));
  for(int i=0;i<keys.size();i++){
   boolean on=controllerMode&&i==selected;TextView key=keys.get(i);
   // Touch presses never leave a controller-selection highlight behind.
   if(key.isSelected()!=on){key.setSelected(on);key.setForeground(on?new ControllerHints.SelectionBadge(keyboardContext):null);}
   else if(on&&key.getForeground()==null)key.setForeground(new ControllerHints.SelectionBadge(keyboardContext));
   key.setTextColor(on?0xffffffff:0xfff5f5f5);
  }
 }


 float[] center(TextView v){int[] at=new int[2];v.getLocationOnScreen(at);return new float[]{at[0]+v.getWidth()/2f,at[1]+v.getHeight()/2f};}
 void move(int dx,int dy){controllerMode=true;if(keys.isEmpty())return;paint();float[] from=center(keys.get(selected));int best=selected;double score=Double.MAX_VALUE;for(int i=0;i<keys.size();i++){float[] to=center(keys.get(i));float x=to[0]-from[0],y=to[1]-from[1];float forward=dx!=0?x*dx:y*dy,side=dx!=0?Math.abs(y):Math.abs(x);if(forward<2)continue;double s=forward+side*(dx!=0?10:3);if(s<score){score=s;best=i;}}if(selected!=best){selected=best;cue("move");}paint();}
 void activate(){controllerMode=true;activationSoundPlayed=false;if(keys.isEmpty())return;paint();Runnable r=actions.get(keys.get(selected));int before=sounds==null?0:sounds.serial;if(r!=null)r.run();activationSoundPlayed=sounds!=null&&sounds.serial!=before;}
 void cursor(int direction){InputConnection ic=getCurrentInputConnection();if(ic!=null){int k=direction<0?KeyEvent.KEYCODE_DPAD_LEFT:KeyEvent.KEYCODE_DPAD_RIGHT;ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,k));ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,k));cue("move");}}
 void swap(){DisplayManager dm=(DisplayManager)getSystemService(DISPLAY_SERVICE);if(dm.getDisplay(4)==null||transitioning)return;cue("swap");int target=currentDisplay==0?4:0;animateAway(()->showKeyboardOnDisplay(sourceDisplay,target));}

 void physicalFeedback(int code){
  TextView target=controllerKeys.get(code);
  if(target==null&&!keys.isEmpty())target=keys.get(Math.max(0,Math.min(selected,keys.size()-1)));
  if(target==null)return;
  if(!activationSoundPlayed)target.playSoundEffect(SoundEffectConstants.CLICK);
  final TextView key=target;key.setPressed(false);key.drawableHotspotChanged(key.getWidth()/2f,key.getHeight()/2f);key.setPressed(true);
  handler.postDelayed(()->key.setPressed(false),130);
 }

 public boolean onKeyDown(int k,KeyEvent e){if(!shown)return super.onKeyDown(k,e);if(transitioning)return true;controllerMode=true;boolean used=true;switch(k){case KeyEvent.KEYCODE_DPAD_LEFT:move(-1,0);break;case KeyEvent.KEYCODE_DPAD_RIGHT:move(1,0);break;case KeyEvent.KEYCODE_DPAD_UP:move(0,-1);break;case KeyEvent.KEYCODE_DPAD_DOWN:move(0,1);break;case KeyEvent.KEYCODE_BUTTON_A:activate();break;case KeyEvent.KEYCODE_BUTTON_B:if(e.getRepeatCount()==0)dismissKeyboard();break;case KeyEvent.KEYCODE_BUTTON_X:backspace();break;case KeyEvent.KEYCODE_BUTTON_Y:if(e.getRepeatCount()==0)toggleShift();else if(e.getRepeatCount()==1){caps=true;shift=true;fill();cue("caps");}break;case KeyEvent.KEYCODE_BUTTON_L1:if(e.getRepeatCount()==0)toggleSymbols();break;case KeyEvent.KEYCODE_BUTTON_R1:if(e.getRepeatCount()==0)swap();break;case KeyEvent.KEYCODE_BUTTON_START:enter();break;case KeyEvent.KEYCODE_BUTTON_THUMBR:text(" ");break;case KeyEvent.KEYCODE_BUTTON_THUMBL:if(e.getRepeatCount()==0)paste();break;default:used=false;}if(used){if(k==KeyEvent.KEYCODE_BUTTON_A)physicalFeedback(k);consumed.add(k);return true;}return super.onKeyDown(k,e);}
 public boolean onKeyUp(int k,KeyEvent e){if(consumed.remove(k))return true;return super.onKeyUp(k,e);}
 int direction(float x){return x<-.55f?-1:x>.55f?1:0;}
 Runnable analogRepeat=new Runnable(){public void run(){long now=SystemClock.uptimeMillis();if((stickX!=0||stickY!=0)&&now>=nextMove){move(stickX,stickY);nextMove=now+130;}if(cursorX!=0&&now>=nextCursor){cursor(cursorX);nextCursor=now+110;}if(shown&&(stickX!=0||stickY!=0||cursorX!=0))handler.postDelayed(this,30);}};
 public boolean onGenericMotionEvent(MotionEvent e){if(shown&&(e.getSource()&InputDevice.SOURCE_JOYSTICK)==InputDevice.SOURCE_JOYSTICK){int x=direction(e.getAxisValue(MotionEvent.AXIS_HAT_X)),y=direction(e.getAxisValue(MotionEvent.AXIS_HAT_Y));if(x==0&&y==0){x=direction(e.getAxisValue(MotionEvent.AXIS_X));y=direction(e.getAxisValue(MotionEvent.AXIS_Y));}if(x!=0&&y!=0){if(Math.abs(e.getAxisValue(MotionEvent.AXIS_X))>Math.abs(e.getAxisValue(MotionEvent.AXIS_Y)))y=0;else x=0;}int cx=direction(e.getAxisValue(MotionEvent.AXIS_Z));if(x!=stickX||y!=stickY){stickX=x;stickY=y;if(x!=0||y!=0){move(x,y);}nextMove=SystemClock.uptimeMillis()+330;}if(cx!=cursorX){cursorX=cx;if(cx!=0)cursor(cx);nextCursor=SystemClock.uptimeMillis()+330;}handler.removeCallbacks(analogRepeat);if(x!=0||y!=0||cx!=0)handler.postDelayed(analogRepeat,30);return true;}return super.onGenericMotionEvent(e);}
 public void onFinishInputView(boolean finishing){shown=false;stickX=stickY=cursorX=0;super.onFinishInputView(finishing);}
 public void onFinishInput(){shown=false;handler.removeCallbacksAndMessages(null);hideOverlay();super.onFinishInput();}
 public void onDestroy(){shown=false;if(sounds!=null){sounds.release();sounds=null;}super.onDestroy();}
}
