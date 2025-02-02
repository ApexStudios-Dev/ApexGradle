import me.modmuss50.mpp.ReleaseType
import net.neoforged.moddevgradle.dsl.ModDevExtension

plugins {
    id("me.modmuss50.mod-publish-plugin")
}

val gameVersion = provider { extensions.getByType(ModDevExtension::class.java).minecraftVersion }

publishMods {
    file = tasks.named("jar", Jar::class.java).map { it.archiveFile }.get()
    type = ReleaseType.STABLE
    modLoaders.add("neoforge")
    changelog = ""

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        minecraftVersions.add(gameVersion)
        announcementTitle = "Download from Modrinth"
    }

    curseforge {
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        minecraftVersions.add(gameVersion)
        announcementTitle = "Download from CurseForge"
    }

    discord {
        webhookUrl = providers.environmentVariable("DISCORD_WEBHOOK_URL")
        username = "ApexStudios"
        avatarUrl = "https://raw.githubusercontent.com/ApexStudios-Dev/.github/refs/heads/master/assets/apexstudios/Logo.png"
        content = "# ${project.name} v${project.version} is out!"
    }
}
