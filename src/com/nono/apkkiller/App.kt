package com.nono.apkkiller

import com.nono.apkkiller.util.RunTimeUtil
import java.io.File

// https://blog.csdn.net/zlp1992/article/details/41013351
fun main(args: Array<String>) {
    val originApk = "D:\\AndroidKillerProject\\抖音.apk"

    val path = "D:\\AndroidKillerProject\\"
    val file = File(path)
    if (!file.exists()) {
        file.mkdirs()
    }

    // 这里在windows下一定要加/c参数 ,否则会报错,/c是 执行字符串指定的命令然后终止
    // 如gradle的用法：commandLine 'cmd', '/c', 'dx --dex --output=' + outputPath + " " + sourcePath
    val result = RunTimeUtil.call("cmd /c apktool d $originApk ")
    println(result)
}