package com.xiangxue.trackdemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.LruCache
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.location.*
import com.xiangxue.trackdemo.util.Utils
import kotlinx.android.synthetic.main.activity_main.*


open class MainActivity : AppCompatActivity(), AMapLocationListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState)

        initLocation()
        tvInfo.setOnClickListener {
            val intent = Intent(MainActivity@ this, LruCacheActivity::class.java)
            startActivity(intent)
        }
    }

    //声明mlocationClient对象
    private val mlocationClient: AMapLocationClient by lazy { AMapLocationClient(this) }
    //声明mLocationOption对象
    private val mLocationOption: AMapLocationClientOption by lazy { AMapLocationClientOption() }

    //初始化定位参数
    fun initLocation() {
        //设置返回地址信息，默认为true
        mLocationOption.isNeedAddress = true
        //设置定位监听
        //设置定位监听
        mlocationClient.setLocationListener(this)
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.interval = 2000
        //设置定位参数
        mlocationClient.setLocationOption(mLocationOption)
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        //启动定位
        mlocationClient.startLocation()
    }


    override fun onDestroy() {
        super.onDestroy()
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState)
    }

    /**
     * 定位回掉接口
     */
    override fun onLocationChanged(amapLocation: AMapLocation?) {
        if (amapLocation == null) return
        if (amapLocation.errorCode == 0) {
            val sb = StringBuffer()
            //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
            //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
            if (amapLocation.getErrorCode() == 0) {
                sb.append("定位成功" + "\n")
                sb.append("定位类型: " + amapLocation.getLocationType() + "\n")
                sb.append("经    度    : " + amapLocation.getLongitude() + "\n")
                sb.append("纬    度    : " + amapLocation.getLatitude() + "\n")
                sb.append("精    度    : " + amapLocation.getAccuracy() + "米" + "\n")
                sb.append("提供者    : " + amapLocation.getProvider() + "\n")
                sb.append("速    度    : " + amapLocation.getSpeed() + "米/秒" + "\n")
                sb.append("角    度    : " + amapLocation.getBearing() + "\n")
                // 获取当前提供定位服务的卫星个数
                sb.append("星    数    : " + amapLocation.getSatellites() + "\n")
                sb.append("国    家    : " + amapLocation.getCountry() + "\n")
                sb.append("省            : " + amapLocation.getProvince() + "\n")
                sb.append("市            : " + amapLocation.getCity() + "\n")
                sb.append("城市编码 : " + amapLocation.getCityCode() + "\n")
                sb.append("区            : " + amapLocation.getDistrict() + "\n")
                sb.append("区域 码   : " + amapLocation.getAdCode() + "\n")
                sb.append("地    址    : " + amapLocation.getAddress() + "\n")
                sb.append("兴趣点    : " + amapLocation.getPoiName() + "\n")
                //定位完成的时间
                sb.append(
                    "定位时间: " + Utils.formatUTC(
                        amapLocation.getTime(),
                        "yyyy-MM-dd HH:mm:ss"
                    ).toString() + "\n"
                )
            } else { //定位失败
                sb.append("定位失败" + "\n")
                sb.append("错误码:" + amapLocation.getErrorCode() + "\n")
                sb.append("错误信息:" + amapLocation.getErrorInfo() + "\n")
                sb.append("错误描述:" + amapLocation.getLocationDetail() + "\n")
            }
            sb.append("***定位质量报告***").append("\n")
            sb.append("* WIFI开关：")
                .append(if (amapLocation.getLocationQualityReport().isWifiAble()) "开启" else "关闭")
                .append("\n")
            sb.append("* GPS状态：")
                .append(getGPSStatusString(amapLocation.getLocationQualityReport().getGPSStatus()))
                .append("\n")
            sb.append("* GPS星数：").append(amapLocation.getLocationQualityReport().getGPSSatellites())
                .append("\n")
            sb.append("* 网络类型：" + amapLocation.getLocationQualityReport().getNetworkType())
                .append("\n")
            sb.append("* 网络耗时：" + amapLocation.getLocationQualityReport().getNetUseTime())
                .append("\n")
            sb.append("****************").append("\n")
            //定位之后的回调时间
            sb.append(
                "回调时间: " + Utils.formatUTC(
                    System.currentTimeMillis(),
                    "yyyy-MM-dd HH:mm:ss"
                ).toString() + "\n"
            )

            //解析定位结果，
            //解析定位结果，
            val result = sb.toString()

            tvInfo.text = result
        } else {
            //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
            Log.e(
                "AmapError", "amapLocation Error, ErrCode:"
                        + amapLocation.errorCode + ", errInfo:"
                        + amapLocation.errorInfo
            )
        }
    }

    /**
     * 获取GPS状态的字符串
     * @param statusCode GPS状态码
     * @return
     */
    private fun getGPSStatusString(statusCode: Int): String? {
        var str = ""
        when (statusCode) {
            AMapLocationQualityReport.GPS_STATUS_OK -> str = "GPS状态正常"
            AMapLocationQualityReport.GPS_STATUS_NOGPSPROVIDER ->
                str = "手机中没有GPS Provider，无法进行GPS定位"
            AMapLocationQualityReport.GPS_STATUS_OFF -> str = "GPS关闭，建议开启GPS，提高定位质量"
            AMapLocationQualityReport.GPS_STATUS_MODE_SAVING ->
                str = "选择的定位模式中不包含GPS定位，建议选择包含GPS定位的模式，提高定位质量"
            AMapLocationQualityReport.GPS_STATUS_NOGPSPERMISSION -> str = "没有GPS定位权限，建议开启gps定位权限"
        }
        return str
    }
}

