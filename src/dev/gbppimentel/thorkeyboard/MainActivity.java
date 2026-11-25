package dev.gbppimentel.thorkeyboard;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
/** Explicit user-driven setup. Never changes the selected keyboard automatically. */
public final class MainActivity extends Activity {
 LinearLayout content;
 int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
 @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);getWindow().setStatusBarColor(0xff080808);getWindow().setNavigationBarColor(0xff080808);
  ScrollView scroll=new ScrollView(this);content=new LinearLayout(this);content.setOrientation(1);content.setPadding(dp(20),dp(16),dp(20),dp(16));content.setBackgroundColor(0xff080808);scroll.addView(content);setContentView(scroll);
  label("Thor Keyboard",26);label("A touch and controller keyboard for AYN Thor. Split on top, normal on bottom; opens opposite the text field.",16);
  button("1. Enable Thor Keyboard",()->startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)));
  button("2. Choose Keyboard",()->((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showInputMethodPicker());
  button("3. Allow Keyboard Overlay",()->startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName()))));
  button("Optional Accessibility Overlay",()->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
  label("Allow an overlay for cross-screen typing. The optional accessibility host offers an alternative overlay type; it does not read app contents. Without overlay access, the keyboard falls back to the editor screen.",14);
  label("Keyboard Height (180–280 dp; bottom minimum 60%)",16);SeekBar height=new SeekBar(this);height.setMax(100);height.setProgress(getSharedPreferences("tools",0).getInt("keyboard_height",230)-180);content.addView(height);TextView value=label((height.getProgress()+180)+" dp",14);
  height.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar s){}public void onProgressChanged(SeekBar s,int p,boolean user){value.setText((p+180)+" dp");}public void onStopTrackingTouch(SeekBar s){getSharedPreferences("tools",0).edit().putInt("keyboard_height",s.getProgress()+180).apply();}});
  label("Arrows / left stick: move · A: type · B: hide · X: delete · Y: Shift (hold Caps) · L1: symbols · R1: swap · Start: Enter · R3: Space · L3: paste",14);
  EditText preview=new EditText(this);preview.setTextColor(0xffffffff);preview.setHintTextColor(0xffbbbbbb);preview.setHint("Tap to try the selected keyboard");preview.setSingleLine();preview.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);content.addView(preview);
  button("Open Wayfinder",()->{Intent launch=getPackageManager().getLaunchIntentForPackage("app.wayfinder");if(launch!=null)startActivity(launch);else startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://github.com/Thor-Wayfinder/thor-wayfinder")));});
 }
 TextView label(String text,int size){TextView v=new TextView(this);v.setText(text);v.setTextColor(0xffeeeeee);v.setTextSize(size);v.setPadding(0,dp(7),0,dp(7));content.addView(v);return v;}
 void button(String title,Runnable action){Button b=new Button(this);b.setText(title);b.setAllCaps(false);b.setTextColor(0xffffffff);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xff303030));b.setOnClickListener(v->action.run());content.addView(b);}
}
