fun myFunc(id: Int = System.currentTimeMillis().toInt()) {
    println(id)
}
fun main() {
    myFunc()
    Thread.sleep(10)
    myFunc()
}
