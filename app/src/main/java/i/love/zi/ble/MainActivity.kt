package i.love.zi.ble

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import i.love.zi.ble.at.BLEBaseCallback
import i.love.zi.ble.service.BLEServerManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        text.setOnClickListener {

            text.text = "连接中"
            BLEServerManager.getInstance().bleservice.connect("8D:E0:75:8C:B6:44")

        }

        write.setOnClickListener {

            BLEServerManager.getInstance().bleservice.writeData("look1/")
        }

        text2.setOnClickListener {
            text2.text="设置监听成功"
            BLEServerManager.getInstance().bleservice.setBleBaseCallback(object :BLEBaseCallback{
                override fun connectionTimedOut(state: Int) {

                    runOnUiThread {
                        text.text = "${text.text}\n 蓝牙连接超时 $state "

                    }

                }

                override fun onReadRemoteSignal(signal: Int) {

                }

                override fun writeATState(status: Boolean, at: String) {

                    runOnUiThread {
                        text.text = "${text.text}\n 写入指令结果$status :$at "

                    }

                }

                override fun getData(data: String) {
                    runOnUiThread {
                        text.text = "${text.text}\n 数据:$data "

                    }
                }

                override fun onConnectionStateChange(state: Int) {
                    runOnUiThread {
                        text.text = "${text.text}\n 状态改变$state "

                    }
                }

                override fun onConnectSuccessful() {
                    runOnUiThread {
                        text.text = "${text.text}\n 连接成功 "
                    }

                }

                override fun connectDis() {

                }

                override fun onError(code: Int) {

                }

            })
        }

    }
}
