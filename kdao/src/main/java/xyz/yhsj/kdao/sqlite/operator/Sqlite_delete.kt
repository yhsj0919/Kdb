package xyz.yhsj.kdao.sqlite.operator

import xyz.yhsj.kdao.sqlite.Condition
import xyz.yhsj.kdao.sqlite.annotation.PrimaryKey
import xyz.yhsj.kdao.sqlite.condition
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.dropTable
import xyz.yhsj.kdao.Kdao
import java.io.Serializable
import kotlin.reflect.full.declaredMemberProperties

/**删除
 * Created by LOVE on 2017/8/4 004.
 */

/**
 * 根据 实例 删除数据 ， 依靠实例的主键定位
 * 返回值：删除的数量*/
inline fun <reified T : Serializable> T.delete(): Int {
    val mClass = this.javaClass.kotlin
    val properties = mClass.declaredMemberProperties
    var propertyValue: Any? = null
    properties.forEach {
        if (it.annotations.map { it.annotationClass }.contains(PrimaryKey::class)) propertyValue = it.get(this)
    }
    if (propertyValue == null) throw Exception("${mClass.simpleName} 类型没有设置PrimaryKey， 或是  实例的PrimaryKey属性不能为null")
    return Kdao.database.use {
        this@delete.deleteByKey(propertyValue!!)
    }
}


/**
 * 清空 该类数据
 */
inline fun <reified T : Serializable> T.clear() {
    val name = this.javaClass.kotlin.simpleName
    return Kdao.database.use {
        dropTable(name!!, true)
    }
}


/**
 * 根据 主键 删除数据
 * 返回值：删除的数量*/
inline fun <reified T : Serializable> T.deleteByKey(primaryKey: Any): Int {
    val mClass = this.javaClass.kotlin
    val name = "${mClass.simpleName}"
    var propertyName: String? = null
    mClass.declaredMemberProperties.forEach {
        if (it.annotations.map { it.annotationClass }.contains(PrimaryKey::class)) propertyName = it.name
    }
    if (propertyName == null) throw Exception("$name 类型没有设置PrimaryKey")
    val condition = condition { propertyName!! equalsData primaryKey }
    return Kdao.database.use {
        tryDo {
            delete(name, condition.selects.joinToString(" and "), *condition.pairs.toTypedArray())
        }.let {
            when (it) {
                null, "no such table" -> 0
                is Int -> it
                else -> 0
            }
        } as Int
    }
}

/**返回值：删除的数量*/
inline fun <reified T : Serializable> T.deleteAll(crossinline condition: () -> Condition): Int {
    val name = "${this.javaClass.kotlin.simpleName}"
    return Kdao.database.use {
        tryDo {
            delete(name, condition().selects.joinToString(" and "), *condition().pairs.toTypedArray())
        }.let {
            when (it) {
                null, "no such table" -> 0
                is Int -> it
                else -> 0
            }
        }

    }
}

