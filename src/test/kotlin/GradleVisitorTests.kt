import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe


class GradleVisitorTests : StringSpec({
    "should parse plugin block and visit plugin entries" {
        val script = """
            plugins {
                id 'java'
                id 'org.jetbrains.kotlin.jvm' version '1.3.50'
            }
        """.trimIndent()

        val tree = parseGradleScript(script)
        val visitor = object : GradleBaseVisitor<Unit>() {
            val dependencies = mutableListOf<String>()

            override fun visitPluginEntry(ctx: GradleParser.PluginEntryContext) {
                dependencies.add(ctx.STRING(0)?.text ?: "")
            }
        }

        visitor.visit(tree)

        visitor.dependencies shouldBe listOf(
            "'java'",
            "'org.jetbrains.kotlin.jvm'"
        )
    }
})