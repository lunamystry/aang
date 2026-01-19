import Dependencies._

ThisBuild / organization := "me.mandla"
ThisBuild / scalaVersion := "3.7.4"

lazy val `aang` =
  project
    .in(file("."))
    .settings(name := "aang")
    .settings(commonSettings)
    .settings(autoImportSettings)
    .settings(dependencies)

lazy val commonSettings = {
  lazy val commonScalacOptions =
    Seq(
      Compile / console / scalacOptions := {
        (Compile / console / scalacOptions)
          .value
          .filterNot(_.contains("wartremover"))
          .filterNot(Scalac.Lint.toSet)
          .filterNot(Scalac.FatalWarnings.toSet) :+ "-Wconf:any:silent"
      },
      Test / console / scalacOptions :=
        (Compile / console / scalacOptions).value,
    )

  lazy val otherCommonSettings =
    Seq(
      update / evictionWarningOptions := EvictionWarningOptions.empty
      // cs launch scalac:3.3.1 -- -Wconf:help
      // src is not yet available for Scala3
      // scalacOptions += s"-Wconf:src=${target.value}/.*:s",
    )

  Seq(
    commonScalacOptions,
    otherCommonSettings,
  ).reduceLeft(_ ++ _)
}

lazy val autoImportSettings =
  Seq(
    scalacOptions +=
      Seq(
        "java.lang",
        "scala",
        "scala.Predef",
        "scala.annotation",
        "scala.util.chaining",
      ).mkString(start = "-Yimports:", sep = ",", end = ""),
    Test / scalacOptions +=
      Seq(
        "org.scalacheck",
        "org.scalacheck.Prop",
      ).mkString(start = "-Yimports:", sep = ",", end = ""),
  )

val app =
  Seq(
    "dev.zio" %% "zio" % "2.1.24",
    "dev.zio" %% "zio-http" % "3.7.4",
    "dev.zio" %% "zio-json" % "0.7.44",
    "dev.zio" %% "zio-logging" % "2.5.3",
    "dev.zio" %% "zio-logging-slf4j2" % "2.5.3",
    "dev.zio" %% "zio-streams" % "2.1.24",
    "ch.qos.logback" % "logback-classic" % "1.5.24",
    "org.slf4j" % "slf4j-api" % "2.0.17",
    "com.cloudinary" % "cloudinary-http5" % "2.3.2",
    "com.cloudinary" % "cloudinary-taglib" % "2.3.2",
  )

val config =
  Seq(
    "dev.zio" %% "zio-config-magnolia" % "4.0.6",
    "dev.zio" %% "zio-config-typesafe" % "4.0.6",
  )

val db =
  Seq(
    "com.zaxxer" % "HikariCP" % "5.1.0",
    "io.getquill" %% "quill-jdbc-zio" % "4.8.6",
    "org.flywaydb" % "flyway-core" % "10.1.0",
    "org.xerial" % "sqlite-jdbc" % "3.49.0.0",
  )

val web =
  Seq(
    "com.lihaoyi" %% "scalatags" % "0.13.1",
    "dev.zio" %% "zio-http" % "3.7.4",
  )

val tests =
  Seq(
    "dev.zio" %% "zio-test" % "2.1.24" % Test,
    "dev.zio" %% "zio-test-sbt" % "2.1.24" % Test,
    "dev.zio" %% "zio-http-testkit" % "3.7.4" % Test,
    "org.slf4j" % "slf4j-simple" % "2.0.17" % Test,
  )

lazy val dependencies =
  Seq(
    libraryDependencies ++= app ++ config ++ db ++ web,
    libraryDependencies ++= Seq(
      com.eed3si9n.expecty.expecty,
      org.scalacheck.scalacheck,
      org.scalameta.`munit-scalacheck`,
      org.scalameta.munit,
      org.typelevel.`discipline-munit`,
    ).map(_ % Test) ++ tests,
  )
