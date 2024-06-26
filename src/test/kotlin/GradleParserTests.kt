import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class GradleParserTest : StringSpec({
    "should parse tasks block" {
        val script = """
            tasks.register('myCustomTask', JavaExec) {
                mainClass = 'com.example.Main'
                classpath = sourceSets.main.runtimeClasspath
            }
        """.trimIndent()

        val tree = parseGradleScript(script)
        val visitor = object : GradleBaseVisitor<Unit>() {
            override fun visitTaskRegistration(ctx: GradleParser.TaskRegistrationContext) {
                ctx.STRING().text shouldBe "'myCustomTask'"
                ctx.IDENTIFIER().text shouldBe "JavaExec"
                ctx.taskStatement().forEach { statement ->
                    when (statement.IDENTIFIER().text) {
                        "mainClass" -> statement.STRING().text shouldBe "'com.example.Main'"
                        "classpath" -> statement.identifierPath().text shouldBe "sourceSets.main.runtimeClasspath"
                    }
                }
            }
        }

        visitor.visit(tree)
    }

    "should parse dependencies block and visit dependency entries" {
        val script = """
            dependencies {
                implementation 'org.springframework:spring-core:5.2.8.RELEASE'
                implementation 'com.google.guava:guava:30.1-jre'
            }
        """.trimIndent()

        val tree = parseGradleScript(script)
        val visitor = object : GradleBaseVisitor<Unit>() {
            val dependencies = mutableListOf<String>()

            override fun visitDependencyEntry(ctx: GradleParser.DependencyEntryContext) {
                dependencies.add(ctx.STRING().text)
            }
        }

        visitor.visit(tree)

        visitor.dependencies shouldBe listOf(
            "'org.springframework:spring-core:5.2.8.RELEASE'",
            "'com.google.guava:guava:30.1-jre'"
        )
    }

    "should parse plugins block and visit plugin entries" {
        val script = """
            plugins {
                id 'java'
                id 'org.jetbrains.kotlin.jvm' version '1.5.31'
            }
        """.trimIndent()

        val tree = parseGradleScript(script)

        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        val originalOut = System.out
        System.setOut(printStream)

        val visitor = object : GradleBaseVisitor<Unit>() {
            override fun visitPluginEntry(ctx: GradleParser.PluginEntryContext) {
                val pluginId = ctx.STRING(0).text
                val version = ctx.STRING(1)?.text
                print("Plugin: $pluginId")
                val optionalVersion = if (version != null) {
                    " Version: $version"
                } else {
                    ""
                }
                println(optionalVersion)
            }
        }

        visitor.visit(tree)
        System.out.flush()
        System.setOut(originalOut)

        val output = outputStream.toString().trim()
        output shouldBe """
            Plugin: 'java'
            Plugin: 'org.jetbrains.kotlin.jvm' Version: '1.5.31'
        """.trimIndent()
    }
})

fun parseGradleScript(script: String): GradleParser.BuildScriptContext {
    val lexer = GradleLexer(CharStreams.fromString(script))
    val tokens = CommonTokenStream(lexer)
    val parser = GradleParser(tokens)
    return parser.buildScript()
}
