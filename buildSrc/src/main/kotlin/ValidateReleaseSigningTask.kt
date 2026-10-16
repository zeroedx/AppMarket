import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * 在打包 release 产物前校验签名参数是否齐备。
 * 缺少密钥时必须显式失败，避免产出未签名或误用 debug 签名的“正式包”。
 */
abstract class ValidateReleaseSigningTask : DefaultTask() {
    @get:Input
    abstract val configured: Property<Boolean>

    @TaskAction
    fun validate() {
        if (!configured.get()) {
            throw GradleException(
                "Release signing is not configured. Provide KEYSTORE_PATH, KEYSTORE_PASS, " +
                    "KEY_ALIAS and KEY_PASSWORD (as environment variables or in local.properties) " +
                    "before building a release artifact. Refusing to produce an unsigned or " +
                    "debug-signed release APK.",
            )
        }
    }
}
