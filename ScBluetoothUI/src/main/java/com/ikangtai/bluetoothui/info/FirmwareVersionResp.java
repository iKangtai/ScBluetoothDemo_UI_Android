package com.ikangtai.bluetoothui.info;

/**
 * desc
 *
 * @author xiongyl 2021/1/25 23:35
 */
public class FirmwareVersionResp {
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        /*"fileUrl": "{\"A\":\"http://yunchengfile.oss-cn-beijing.aliyuncs.com/firmware/A31/Athermometer.bin\",\"B\":\"http://yunchengfile.oss-cn-beijing.aliyuncs.com/firmware/A31/Bthermometer.bin\"}\r\n",
        "version": "3.67",
        "type": 1*/
        private String fileUrl;
        private String version;
        private int type;

        public String getFileUrl() {
            return fileUrl;
        }

        public void setFileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }
}
