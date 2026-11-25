package dev.gbppimentel.thorkeyboard;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.SystemClock;
import android.provider.Settings;
import java.util.HashMap;
import java.util.HashSet;

/** Short local UI cues, on the system-effects stream; never changes device volume. */
final class KeyboardSounds {
 final Context context;
 final SoundPool pool;
 final HashMap<String,Integer> samples=new HashMap<>();
 final HashSet<Integer> ready=new HashSet<>();
 long lastMove=-1000,lastDelete=-1000;
 int serial=0;
 KeyboardSounds(Context context){
  this.context=context;
  pool=new SoundPool.Builder().setMaxStreams(4).setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build();
  pool.setOnLoadCompleteListener((p,id,status)->{if(status==0)ready.add(id);});
  for(String name:new String[]{"move","delete","shift_on","shift_off","caps","space","enter","symbols","swap","hide","paste"}){
   try(AssetFileDescriptor fd=context.getAssets().openFd("keyboard-sounds/"+name+".wav")){samples.put(name,pool.load(fd,1));}
   catch(Exception e){android.util.Log.w("KeyboardSounds","Cannot load "+name,e);}
  }
 }
 void play(String name){
  long now=SystemClock.uptimeMillis();boolean move=name.equals("move");
  if(move&&now-lastMove<85)return;
  if(name.equals("delete")&&now-lastDelete<55)return;
  if(Settings.System.getInt(context.getContentResolver(),Settings.System.SOUND_EFFECTS_ENABLED,1)==0)return;
  Integer id=samples.get(name);if(id==null||!ready.contains(id))return;
  float gain=move?.10f:name.equals("space")?.58f:.46f;
  if(pool.play(id,gain,gain,move?0:1,0,1f)!=0){serial++;if(move)lastMove=now;if(name.equals("delete"))lastDelete=now;}
 }
 void release(){pool.release();ready.clear();samples.clear();}
}
