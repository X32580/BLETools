package i.love.zi.ble.bae

import android.app.Application
import android.util.Log
import i.love.zi.ble.service.BLEServerManager

/**
 * 作者 :  叶鹏
 * 时间 :  2020/5/26 16:10
 * 邮箱 :  1632502697@qq.com
 * 简述 :
 * 更新 :
 * 时间 :
 * 版本 : V 1.0
 */
class BaseApploction :Application() {
    override fun onCreate() {

        super.onCreate()

        Log.e("初始化 成功过","0.0")
        BLEServerManager.initBleServer(this)
    }
}