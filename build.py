#!/usr/bin/env python3
"""Build the dependency-free Android app with a local SDK and JDK.
Debug builds use a disposable local key; release passwords are read from env.
"""
from pathlib import Path
import argparse,json,os,re,shutil,subprocess,sys,zipfile

def main():
 parser=argparse.ArgumentParser();parser.add_argument('--release',action='store_true');args=parser.parse_args()
 root=Path(__file__).resolve().parent;config=json.loads((root/'project.json').read_text())
 env=dict(os.environ)
 sdk=Path(env.get('ANDROID_SDK_ROOT') or env.get('ANDROID_HOME') or str(Path.home()/'Library/Android/sdk'))
 if not sdk.is_dir():raise SystemExit('Set ANDROID_SDK_ROOT to an installed Android SDK.')
 def version(path):return tuple(int(x) for x in re.findall(r'\d+',path.name))
 choices=sorted((sdk/'build-tools').glob('*'),key=version)
 if not choices:raise SystemExit('Install Android SDK build-tools (tested with 36.0.0).')
 bt=sdk/'build-tools'/env['ANDROID_BUILD_TOOLS_VERSION'] if env.get('ANDROID_BUILD_TOOLS_VERSION') else choices[-1]
 platform=env.get('ANDROID_PLATFORM','android-36');android_jar=sdk/'platforms'/platform/'android.jar'
 if not android_jar.exists():raise SystemExit('Install SDK platform '+platform)
 java_home=Path(env['JAVA_HOME']) if env.get('JAVA_HOME') else None
 def java_tool(name):
  tool=str(java_home/'bin'/name) if java_home else shutil.which(name)
  if not tool:raise SystemExit('Install JDK 17+ and set JAVA_HOME.')
  return tool
 if java_home:env['PATH']=str(java_home/'bin')+os.pathsep+env.get('PATH','')
 def run(*items):subprocess.run([str(x) for x in items],cwd=root,env=env,check=True)
 build=root/'build';dist=root/'dist'
 for sub in ['classes','dex']:
  target=build/sub
  if target.exists():shutil.rmtree(target)
  target.mkdir(parents=True)
 dist.mkdir(exist_ok=True)
 run(bt/'aapt','package','-f','-M',config['manifest'],'-S','res','-A','assets','-I',android_jar,'-0','wav','-F',build/'resources.apk')
 run(java_tool('javac'),'-source','8','-target','8','-bootclasspath',str(android_jar)+os.pathsep+str(bt/'core-lambda-stubs.jar'),'-d',build/'classes',*sorted((root/'src').rglob('*.java')))
 run(bt/'d8','--lib',android_jar,'--min-api',str(config['min_api']),'--output',build/'dex',*sorted((build/'classes').rglob('*.class')))
 shutil.copyfile(build/'resources.apk',build/'unsigned.apk')
 with zipfile.ZipFile(build/'unsigned.apk','a') as apk:apk.write(build/'dex/classes.dex','classes.dex')
 run(bt/'zipalign','-f','4',build/'unsigned.apk',build/'aligned.apk')
 if args.release:
  required=['SIGNING_KEYSTORE','SIGNING_STORE_PASSWORD','SIGNING_KEY_ALIAS']
  missing=[key for key in required if not env.get(key)]
  if missing:raise SystemExit('Release signing needs: '+', '.join(missing))
  key=Path(env['SIGNING_KEYSTORE']).expanduser().resolve();alias=env['SIGNING_KEY_ALIAS'];password='env:SIGNING_STORE_PASSWORD'
 else:
  key=build/'debug.keystore';alias='androiddebugkey';password='pass:android'
  if not key.exists():run(java_tool('keytool'),'-genkeypair','-keystore',key,'-storepass','android','-keypass','android','-alias',alias,'-keyalg','RSA','-keysize','2048','-validity','10000','-dname','CN=Android Debug,O=Android,C=US')
 destination=dist/(config['name']+('-release' if args.release else '-debug')+'.apk')
 command=[bt/'apksigner','sign','--ks',key,'--ks-key-alias',alias,'--ks-pass',password]
 if args.release and env.get('SIGNING_KEY_PASSWORD'):command+=['--key-pass','env:SIGNING_KEY_PASSWORD']
 elif not args.release:command+=['--key-pass','pass:android']
 run(*command,'--out',destination,build/'aligned.apk');run(bt/'apksigner','verify',destination)
 print('Built and signature-verified:',destination)
if __name__=='__main__':main()
