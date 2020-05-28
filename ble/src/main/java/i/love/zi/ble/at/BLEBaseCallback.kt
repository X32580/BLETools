package i.love.zi.ble.at

/**
 * 作者 :  叶鹏
 * 时间 :  2020/5/26 11:46
 * 邮箱 :  1632502697@qq.com
 * 简述 :  蓝牙 接口回调
 * 更新 :  回调属于异步 如有 更改ui得功能 需要手动调用切换到UI线程
 * 时间 :
 * 版本 : V 1.0
 */
interface BLEBaseCallback {

    /**
     * 连接超时 返回
     * @param state 蓝牙连接状态
     */
    fun connectionTimedOut(state :  Int)


    /**
     * 获取蓝牙信号
     * @param signal 信号
     */
    fun onReadRemoteSignal( signal: Int){}


    /**
     *  写入 蓝牙指令
     *  @param status 写入状态
     *  @param at 写入得指令
     */
    fun writeATState(status: Boolean,at :String){

    }



    /**
     * 设备 设备mtu 值
     * @param mtu 值
     */
    fun  setMtu(mtu:Int){

    }


    /**
     * 写入特征值 返回结果
     * @param state 是否写入成功
     */

    fun writeDescriptor(state: Boolean,code :Int){

    }

    /**
     * 获取蓝牙数据
     * @param data  数据
     */

    fun getData(data:String)

    /**
     * 蓝牙连接状态 改变
     * @param state 状态
     *
     */
    fun onConnectionStateChange(state: Int){

    }


    /**
     * 蓝牙连接成功
     */
    fun onConnectSuccessful()

    /**
     * 蓝牙断开 回调
     */
    fun connectDis()

    /**
     * 出现错误的时候
     */
    fun onError(code: Int)




}