package sample

import org.apache.commons.io.FileUtils
import org.jsoup.Jsoup
import java.io.File

/**
 * 网页端解析
 */
fun main(args: Array<String>) {
    val PATH = "D:\\tempDelete\\客户端文案.html"
    val html = FileUtils.readFileToString(File(PATH))
    var doc = Jsoup.parse(html)
    val trs = doc.select("tbody").select("tr")

    var list = arrayListOf<LangBean>()
    trs.forEach {
        val tds = it.select("td")

        val lineNum = it.select("th").text()
        val key = tds[0].text()
        val notUsed1 = tds[1].text()
        // 10种语言
        val lang_cn = tds[2].text()
        val lang_en = tds[3].text()
        val lang_tw = tds[4].text()
        val lang_in = tds[5].text()
        val lang_tr = tds[6].text()
        val lang_vi = tds[7].text()
        val lang_nono_els    = tds[8].text()    // 俄罗斯
        val lang_nono_ay     = tds[9].text()    // 阿语
        val lang_es          = tds[10].text()   // 西班牙
        val lang_nono_dy     = tds[11].text()   // 德语
        val lang_jp          = tds[12].text()
        val lang_kr          = tds[13].text()   // 韩语
        val notUsed2         = tds[14].text()
        val lang_th          = tds[15].text()    // 泰语

        val langBean = LangBean(
                        lineNum,
                        key,
                        lang_cn,
                        notUsed1,
                        lang_en,
                        lang_tw,
                        lang_in,
                        lang_tr,
                        lang_vi,
                        lang_nono_els,
                        lang_nono_ay,
                        lang_es,
                        lang_nono_dy,
                        lang_jp,
                        lang_kr,
                        notUsed2,
                        lang_th
        )
        list.add(langBean)
    }

    list.forEach {
        println(it)
        println()
    }
}

data class LangBean(
        var lineNum: String,
        var key: String,
        var lang_cn: String,
        var notUsed1: String,
        var lang_en: String,
        var lang_tw: String,
        var lang_in: String,
        var lang_tr: String,
        var lang_vi: String,
        var lang_nono_els: String,
        var lang_nono_ay: String,
        var lang_es: String,
        var lang_nono_dy: String,
        var lang_jp: String,
        var lang_kr: String,
        var notUsed2: String,
        var lang_th: String
) {
    override fun toString(): String {
        return "LangBean(lineNum='$lineNum', key='$key', lang_cn='$lang_cn', notUsed1='$notUsed1', lang_en='$lang_en', lang_tw='$lang_tw', lang_in='$lang_in', lang_tr='$lang_tr', lang_vi='$lang_vi', lang_nono_els='$lang_nono_els', lang_nono_ay='$lang_nono_ay', lang_es='$lang_es', lang_nono_dy='$lang_nono_dy', lang_jp='$lang_jp', lang_kr='$lang_kr', notUsed2='$notUsed2', lang_th='$lang_th')"
    }
}