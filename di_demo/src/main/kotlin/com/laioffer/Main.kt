package com.laioffer

// Lesson 55 可执行部分：纯 Kotlin 的依赖注入（Constructor Injection）演示。
// 对应讲义截图 02/03 的 Car + Engine 例子，无需 Android 模拟器即可运行。
// 运行：gradlew run

interface Engine {
    fun start()
}

class GasEngine : Engine {
    override fun start() = println("GasEngine: vroom! (gas engine starts)")
}

class ElectricEngine : Engine {
    override fun start() = println("ElectricEngine: silent whir... (electric engine starts)")
}

// --- 反例（截图02）：Car 自己 new Engine，紧耦合，无法换发动机 ---
class BadCar {
    private val engine = GasEngine() // 写死，升级电动车要改源码
    fun start() = engine.start()
}

// --- 正例（截图03）：构造函数注入，Car 不关心 Engine 是谁 ---
class Car(private val engine: Engine) {
    fun start() = engine.start()
}

fun main() {
    println("== 1. bad example: tightly coupled ==")
    BadCar().start()

    println("== 2. constructor injection (manual DI) ==")
    val gasCar = Car(GasEngine())
    gasCar.start()

    val electricCar = Car(ElectricEngine())
    electricCar.start()

    println("== 3. what Hilt does for you ==")
    println("Hilt would build the dependency graph and call Car(GasEngine()) automatically.")
}
