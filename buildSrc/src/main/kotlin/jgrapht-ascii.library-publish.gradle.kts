plugins {
    `java-library`
    `maven-publish`
    signing
    id("io.deepmedia.tools.deployer")
}

deployer {
    projectInfo {
        name.set("jgrapht-exporter-ascii")
        description.set("ASCII/Unicode graph exporter for JGraphT using the Sugiyama layered layout algorithm")
        url.set("https://github.com/driessamyn/jgrapht-exporter-ascii")
        scm.fromGithub("driessamyn", "jgrapht-exporter-ascii")
        license(apache2)
        developer("driessamyn", "dries@samyn.net")
    }

    content {
        component {
            fromJava()
        }
    }

    centralPortalSpec {
        auth.user.set(secret("MAVEN_USERNAME"))
        auth.password.set(secret("MAVEN_PASSWORD"))
        signing.key.set(secret("GPG_SIGNING_KEY"))
        signing.password.set(secret("GPG_SIGNING_PASSPHRASE"))
        allowMavenCentralSync = false
    }

    githubSpec {
        owner.set("driessamyn")
        repository.set("jgrapht-exporter-ascii")
        auth.user.set(secret("GH_USER"))
        auth.token.set(secret("GH_TOKEN"))
        signing.key.set(secret("GPG_SIGNING_KEY"))
        signing.password.set(secret("GPG_SIGNING_PASSPHRASE"))
    }

    localSpec {
        directory.set(file("build/inspect"))
    }
}
