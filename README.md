# ShecareBle SDK UI Demo
Divided into UI library and SDK library, the UI library needs to be manually copied to your own project, and the SDK library is uploaded to bintray and imported through Gradle
## Demo
[http://fir.ikangtai.cn/x5zp](http://fir.ikangtai.cn/x5zp)

## Internationalization
English | [中文文档](README_zh.md)

## UI library
### UI library integration
   ```java
       implementation project(path:':ScBluetoothUI')
   ```
### Instructions
  1.Project Bluetooth module MVP structure to realize related interfaces
  ```java

       * implements BleContract.IView
       presenter = new BlePresenter(this, this)
  ```
  2.Implement related data callback
  ```java
      /**
       * Handling of received thermometer temperature
       *
       * @param temperatureInfoList
       */
      @Override
      public void onReceiveTemperatureData(List<TemperatureInfo> temperatureInfoList) {
          if (App.getInstance().isForeground()) {
              //App front pop-up display

          } else {
              //App background to send notifications

          }
      }

      /**
       * Handling manual preservation of thermometer temperature
       *
       * @param temperatureInfo
       */
      public void onSaveTemperatureData(TemperatureInfo temperatureInfo) {
          //Processing storage temperature

      }
  ```
  3.Receive EventBus events
  Bluetooth or thermometer status changes receive related events
   ```java

     /**
      * Show thermometer status
      *
      * @param eventBus
      */
     @Subscribe(threadMode = ThreadMode.MAIN)
     public void syncBLeState(BleStateEventBus eventBus) {


     }

     /**
      * Display device Bluetooth status
      *
      * @param eventBus
      */
     @Subscribe(threadMode = ThreadMode.MAIN)
     public void synBluetoothState(BluetoothStateEventBus eventBus) {

     }
   ```
  4.Handle the logic related to the thermometer
   ```java
        @Override
        public void onResume() {
            super.onResume();
            //Start scanning for nearby devices when the device is not connected
            if (!AppInfo.getInstance().isThermometerState()) {
                presenter.startScan();
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            //Processing request to turn on the Bluetooth switch and positioning switch results
            CheckBleFeaturesUtil.handBleFeaturesResult(getContext(), requestCode, resultCode);
        }
   ```
  5.Show add temperature Dialog
  ```java
    presenter.showAddTemperatureView();
  ```

  6.Release resources to disconnect Bluetooth
  ```java
    presenter.destroy();
  ```
### Confusion configuration
If your application uses code obfuscation, please add the following configuration to avoid SDK being unavailable due to incorrect obfuscation.
```java
    -dontwarn  com.ikangtai.bluetoothsdk.**
    -keep class com.ikangtai.bluetoothsdk.** {*;}
    -dontwarn  com.ikangtai.bluetoothui.**
    -keep class com.ikangtai.bluetoothui.** {*;}
```
  
## SDK privacy agreement
a) Purpose/purpose of collecting personal information: optimizing hardware compatible devices<br/>
b) The type of personal information collected: device model, operating system, mobile phone developer identifier, network data<br/>
c) Required permissions: network permissions, Bluetooth permissions<br/>
d) Third-party SDK privacy policy link: https://static.shecarefertility.com/shecare/resource/dist/#/blesdk_privacy_policy<br/>
e) Provider: Beijing ikangtai Technology Co., Ltd.<br/>

## Bluetooth SDK access guide
[English document](https://github.com/iKangtai/ScBluetoothSdkDemo_Android/blob/master/README.md)