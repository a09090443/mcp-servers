package tw.zipe.mcp.googlemap.places

import com.google.gson.FieldNamingStrategy
import java.lang.reflect.Field

/**
 * 移除 key 結尾的 "_"
 * @author zipe1
 * @created 2025/5/7
 */
class RemoveTrailingUnderscoreNamingStrategy : FieldNamingStrategy {
    override fun translateName(f: Field): String {
        return if (f.name.endsWith("_")) {
            f.name.removeSuffix("_")
        } else {
            f.name
        }
    }
}
