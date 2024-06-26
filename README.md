# SBT Sonatype Central

A plugin for deploying open-source artifacts to Maven via the Sonatype Central portal

## Getting Started

*Note: the following will not work until this plugin is successfully deployed to Maven. This is currently pending, due to the Sonatype Central portal having problems correctly validating sbt plugins*

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

4. Start the sbt shell by running `sbt`. 
5. Run `publishSigned` from the sbt shell and input your gpg password when prompted.
6. Run either `sonatypeCentralUpload` or `sonatypeCentralRelease` from the sbt shell.