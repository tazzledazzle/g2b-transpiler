import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File

fun main() {
//    val fileName = "src/test/resources/build.gradle"
//    val gradleScript = File(fileName).readText()
    val gradleScript = """
        plugins {
            id 'java'
            id 'org.jetbrains.kotlin.jvm' version '1.5.31'
        }

        repositories {
            mavenCentral()
        }

        dependencies {
            implementation 'org.springframework:spring-core:5.2.8.RELEASE'
            implementation 'com.google.guava:guava:30.1-jre'
        }

        tasks.register('myCustomTask', JavaExec) {
            mainClass = 'com.example.Main'
            classpath = sourceSets.main.runtimeClasspath
        }
    """.trimIndent()

    val tree: GradleParser.BuildScriptContext = parseGradleScript(gradleScript)
    val visitor = GradleToBazelVisitor()
    /*
        // Lexer
        val lexer = GradleLexer(CharStreams.fromString(gradleScript))
        val tokens = CommonTokenStream(lexer)

        // Parser
        val parser = GradleParser(tokens)
    //    val tree = parser.buildScript()*/
    visitor.visit(tree)
    visitor.generateBazelBuildFiles("output")

}

fun testParsingPrint(tree: GradleParser.BuildScriptContext) {

    // Visitor to process the tree
    val visitor = object : GradleBaseVisitor<Unit>() {
        override fun visitDependencyEntry(ctx: GradleParser.DependencyEntryContext) {
            val stringNode = ctx.STRING()
            if (stringNode != null) {
                println("Dependency: ${stringNode.text}")
            }
        }

        override fun visitPluginEntry(ctx: GradleParser.PluginEntryContext) {
            val pluginId = ctx.STRING(0).text
            val version = ctx.STRING(1)?.text
            print("Plugin: $pluginId")
            val optionalVersion = if (version != null) {
                " Version: $version"
            }
            else {
                ""
            }
            println(optionalVersion)

        }


        override fun visitRepositoryEntry(ctx: GradleParser.RepositoryEntryContext) {
            println("Repository: ${ctx.text}")
        }

        override fun visitTaskRegistration(ctx: GradleParser.TaskRegistrationContext) {
            val identifier = ctx.IDENTIFIER()
            if (identifier != null) {
                println("Task: ${identifier.text}")
            }
            super.visitTaskRegistration(ctx)
        }
    }

    val dependencies = visitor.visit(tree)
    println("Dependencies: $dependencies")
}