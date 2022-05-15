package cbdc.corda.flow

import org.junit.jupiter.api.Test
import java.io.File

class DummyFlowTest : CBDCFlowTest() {

    @Test
    fun test() {
        val file = File("./transfer_timestamps.txt")
        file.delete()
        val max = 100

        for (i in 1..max) {
            file.appendText("\n")
            nodeA.runFlow(DummyFlow("$i", true, true))
        }
    }
}
