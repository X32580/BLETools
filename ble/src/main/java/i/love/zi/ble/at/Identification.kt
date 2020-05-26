package i.love.zi.ble.at

/**
 * 作者 :  叶鹏
 * 时间 :  2020/5/26 11:27
 * 邮箱 :  1632502697@qq.com
 * 简述 :  所有的标识
 * 更新 :
 * 时间 :
 * 版本 : V 1.0
 */
object Identification {


    /**
     * 通道
     */
    const val BLE_SPP_Service = "0000fee0-0000-1000-8000-00805f9b34fb"
    const val BLE_SPP_Notify_Characteristic = "0000fee1-0000-1000-8000-00805f9b34fb"
    const val BLE_SPP_Write_Characteristic = "0000fee2-0000-1000-8000-00805f9b34fb"
    const val BLE_SPP_AT_Characteristic = "0000fee3-0000-1000-8000-00805f9b34fb"
    const val CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"


    /**
     * 通讯 信息 标识
     */
    const val SIGNAL_DATA = "RSSI_DATA"   //蓝牙信号
    const val CONNECT_SUCCESS = "Connect_Success" //连接成功
    const val DISCONNECT = "DisConnect" // 连接断开
    const val BLE_DATA = "Ble_Data"   //蓝牙发送的数据
    const val BLE_CONNECT_OVERTIME= "Connect_Overtime" //连接超时了


    /**
     *蓝牙状态  代码
     */
    //默认状态
    const val DEFAULT_STATE = 0x000
    //开始连接
    const val START_CONNECT =0x001
    // 没有找到目标设备
    const val NO_FIND_EQUIPMENT = 0x002
    // 找到设备并连接成功
    const val FIND_EQUIPMENT_AND_CONNECT_SUCCESS = 0x003
    //找到通道
    const val FIND_PASSAGEWAY_SUCCESS = 0x004
    // 没有找到通道
    const val FIND_PASSAGEWAY_FAIL = 0x005
    // 写入特征值 成功
    const val WRITE_PASSAGWAY_SUCCESS = 0x006
    //写入特征值失败
    const val WRITE_PASSAGWAY_FAIL = 0x007
    //设备连接完成
    const val DEVICE_CONNECT_SUCCESSFUL = 0x008
    //蓝牙断开
    const val DEVICE_DISCONNECT =0x008








}