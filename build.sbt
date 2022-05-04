scalaVersion := "2.13.8"

name := "property-manager"

val CatsVersion   = "2.6.1"
val FlywayVersion = "8.5.9"
val DoobieVersion = "1.0.0-RC1"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core"       % CatsVersion,
  "org.flywaydb"   % "flyway-core"     % FlywayVersion,
  "org.tpolecat"  %% "doobie-core"     % DoobieVersion,
  "org.tpolecat"  %% "doobie-hikari"   % DoobieVersion,
  "org.tpolecat"  %% "doobie-postgres" % DoobieVersion,
)
