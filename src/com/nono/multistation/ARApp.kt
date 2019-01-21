package com.nono.multistation

var singleLangEN = SingleLang(EN)     // 英文
var singleLangAR = SingleLang(AR)     // 阿拉伯

fun main(args: Array<String>) {

    // 设置当前比较的语言
    SingleLang.initBaseLang(singleLangEN)
    SingleLang.initDiffLang(singleLangAR)

    // 占位和特殊字符有重复的keys.用set过滤
    val filterKeys = mutableSetOf<String>()
    val noFilterKeys = mutableListOf<String>()

    val holders = singleLangEN.getKeysForHolder()
    holders.forEach {
        filterKeys.add(it.key)
    }

    val specials = singleLangEN.getKeysForSpecial()
    specials.forEach {
        filterKeys.add(it.key)
    }

    singleLangEN.keyValues.forEach {
        val key = it.key
        if (!filterKeys.contains(key)) {
            noFilterKeys.add(key)
        }
    }
    val holdersNew = singleLangEN.getKeysForHolder()

    println("==============================")
//    holdersNew.forEach {
//        println("===  英文-阿语对照 ===")
//        println("key: ${it.key}  value: ${it.value}")
//        val keyValue = singleLangAR.findKeyValue(it.key)
//        println("key: ${keyValue.key}  value: ${keyValue.value}")
//    }

    singleLangAR.replaceValue("home_check_in_day_prefix", "")
}


fun doReplaceWebAr() {
    WebArHelper.getARList().forEach {
        var newkey = it.key
        var newValue = it.value
        singleLangAR.replaceValue(newkey, newValue)
    }

    // 没有变化的标注一下, 注意，因为文件已经改变, 必须重新初始化
    var singleLangARUpdate = SingleLang(AR)
    SingleLang.initDiffLang(singleLangARUpdate)

    singleLangEN.keyValues.forEach {
        val flag = SingleLang.findNotTranslateByOneFile(it.key, singleLangARUpdate)
        if (flag) {
            try {
                singleLangAR.replaceValue(it.key, it.value, "<!-- TODO UPDATE -->")
                // singleLangAR.replaceValue(it.key, it.value, "")
            } catch (e: Exception) {
                println("error: ${it.key}")
                e.printStackTrace()
            }
        } else {
            // not change
        }
    }
}
