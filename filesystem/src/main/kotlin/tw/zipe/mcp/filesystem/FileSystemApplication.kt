package tw.zipe.mcp.filesystem

import io.quarkus.runtime.Quarkus
import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain
import java.util.logging.Logger
import kotlin.jvm.java

/**
 * @author Gary
 * @created 2025/4/21
 */
@QuarkusMain
class FileSystemApplication : QuarkusApplication {
    companion object {
        private val logger = Logger.getLogger(FileSystemApplication::class.java.name)
    }

    override fun run(vararg args: String?): Int {
        logger.info("啟動 FileSystem MCP server...")

        if (args.isEmpty() || args.all { it == null }) {
            logger.severe("錯誤：未提供必要的檔案路徑參數，系統將終止啟動")
            return 1 // 返回非零值表示異常終止
        }

        val paths = args.filterNotNull().joinToString(",")
        System.setProperty("fileserver.paths", paths)
        logger.info("啟動參數已設置: fileserver.paths=$paths")

        Quarkus.waitForExit()
        return 0
    }
}

fun main(args: Array<String>) {
    System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
    Quarkus.run(FileSystemApplication::class.java, *args)
}
