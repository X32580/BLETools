package i.love.zi.ble.service

import android.app.Service
import android.bluetooth.*
import android.content.Intent
import android.os.Binder
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.bluetooth.BluetoothGatt as BluetoothGatt1
import android.bluetooth.BluetoothGattCallback as BluetoothGattCallback1
import i.love.zi.ble.at.BLEBaseCallback
import i.love.zi.ble.at.Identification
import i.love.zi.ble.at.Identification.BLE_SPP_Notify_Characteristic
import i.love.zi.ble.at.Identification.BLE_SPP_Service
import i.love.zi.ble.at.Identification.BLE_SPP_Write_Characteristic
import i.love.zi.ble.at.Identification.CLIENT_CHARACTERISTIC_CONFIG
import i.love.zi.ble.at.Identification.DEFAULT_STATE
import i.love.zi.ble.at.Identification.DEVICE_CONNECT_SUCCESSFUL
import i.love.zi.ble.at.Identification.DEVICE_DISCONNECT
import i.love.zi.ble.at.Identification.FIND_EQUIPMENT_AND_CONNECT_SUCCESS
import i.love.zi.ble.at.Identification.FIND_PASSAGEWAY_FAIL
import i.love.zi.ble.at.Identification.FIND_PASSAGEWAY_SUCCESS
import i.love.zi.ble.at.Identification.NO_FIND_EQUIPMENT
import i.love.zi.ble.at.Identification.START_CONNECT
import i.love.zi.ble.at.Identification.WRITE_PASSAGWAY_FAIL
import i.love.zi.ble.at.Identification.WRITE_PASSAGWAY_SUCCESS



/**
 * 作者 :  叶鹏
 * 时间 :  2019/12/10 13:34
 * 邮箱 :  1632502697@qq.com
 * 简述 :   蓝牙连接服务
 * 更新 :
 * 时间 :
 */
class BLEService : Service() {


    val tag ="BLEService:"
    /**
     * 蓝牙操作对象
     */
    private  var bluetoothAdapter : BluetoothAdapter ?= null
    private  var bluetoothGatt: BluetoothGatt1?= null

    /**
     * 读写 对象
     */
    private lateinit var mNotifyCharacteristic : BluetoothGattCharacteristic
    private lateinit var mWriteCharacteristic  :BluetoothGattCharacteristic

    /**
     * 处理业务 线程池
     */
    private lateinit var sendMessage : ExecutorService
    private lateinit var sendRiss : ExecutorService

    /**
     * 特征值
     */
    private  val UUID_BLE_SPP_NOTIFY = UUID.fromString(BLE_SPP_Notify_Characteristic)
    //蓝牙得状态
     var state  = DEFAULT_STATE

    //蓝牙基础回调
    private lateinit var callback : BluetoothGattCallback1

    //蓝牙地址
    private lateinit var address : String
    //倒计时
    private lateinit var countDownTimer: CountDownTimer  //计时器  如果 十秒以内没有成功 会 自动反馈给界面

    private lateinit var bleBaseCallback: BLEBaseCallback


    //是否获取蓝牙信号
   private  var isGetRiss = true

    //蓝牙信号 获取速度
    var rissSeep :Long = 500


    override fun onCreate() {
        super.onCreate()
        //两路线程 用于通信
        sendMessage = Executors.newSingleThreadExecutor()  //  发送数据线程池
        sendRiss = Executors.newSingleThreadExecutor() //发送 信号线程池
        bleBaseCallback = object : BLEBaseCallback{
            override fun connectionTimedOut(state: Int) {

            }

            override fun onReadRemoteSignal(signal: Int) {

            }

            override fun writeATState(status: Boolean, at: String) {

            }

            override fun setMtu(mtu: Int) {

            }

            override fun writeDescriptor(state: Boolean, code: Int) {

            }

            override fun getData(data: String) {

            }

            override fun onConnectionStateChange(state: Int) {

            }

            override fun onConnectSuccessful() {

            }

            override fun connectDis() {

            }

            override fun onError(code: Int) {

            }

        }

        countDownTimer =  object :CountDownTimer(10000,1000){
            override fun onFinish() {
                /**
                 * 超时 从状态 判断 是如何超时
                 */
                when(state){
                    START_CONNECT -> {
                        state = NO_FIND_EQUIPMENT
                        bleBaseCallback.connectionTimedOut(state)
                    }
                    else->{
                        bleBaseCallback.connectionTimedOut(state)

                    }

                }


            }

            override fun onTick(millisUntilFinished: Long) {

            }


        }

    }

    /**
     * 初始化蓝牙
     */


    var enabledCount = 0

    private fun initBluetooh(){

        if (bluetoothAdapter == null)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        /**
         * 没有开启蓝牙
         *
         */
        if (!bluetoothAdapter!!.isEnabled){

            //开启蓝牙 默认自动调起一次开启蓝牙
            if (enabledCount<1){
                enabledCount++
                bluetoothAdapter!!.enable()
                initBluetooh()
            }else{
                bleBaseCallback.onError(Identification.BLUETOOTH_NOT_ON)
            }

            //防止重复 调起蓝牙

            return
        }

        callback  = object :BluetoothGattCallback1(){
            override fun onReadRemoteRssi(
                gatt: android.bluetooth.BluetoothGatt?,
                rssi: Int,
                status: Int
            ) {
                /**
                 * 获取 蓝牙信号
                 */
                if (status ==BluetoothGatt1.GATT_SUCCESS)
                bleBaseCallback.onReadRemoteSignal(rssi)

            }


            override fun onCharacteristicRead(
                gatt: android.bluetooth.BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {


            }

            override fun onCharacteristicWrite(
                gatt: android.bluetooth.BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {

                val at =  String(characteristic!!.value)
                when (status) {
                    android.bluetooth.BluetoothGatt.GATT_SUCCESS -> {
                        bleBaseCallback.writeATState(true,at)
                    }
                    android.bluetooth.BluetoothGatt.GATT_FAILURE -> {
                        bleBaseCallback.writeATState(false,at)
                        Log.e(tag,"写入失败")
                    }
                    android.bluetooth.BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                        bleBaseCallback.writeATState(false,at)
                        Log.e(tag,"写入失败没有权限")
                    }
                    android.bluetooth.BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED -> {
                        bleBaseCallback.writeATState(false,at)
                        Log.e(tag,"给定的请求不被支持")
                    }
                    android.bluetooth.BluetoothGatt.GATT_INVALID_OFFSET -> {
                        bleBaseCallback.writeATState(false,at)
                        Log.e(tag,"请求了具有无效偏移量的读取或写入操作")
                    }
                    android.bluetooth.BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> {
                        bleBaseCallback.writeATState(false,at)
                        Log.e(tag,"写入操作超出了属性的最大长度")
                    }
                    android.bluetooth.BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION -> {
                        bleBaseCallback.writeATState(false,at)
                        Log.e(tag,"给定操作的加密不足")
                    }
                    android.bluetooth.BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION -> {
                        bleBaseCallback.writeATState(false,at)
                        Log.e(tag,"给定操作的身份验证不足")
                    }
                    else -> {
                        bleBaseCallback.writeATState(false,at)
                        Log.e(tag, "未知错误 :code$status")
                    }
                }

            }

            override fun onServicesDiscovered(gatt: android.bluetooth.BluetoothGatt?, status: Int) {

                if (status == android.bluetooth.BluetoothGatt.GATT_SUCCESS) { //成功获取当前设备



                    val service = gatt!!.getService(UUID.fromString(BLE_SPP_Service))
                    if (service != null) {
                        //找到服务，继续查找特征值
                        //2.1 特征值【0000fee1-0000-1000-8000-00805f9b34fb】
                        mNotifyCharacteristic =
                            service.getCharacteristic(UUID.fromString(BLE_SPP_Notify_Characteristic))
                        //2.2 特征值【0000fee2-0000-1000-8000-00805f9b34fb】
                        mWriteCharacteristic =
                            service.getCharacteristic(UUID.fromString(BLE_SPP_Write_Characteristic))
                    }

                        state =FIND_PASSAGEWAY_SUCCESS
                        Log.e(tag,"特征值找到 准备写入特征值")

                        //写入特征值才能通讯
                        setCharacteristicNotification(mNotifyCharacteristic, true)



                        //如果获取为空 则重新获取一个特征值 特征值【0000fee1-0000-1000-8000-00805f9b34fb】
                        mWriteCharacteristic = service!!.getCharacteristic(UUID.fromString(BLE_SPP_Notify_Characteristic))



                }else{

                    state  = FIND_PASSAGEWAY_FAIL
                    bleBaseCallback.onConnectionStateChange(state)
                    bleBaseCallback.connectDis()

                }


            }


            override fun onMtuChanged(
                gatt: android.bluetooth.BluetoothGatt?,
                mtu: Int,
                status: Int
            ) {
                super.onMtuChanged(gatt, mtu, status)
                bleBaseCallback.setMtu(mtu)
            }

            override fun onReliableWriteCompleted(
                gatt: android.bluetooth.BluetoothGatt?,
                status: Int
            ) {
                super.onReliableWriteCompleted(gatt, status)
            }

            override fun onDescriptorWrite(
                gatt: android.bluetooth.BluetoothGatt?,
                descriptor: BluetoothGattDescriptor?,
                status: Int
            ) {

                when (status) {
                    android.bluetooth.BluetoothGatt.GATT_SUCCESS -> {
                        //特征值写入完成
                        Log.e(tag,"写入成功")

                        state = WRITE_PASSAGWAY_SUCCESS
                        countDownTimer.cancel() //取消定时器
                        /**
                         * 缓冲 一秒钟 不然写不进去 指令
                         */
                          state = DEVICE_CONNECT_SUCCESSFUL

                          gatt!!.requestMtu(24)

                            bleBaseCallback.onConnectSuccessful()


                        /**
                         * 连接成功以后 发送蓝牙信号
                         */

                        if (isGetRiss){

                            sendRiss.execute{

                                while (true){
                                    if (state == DEVICE_CONNECT_SUCCESSFUL) {
                                        bluetoothGatt?.readRemoteRssi()
                                    }

                                        Thread.sleep(rissSeep)

                                }


                            }

                        }


                    }
                    android.bluetooth.BluetoothGatt.GATT_FAILURE -> {
                        state = WRITE_PASSAGWAY_FAIL
                         bleBaseCallback.onConnectionStateChange(state)
                        Log.e(tag,"写入失败")
                    }
                    android.bluetooth.BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                        state = WRITE_PASSAGWAY_FAIL
                        bleBaseCallback.onConnectionStateChange(state)
                        Log.e(tag,"写入失败没有权限")
                    }
                    android.bluetooth.BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED -> {
                        state = WRITE_PASSAGWAY_FAIL
                        bleBaseCallback.onConnectionStateChange(state)
                        Log.e(tag,"给定的请求不被支持")
                    }
                    android.bluetooth.BluetoothGatt.GATT_INVALID_OFFSET -> {
                        state = WRITE_PASSAGWAY_FAIL
                        bleBaseCallback.onConnectionStateChange(state)
                        Log.e(tag,"请求了具有无效偏移量的读取或写入操作")
                    }
                    android.bluetooth.BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> {
                        state = WRITE_PASSAGWAY_FAIL
                        bleBaseCallback.onConnectionStateChange(state)
                        Log.e(tag,"写入操作超出了属性的最大长度")
                    }
                    android.bluetooth.BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION -> {
                        state = WRITE_PASSAGWAY_FAIL
                        bleBaseCallback.onConnectionStateChange(state)
                        Log.e(tag,"给定操作的加密不足")
                    }
                    android.bluetooth.BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION -> {
                        state = WRITE_PASSAGWAY_FAIL
                        bleBaseCallback.onConnectionStateChange(state)
                        Log.e(tag,"给定操作的身份验证不足")
                    }
                    else -> {
                        state = WRITE_PASSAGWAY_FAIL
                        bleBaseCallback.onConnectionStateChange(state)
                        Log.e(tag,"未知错误  code :$status")
                    }
                }

            }

            //收到蓝牙板发送数据会调用这个方法
            override fun onCharacteristicChanged(
                gatt: android.bluetooth.BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?
            ) {

                sendMessage.execute {
                    bleBaseCallback.getData(String(characteristic!!.value))
                }


            }

            override fun onDescriptorRead(
                gatt: android.bluetooth.BluetoothGatt?,
                descriptor: BluetoothGattDescriptor?,
                status: Int
            ) {

            }

            override fun onConnectionStateChange(
                gatt: android.bluetooth.BluetoothGatt?,
                status: Int,
                newState: Int
            ) {
                when {
                    newState == android.bluetooth.BluetoothGatt.STATE_CONNECTED -> {  //连接成功
                        Log.e(tag,"蓝牙连接成功  准备寻找通道特征值 ")
                        state = FIND_EQUIPMENT_AND_CONNECT_SUCCESS
                        bluetoothGatt?.discoverServices()  //调用发现服务方法 找到通道
                    }
                    newState == android.bluetooth.BluetoothGatt.STATE_DISCONNECTED -> { //连接断开
                        state =DEVICE_DISCONNECT
                        bleBaseCallback.onConnectionStateChange(state)
                        bleBaseCallback.connectDis()
                    }
                    status == android.bluetooth.BluetoothGatt.GATT_CONNECTION_CONGESTED -> {
                        state =DEVICE_DISCONNECT
                        bleBaseCallback.onConnectionStateChange(state)
                        bleBaseCallback.connectDis()
                    }
                    else -> {
                        state =DEVICE_DISCONNECT
                        bleBaseCallback.onConnectionStateChange(state)
                        bleBaseCallback.connectDis()
                    }
                }

            }
        }


    }

    /**
     * 写入特征值
     */
    fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic,
        enabled: Boolean
    ) {

        bluetoothGatt?.setCharacteristicNotification(characteristic, enabled)

        // This is specific to BLE SPP Notify.
        if (UUID_BLE_SPP_NOTIFY == characteristic.uuid) { // 0000fee1-0000-1000-8000-00805f9b34fb == (characteristic.getUuid()
            // 获取DescriptorUUID值00002902-0000-1000-8000-00805f9b34fb 返回一个BluetoothGattDescriptor对象
            val descriptor =
                characteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG))
            descriptor.value =
                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE // 设置 BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE： {0x01, 0x00}
            bluetoothGatt?.writeDescriptor(descriptor)// 将给定描述符的值写入关联的远程设备。
        }
    }







    /**
     * 踩坑了
     *
     * 每次 调用写入 都需要 先获取 蓝牙 写入对象
     *
     * 读写需要写在一线程 保证同步 如果不同步  会造成数据错乱
     *
     */

    fun writeBLE(context: String) {


        if (state == DEVICE_CONNECT_SUCCESSFUL){

            sendMessage.execute {

                if ( bluetoothGatt ==null  || bluetoothGatt!!.getService(UUID.fromString(BLE_SPP_Service)) == null||bluetoothGatt!!.getService(UUID.fromString(BLE_SPP_Service)).getCharacteristic(UUID.fromString(BLE_SPP_Write_Characteristic))==null  ){
                   Log.e(tag,"UUID 为空")
                    return@execute
                }

                mWriteCharacteristic = bluetoothGatt!!.getService(UUID.fromString(BLE_SPP_Service)).getCharacteristic(UUID.fromString(BLE_SPP_Write_Characteristic))
                //放入写入数据

                /**
                 * 大于20 需要分包 处理
                 * 分包处理的数据 需要更快执行
                 */
                if (context.length>20){
                    mWriteCharacteristic.value = context.substring(0,19).toByteArray()
                    /**
                     * 可能会有问题因为 重复得我只会发一次
                     */

                    //写入 对象
                    bluetoothGatt?.writeCharacteristic(mWriteCharacteristic)

                    Thread.sleep(50)

                    writeBLE(context.substring(19,context.length))

                }else{


                    mWriteCharacteristic.value = context.toByteArray()
                    /**
                     * 可能会有问题因为 重复得我只会发一次
                     */

                    //写入 对象
                    bluetoothGatt?.writeCharacteristic(mWriteCharacteristic)
                    Thread.sleep(100)

                }



            }

        }else{
            /**
             *  蓝牙断开写入失败 回调
             */
            bleBaseCallback.writeATState(false,context)

        }



    }






    /**
     * 用于判断是否连接成功
     */
    private lateinit var device : BluetoothDevice

    /**
     *
     * 连接 蓝牙
     *
     */
    fun connectBLE(address: String) {

        //调用 会清除 蓝牙自动开启 的计数器
        enabledCount= 0

        this.address = address

        clean()

        initBluetooh()

        Log.e(tag,"连接得蓝牙$address")
         device = bluetoothAdapter!!.getRemoteDevice(address)  // 根据mac蓝牙地址获取蓝牙设备
        


        // parameter to false.
        // age1:
        // age2:是否需要自动连接。如果设置为 true, 表示如果设备断开了，会不断的尝试自动连接。设置为 false 表示只进行一次连接尝试。
        // age3:连接后进行的一系列操作的回调，例如连接和断开连接的回调，发现服务的回调，成功写入数据，成功读取数据的回调等等。
        state =START_CONNECT
        bluetoothGatt = device.connectGatt(this, false, callback)
        countDownTimer.start()  //启动定时器

    }




    /**
     *
     * 清除蓝牙引用
     *
     */
    private  fun clean(){
        if (bluetoothGatt !=null){
            bluetoothGatt!!.disconnect()
        }
        state =DEVICE_DISCONNECT

    }

    /**
     * 此功能需要外部调用
     */
    private fun disConnectAndRemoveCallback(){

        /**
         *  断开连接的时候清除 监听器
         */
        clean()

        bleBaseCallback =object :BLEBaseCallback{
            override fun connectionTimedOut(state: Int) {

            }

            override fun onReadRemoteSignal(signal: Int) {

            }

            override fun writeATState(status: Boolean, at: String) {

            }

            override fun getData(data: String) {

            }

            override fun onConnectionStateChange(state: Int) {

            }

            override fun onConnectSuccessful() {

            }

            override fun connectDis() {

            }

            override fun onError(code: Int) {

            }

        }

    }

    /**
     *
     * 绑定 以及对外方法
     *
     */
    override fun onBind(intent: Intent?): IBinder? {
        return BleBinder()
    }

    /**
     * 接触绑定的时候 断开蓝牙
     * 释放内存
     *
     */
    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }


    /**
     * 对外暴露的方法实现
     */
    inner class BleBinder : Binder(),IBleBinderInterface{
        override fun getConnectState(): Boolean {
            return state==DEVICE_CONNECT_SUCCESSFUL
        }

        override fun setGetRiss(b: Boolean) {
            if (!isGetRiss){

                isGetRiss = b


                if (isGetRiss){
                    sendRiss.execute{

                        while (true){
                            if (state == DEVICE_CONNECT_SUCCESSFUL && isGetRiss) {
                                bluetoothGatt?.readRemoteRssi()
                            }
                            try {
                                Thread.sleep(rissSeep)
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }
                        }

                    }
                }



            }

            isGetRiss = b

        }

        override fun setBleBaseCallback(callback: BLEBaseCallback) {
            bleBaseCallback = callback
        }


        override fun exit() {
            bluetoothGatt?.close()
        }



        override fun connect(address: String) {
            connectBLE(address)
        }

        override fun writeData(data: String) {
            writeBLE(data)
        }

        override fun disConnectAndRemoveCallback() {
            this@BLEService.disConnectAndRemoveCallback()
        }

        override fun disConnect() {
            clean()
        }

    }

    //对外暴露的接口
    internal interface IBleBinderInterface{

        fun connect(address:String)

        fun writeData(data:String)

        //断开连接 并且 清除回调
        fun disConnectAndRemoveCallback()

        //清除蓝牙连接 不会清除接口
        fun disConnect()

        /**
         * 退出才 调用 关闭蓝牙 gatt 连接
         */
        fun exit()

        fun getConnectState():Boolean

        fun setGetRiss(b : Boolean)

        fun setBleBaseCallback(callback: BLEBaseCallback)

    }

}