# SBT Sonatype Central

A plugin for deploying open-source artifacts to Maven via the Sonatype Central portal

## Getting Started

**Prerequisites: Make sure that you are running sbt 1.9.0 or higher.**

1. Add the following to your `project/plugins.sbt` file (or equivalent):

```sbt
addSbtPlugin("com.lumidion"   % "sbt-sonatype-central"  % "0.1.0")
addSbtPlugin("com.github.sbt" % "sbt-pgp"               % "2.2.1")
```

2. Make sure that your version is not a snapshot (Sonatype Central does not support publishing snapshot versions).
Add the following to your `build.sbt` file (or equivalent):

```sbt
publishTo := sonatypeCentralPublishToBundle.value
```

3. Follow the guide to setting up your gpg credentials [here](https://github.com/sbt/sbt-ci-release?tab=readme-ov-file#gpg) if you have not already done so.

4. Generate a portal token in Sonatype Central, as illustrated in the [following guide](https://central.sonatype.org/publish/generate-portal-token/). Set the resultant username and password in your environment via the `SONATYPE_USERNAME` and `SONATYPE_PASSWORD` environment variables.

5. Start the sbt shell by running `sbt`. 
6. Run `publishSigned` from the sbt shell and input your gpg password when prompted.
7. Run either `sonatypeCentralUpload` or `sonatypeCentralRelease` from the sbt shell.