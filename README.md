
## Zeroth Speech-To-Text library for And

Zeroth was initially developed as part of Atlas’s Conversational AI platform, which enables enterprises to add analysis and intelligence to their conversational data. Visit our homepage for more information.

We now introduce Zeroth Cloud as a hosted service for any developer to incorporate speech-to-text into his or her service*.*

We'd love to hear from you! Please email us at support@goodatlas.com with any questions, suggestions or requests.

## Features
Real time speech-to-text
Change sample rate between 16000 and 44100

## Requirements
* Android 6.0++
* Android Studio 3.0++

## Installation

Gradle

Add the zerothandroid project to your App build.gradle file


```
dependencies {
    implementation project(':zerothandroid')
}
```


and to allow the following permissions in your `manifest`

```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.RECORD_AUDIO"/>
```

## Usage
The entire process for using the Zeroth library is as follows

1.  Zeroth.initialize() in your `application`
2.  request `android.permission.RECORD_AUDIO` Permission
3. 	 request Get Access Token
4.  Setting `ZerothParam` for Audio Option
5.  create Zeroth.createZerothMic Interface 
6.  startListener
7.  stop/shutdown Listener


* add Zeroth.initialize() in your `application`
* input the AppId and AppSecretId assigned by Zeroth Console as parameter <https://zeroth-console.goodatlas.com>

```
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Zeroth.initialize(
                this,
                /* your App Id*/"",
                /* your App Secret Id*/"");
    }
}

```

* Check `android.permission.RECORD_AUDIO` Permission

```
Zeroth.requestActivityPermission(Context);
```


#### <a name="get-access-token">Get-Access-Token</a>
* Get AccessToken for access to Zeroth Cloud 

```
Zeroth.getToken(new OnGetTokenListener() {
            @Override
            public void onGetToken(OAuthToken oAuthToken) {
            		//get Token
            }
             @Override
            public void onFailed(ErrorModel error) {
					//fail..
            }
```
- Response 
 ```
{"access_token":"abcd","token_type":"Bearer","expires_in":600}
 ```
 

| OAuthToken  | type  
|:------------- |:---------------:  
| access_token  | String 
| token_type    | String        
| expired_in 	   | int

* Setting ZerothParam for Audio Option

```
mParam = new ZerothParam();
mParam.accessToken = oAuthToken.access_token;  
mParam.language = ZerothDefine.ZEROTH_LANG_KOR; // or ZerothDefine.ZEROTH_LANG_ENG
mParam.channels = ZerothDefine.ZEROTH_MONO;     // or ZerothDefine.ZEROTH_STEREO
mParam.audioRate = ZerothDefine.ZEROTH_RATE_16;	// or ZerothDefine.ZEROTH_RATE_44
mParam.isFinal = false;

```

* create Zeroth Mic Interface
 * if you get ZerothMic Interface then call startListener()
 * If you want to quit, call shutdown.

```
public interface ZerothMic {

    void startListener();

    void stopListener();

    boolean shutdown();
    
    void setWebSocketListener(OnWebSocketListener l);

}
```

```
mZerothMic = Zeroth.createZerothMic(param, new OnZerothResult() {

                    @Override
                    public void onProgressStatus(ZerothDefine.ZerothStatus status) {
                        mStatus = status; 
                        invalidateOptionsMenu();
                        ExLog.i("ZerothResult","onProgressStatus=" + status.name());
                    }

                    @Override
                    public void onMessage(final Transcript transcript, String lowData) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.add(transcript);
                            }
                        });
                    }

                    @Override
                    public void onFailed(ErrorModel errorModel) {
                        ExLog.i("ZerothResult","fail...=" + GsonManager.toJson(errorModel));
                    }

                });
```

* Response

```
 onMessage{"transcript":"하나","final":false}
```
* Transcript

| OAuthToken  | type  
|:------------- |:---------------:  
| transcript  | String 
| finalText    | Boolean        


* ZerothStatus

	`IDLE`    : State before initialization  
	`INIT`    : If you successfully invoke : the createZerothMic method  
	`RUNNING`	: called startListener() by ZerothMic interface  
	`SHUTDOWN` : called shutdown() by ZerothMic interface  

* if you want to see WebSoketNetwork Data

```
mZerothMic.setWebSocketListener(new OnWebSocketListener() {
                    @Override
                    public void onOpen(WebSocket webSocket, Response response) {
                        
                    }

                    @Override
                    public void onMessage(WebSocket webSocket, String text) {

                    }

                    @Override
                    public void onMessage(WebSocket webSocket, ByteString bytes) {

                    }

                    @Override
                    public void onClosing(WebSocket webSocket, int code, String reason) {

                    }

                    @Override
                    public void onClosed(WebSocket webSocket, int code, String reason) {

                    }

                    @Override
                    public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {

                    }
                });
```

## License
Zeroth for Android is license under the Apache License.

## Version History
0.1.0 First public version

Copyright © 2019 Atlas Guide. All rights reserved.


