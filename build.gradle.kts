import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

@CacheableTask
abstract class GitProducer : DefaultTask() {
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @OptIn(ExperimentalPathApi::class)
    @TaskAction
    fun run() {
        val gitDir = outputDir.get().asFile.resolve(".git")
        if (gitDir.exists()) {
            gitDir.toPath().deleteRecursively()
        }
        gitDir.mkdirs()

        // Initialize a new git repository
        ProcessBuilder("git", "init")
            .directory(gitDir)
            .start()
            .waitFor()

        // Create an empty commit
        ProcessBuilder("git", "commit", "--allow-empty", "-m", "Initial commit")
            .directory(gitDir)
            // Configure author
            .apply {
                environment()["GIT_AUTHOR_NAME"] = "tester"
                environment()["GIT_AUTHOR_EMAIL"] = "test@test.test"
            }
            .start()
            .waitFor()
    }
}

@UntrackedTask(because = "Makes the issue more clear")
abstract class GitConsumer : DefaultTask() {
    @get:InputDirectory
    abstract val inputDir: DirectoryProperty

    @OptIn(ExperimentalPathApi::class)
    @TaskAction
    fun run() {
        if (inputDir.get().asFile.resolve(".git").exists()) {
            println("Successfully found .git directory")
        } else {
            throw IllegalStateException("Did not find .git directory")
        }
    }
}

val producer = tasks.register<GitProducer>("gitProducer") {
    outputDir.set(layout.buildDirectory.dir("gitProducerOutput"))
    inputs.property("cacheBuster", providers.gradleProperty("testid").orElse("default"))
}

tasks.register<GitConsumer>("gitConsumer") {
    inputDir.set(producer.flatMap { it.outputDir })
}
