package com.nono.multistation

import org.apache.commons.io.FileUtils
import org.jsoup.Jsoup
import java.io.File

object WebArHelper {

    fun getARList(): MutableList<ArBean> {
        val path = "D:\\tempDelete\\Nonolive多国文案\\客户端文案.html"
        val html = FileUtils.readFileToString(File(path))
        var doc = Jsoup.parse(html)
        val trs = doc.select("tbody").select("tr")

        var list = arrayListOf<ArBean>()
        trs.forEach {
            val tds = it.select("td")

            val lineNum = it.select("th").text()

            val key = tds[0].text()
            // 阿拉伯
            val value = tds[9].text()
            // 英文
            // val value = tds[2].text()

            val langBean = ArBean(
                    key = key,
                    value = value
            )

            if (key != "" && value != "") {
                list.add(langBean)
            }
        }
        println("WebArSize: ${list.size}")
        return list
    }
}

data class ArBean(
        var key: String,
        var value: String
)