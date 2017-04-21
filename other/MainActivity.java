/*
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
*/

package <%PACKAGE_NAME%>;


import android.content.ComponentName;
import android.app.ActivityManager;
import leancloud.cordova.leanpush.LeanPush;
import android.os.Bundle;
import org.apache.cordova.*;
import java.util.List;
import com.avos.avoscloud.PushService;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVOSCloud;


import android.app.Activity;
import android.content.Intent;

public class MainActivity extends CordovaActivity
{
    private void checkAndSend(final String where, final String status){
        Bundle extras = getIntent().getExtras();
        if (extras != null)	{
            String data = extras.getString("com.avos.avoscloud.Data");
            if(data !=null){
                getIntent().removeExtra("com.avos.avoscloud.Data");
                LeanPush.sendJsonString(data, status);
            }
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        loadUrl(launchUrl);
        this.checkAndSend("onCreate","closed");
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.checkAndSend("onResume","background");                  
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

}
