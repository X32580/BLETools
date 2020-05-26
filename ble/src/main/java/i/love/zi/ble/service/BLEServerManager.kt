package i.love.zi.ble.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder

/**
 * 作者 :  叶鹏
 * 时间 :  2019/12/14 8:58BLEServiceBLEService
 * 邮箱 :  1632502697@qq.com
 * 简述 :  蓝牙连接管理类
 * 更新 :
 * 时间 :
 */
class BLEServerManager(context: Context) {

    lateinit var bleservice :BLEService.BleBinder
    private  var serviceConnection :ServiceConnection

    init {
        serviceConnection =object :ServiceConnection{

            override fun onServiceDisconnected(name: ComponentName?) {

            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
              bleservice  = service as BLEService.BleBinder  //得到服务对外的接口
            }
        }

        context.bindService(Intent(context,BLEService::class.java),serviceConnection,Context.BIND_AUTO_CREATE)

    }


    //静态 管理对象
    companion object {
       private  var bleServerManager: BLEServerManager? = null

        fun getInstance():BLEServerManager{

            return bleServerManager!!

        }


        fun initBleServer(context: Context){
            bleServerManager = BLEServerManager(context)

        }

    }




}