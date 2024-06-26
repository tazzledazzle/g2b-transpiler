import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File

class GradleToBazelVisitor : GradleBaseVisitor<Unit>() {

    val bazelModel = StringBuilder()
    val workspaceModel = StringBuilder()
    val repositories = mutableSetOf<String>()
    val plugins = mutableSetOf<String>()

    init {
        // todo: this may move into the visitBuildScript or visitPluginEntry functions
        workspaceModel.append(
            """
            load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

            rules_jvm_external_version = "4.0"
            http_archive(
                name = "rules_jvm_external",
                urls = ["https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % rules_jvm_external_version],
                strip_prefix = "rules_jvm_external-%s" % rules_jvm_external_version,
            )

            load("@rules_jvm_external//:defs.bzl", "maven_install")
            maven_install(
                artifacts = [
            """.trimIndent()
        )
    }

    override fun visitBuildScript(ctx: GradleParser.BuildScriptContext) {
        super.visitBuildScript(ctx)
        workspaceModel.append(
            """
                ],
                repositories = [
            """.trimIndent()
        )
        repositories.forEach { repo ->
            workspaceModel.append("                    \"$repo\",\n")
        }
        workspaceModel.append(
            """
                ],
            )
            """.trimIndent()
        )
    }

    override fun visitPluginsDeclaration(ctx: GradleParser.PluginsDeclarationContext?) {
        super.visitPluginsDeclaration(ctx)
        ctx?.pluginEntry()?.forEach { plugin -> visit(plugin) }
    }

    override fun visitPluginEntry(ctx: GradleParser.PluginEntryContext) {
        ctx.STRING().forEach { pluginId ->
            // Handle plugin conversion logic here
            println(pluginId.text)
//            when {
//                ctx.ID() != null -> identifyAndAddPlugins()
//            }
        }
    }

    override fun visitRepositoriesDeclaration(ctx: GradleParser.RepositoriesDeclarationContext) {
        super.visitRepositoriesDeclaration(ctx)
        ctx.repositoryEntry().forEach { visit(it) }
    }

    override fun visitRepositoryEntry(ctx: GradleParser.RepositoryEntryContext) {
        // Handle repository conversion logic here
        when {
            ctx.MAVENCENTRAL() != null -> repositories.add("https://repo1.maven.org/maven2")
            ctx.JCENTER() != null -> repositories.add("https://jcenter.bintray.com/")
            ctx.MAVEN() != null -> {
                val url = ctx.STRING().text.trim('\'')
                repositories.add(url)
            }
        }
    }

    override fun visitDependencyEntry(ctx: GradleParser.DependencyEntryContext) {
        val dependency = ctx.STRING().text.trim('\'')
        // Convert Gradle dependency to Bazel dependency
        workspaceModel.append("        \"$dependency\",\n")
        bazelModel.append("deps = [\"@maven//:${dependency.replace(':', '_')}\"]\n")

    }

    override fun visitTaskRegistration(ctx: GradleParser.TaskRegistrationContext) {
        // Handle task conversion logic here
        val taskName = ctx.STRING().text.trim('\'')
        val mainClass = ctx.taskStatement().find { it.IDENTIFIER().text == "mainClass" }?.STRING()?.text?.trim('\'')
        if (mainClass != null) {
            bazelModel.append(
                """
                java_binary(
                    name = "$taskName",
                    main_class = "$mainClass",
                    runtime_deps = [":my_library"],
                )
                """.trimIndent()
            )
        }
    }

    fun generateBazelBuildFiles(outputDirectory: String) {
        File("$outputDirectory/WORKSPACE").writeText(workspaceModel.toString())
        File("$outputDirectory/BUILD.bazel").writeText(bazelModel.toString())
    }
}

fun parseGradleScript(script: String): GradleParser.BuildScriptContext {
    val lexer = GradleLexer(CharStreams.fromString(script))
    val tokens = CommonTokenStream(lexer)
    val parser = GradleParser(tokens)
    return parser.buildScript()
}
