package com.nono.multistation

import org.apache.commons.io.FileUtils
import java.io.File

fun main(args: Array<String>) {

    val regex = """<string name\s*=\s*"(.+)">(.+)</string>""".toRegex()
    val text = """
        <string name="luckydraw_message_join">join</string>
        <string name="push_hostlink_refuse_countdown">拒絕 </string>
        <string name="multi_guest_user_has_reject">已拒絕。</string>
        <string name="rate_message">五星好評提升Nonolive口碑</string>
        <string name="moment_tag_pattern">#%1${'$'}s 不错哦</string>
        <string name="moment_reply_to">回復：</string>
    """.trimIndent()

    // me_change_country_tips字符有换行
    var html = FileUtils.readFileToString(File(EN))
    val key = "moment_tag_pattern"
    val regexReplace = """<string name\s*=\s*"#key#">(.+)</string>""".replace("#key#", key).toRegex()
    val results = regexReplace.findAll(text)

    val newValue = "Hello %1\\${'$'}s".toString()
    results.forEach {
        val (oldValue) = it.destructured
        html = text.replace(regexReplace, "$newValue")
        println(html)
    }
}