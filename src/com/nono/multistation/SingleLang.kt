package com.nono.multistation

import org.apache.commons.io.FileUtils
import java.io.File

val regex = """<string name\s*=\s*"(.+)">(.+)</string>""".toRegex()

class SingleLang(val lang: String){

    val keyValues = mutableListOf<KeyValue>()

    init {
        createKeyValues()
    }

    /**
     * 创建某个国家语言的所有key value 集合
     */
    fun createKeyValues() {
        val html = FileUtils.readFileToString(File(lang))
        val results = regex.findAll(html)
        results.forEach {
            val (first, second) = it.destructured
            val keyValue = KeyValue(key = first, value = second)
            keyValues.add(keyValue)
        }
    }

    /**
     * 获取该语言占位符key的个数
     */
    fun getKeysForHolder(): List<KeyValue> {
        var list = keyValues.filter {
            // 每一个keyValue匹配其中的占位符
            // Remains: %1$d day %2$02d:%3$02d:%4$02d
            val regex = """%\d*\$+\d*[s|d]""".trimIndent().toRegex()
            val results = regex.findAll(it.value)
            it.holderResults = results.toList()
            results.count() > 0
        }
        println("holderKeySize: ${list.size}")
        return list
    }

    /**
     * 获取该语言特殊字符key的个数
     */
    fun getKeysForSpecial(): List<KeyValue> {
        var list = keyValues.filter {
            // 每一个keyValue匹配其中的占位符
            // &#38;
            val regex = """\&#\d+;""".trimIndent().toRegex()
            val results = regex.findAll(it.value)
            it.specialResults = results.toList()
            results.count() > 0
        }
        println("specialKeySize: ${list.size}")
        return list
    }

    /**
     * 获取当前文件中含有某个值的key
     */
    fun findKeyContains(content: String): List<KeyValue> {
        val resultList = keyValues.filter {
            it.value.contains(content)
        }
        return resultList
    }

    /**
     * 获取当前文件某个key-value
     */
    fun findKeyValue(key: String): KeyValue {
        return keyValues.first {
            it.key == key
        }
    }

    /**
     * 替换当前文件某个key的值
     */
    @Synchronized
    fun replaceValue(key: String, newValule: String, mark: String = ""): String {
        if(key == "" || newValule == "") {
            println("return : not replace")
            return ""
        }

        val regexReplace = """<string name\s*=\s*"#key#">(.+)</string>""".replace("#key#", key).toRegex()
        var html = FileUtils.readFileToString(File(lang))
        val results = regexReplace.findAll(html)
        val template = """<string name="$key">#value#</string>$mark"""
        // 注意特殊字符的处理, 蛋疼的Java每次输出都会把类似\n等转为真正的换行到文本中.
        val newResult = template.replace("#value#", newValule).replace("\n","\\n")
        results.forEach {
            html = html.replace(regexReplace, newResult)
        }
        FileUtils.write(File(lang), html)
        return html
    }

    companion object {

        /**
         * 创建基准,防止每次都创建一次
         */
        var baseSingleLang: SingleLang? = null

        /**
         * 缓存当前需要比较的语言
         */
        var diffSingleLang: SingleLang? = null

        /**
         * 生成xml文件
         */
        fun generateXml(keyValues: List<KeyValue>) {
            val template = """
            <string name="[key]">[value]</string>
            """.trimIndent()
            val buffer = StringBuffer()
            buffer.apply {
                keyValues.forEach {
                    val str = template.replace("[key]", it.key).replace("[value]", it.value)
                    append(str)
                }
            }
            FileUtils.write(File("D:/test/1.xml"), buffer.toString())
        }

        /**
         * 获取所有文件中含有某个值的key
         */
        fun findKeyContainsByAllFile(content: String): HashSet<String> {
            val resultKeySet = hashSetOf<String>()
            allFilePath.forEach {
                val singleKeyValue = SingleLang(it)
                val keyValues = singleKeyValue.findKeyContains(content)
                if (keyValues.isNotEmpty()) {
                    keyValues.forEach {
                        resultKeySet.add(it.key)
                    }
                }
            }
            return resultKeySet
        }

        /**
         * 通过对比基准EN，某个key在其他各国中没有翻译的文件名
         */
        fun findNotTranslateByAllFile(key: String): ArrayList<String> {
            val fileNames = arrayListOf<String>()

            val valueFirst = baseSingleLang!!.findKeyValue(key)
            allFilePath.forEach {
                val singleLang = SingleLang(it)
                val valueSecond = singleLang.findKeyValue(key)
                if (valueFirst.value == valueSecond.value) {
                    fileNames.add(singleLang.lang)
                }
            }
            return fileNames
        }

        /**
         * 通过对比基准EN，某个key在某国中没有翻译
         */
        @Synchronized
        fun findNotTranslateByOneFile(key: String, diffLang: SingleLang): Boolean {
            val valueFirst = baseSingleLang!!.findKeyValue(key)
            val valueSecond = diffLang.findKeyValue(key)
            // println("valueFirst: ${valueFirst.value} valueSecond: ${valueSecond.value}")
            return valueFirst.value == valueSecond.value
        }

        fun initBaseLang(singleLang: SingleLang) {
            baseSingleLang = singleLang
        }

        fun initDiffLang(singleLang: SingleLang) {
            diffSingleLang = singleLang
        }
    }
}

data class KeyValue (
        var key: String,
        var value: String,
        var holderResults: List<MatchResult>? = null,
        var specialResults: List<MatchResult>? = null
)