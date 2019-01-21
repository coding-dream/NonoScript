package com.nono.multistation


/**
 * 需求：
 * 多国对齐
 *
 * 1. 把需要替换的转为对象，代码中仅仅作比较和缓存，真正的替换则通过正则匹配原文件的某个位置的key，否则通过记录位置或重新生成容易出现问题(注释丢失等)
 */
fun main(args: Array<String>) {

    var singleLangEN = SingleLang(EN)     // 英文
    var singleLangTW = SingleLang(TW)     // 台湾
    var singleLangIN = SingleLang(IN)     // 印尼
    var singleLangTR  = SingleLang(TR)    // 土耳其
    var singleLangVI = SingleLang(VI)     // 越南
    var singleLangES = SingleLang(ES)     // 西班牙
    var singleLangTH = SingleLang(TH)     // 泰文
    var singleLangJP = SingleLang(JP)     // 日本
    var singleLangKR = SingleLang(KR)     // 韩国
    var singleLangAR = SingleLang(KR)     // 阿拉伯

    var holderList = singleLangEN.getKeysForHolder()
    holderList.forEach {
        val key = it.key
        var results = StringBuffer()
        it.specialResults?.forEach {
            results.append(it.groupValues)
        }
        // println("$key $results")
    }

    // 找到所有语言中拥有某个值的key
    val keySet = SingleLang.findKeyContainsByAllFile("Fill in the contact information")
    println("keySet: $keySet")

    // 获取其他语言中某个key-value值目前没有翻译的文件
    val noTransList = SingleLang.findNotTranslateByAllFile("playbacks_my_playbacks")
    println("==============================")
    noTransList.forEach {
        println("noTransList: $it")
    }
    println("==============================")

    val breakRun = false
    if (breakRun) {
        println("不在继续执行")
        return
    }
    // 替换某国语言的key值
    val replaceKey = "playbacks_my_playbacks"
    // singleLangEN.replaceValue(replaceKey, "Congratulations! You win the prize draw in %1\${'$'}s live room and you will receive the gift from the host!")
    singleLangEN.replaceValue(replaceKey, "")
    singleLangTW.replaceValue(replaceKey, "")
    singleLangIN.replaceValue(replaceKey, "")
    singleLangTR.replaceValue(replaceKey, "")
    singleLangVI.replaceValue(replaceKey, "")
    singleLangES.replaceValue(replaceKey, "")
    singleLangTH.replaceValue(replaceKey, "")
    singleLangJP.replaceValue(replaceKey, "")
    singleLangKR.replaceValue(replaceKey, "")
    singleLangAR.replaceValue(replaceKey, "")
}

/**
 * 找出两个key集合中缺少的key
 */
fun compareKeyValues(keyValues: MutableList<KeyValue>, list: List<KeyValue>): List<KeyValue> {
    println("first: ${keyValues.count()}  second: ${list.count()}")

    var firstList: MutableList<KeyValue>?
    var secondList: MutableList<KeyValue>?
    if (keyValues.size > list.size) {
        firstList = keyValues
        secondList = list as MutableList<KeyValue>
    } else {
        firstList = list as MutableList<KeyValue>
        secondList = keyValues
    }
    val diffList = firstList.filterNot {
        filterMe(it, secondList)
    }
    println("diffList size: ${diffList.count()}")
    return diffList
}

fun filterMe(it: KeyValue, secondList: MutableList<KeyValue>?): Boolean {
    for (keyValue in secondList!!) {
        if (keyValue.key == it.key) {
            return true
        }
    }
    return false
}
