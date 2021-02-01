# Shecare蓝牙SDK UI Demo
分为UI库和SDK库,UI库需要手动Copy到自己项目，SDK库上传到bintray通过Gradle引入

## UI库
### UI库接入
   ```java
       implementation project(path:':ScBluetoothUI')
   ```
### 使用方法
  1.项目蓝牙模块MVP结构，实现相关接口
  ```java

       * implements BleContract.IView
       presenter = new BlePresenter(this, this)
  ```
  2.实现相关数据回调
  ```java
      /**
       * 处理接收到体温计温度
       *
       * @param temperatureInfoList
       */
      @Override
      public void onReceiveTemperatureData(List<TemperatureInfo> temperatureInfoList) {
          if (App.getInstance().isForeground()) {
              //App前台弹框显示

          } else {
              //App后台发送通知

          }
      }

      /**
       * 处理手动保存体温计温度
       *
       * @param temperatureInfo
       */
      public void onSaveTemperatureData(TemperatureInfo temperatureInfo) {
          //处理保存温度

      }
  ```
  3.接收EventBus事件
  蓝牙或者体温计状态改变接收到相关事件
   ```java

     /**
      * 显示体温计状态
      *
      * @param eventBus
      */
     @Subscribe(threadMode = ThreadMode.MAIN)
     public void syncBLeState(BleStateEventBus eventBus) {


     }

     /**
      * 显示设备蓝牙状态
      *
      * @param eventBus
      */
     @Subscribe(threadMode = ThreadMode.MAIN)
     public void synBluetoothState(BluetoothStateEventBus eventBus) {

     }
   ```
  4.处理体温计相关逻辑
   ```java
        @Override
        public void onResume() {
            super.onResume();
            //未连接设备时开始扫描附近设备
            if (!AppInfo.getInstance().isThermometerState()) {
                presenter.startScan();
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            //处理请求打开蓝牙开关、定位开关结果
            CheckBleFeaturesUtil.handBleFeaturesResult(getContext(), requestCode, resultCode);
        }
   ```
  5.显示添加温度Dialog
  ```java
    presenter.showAddTemperatureView();
  ```

  6.释放资源断开蓝牙
  ```java
    presenter.destroy();
  ```
## 蓝牙SDK接入指南
[中文文档](https://github.com/iKangtai/ScBluetoothSdkDemo_Android/blob/master/README_zh.md)