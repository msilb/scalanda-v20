sonatypeProfileName := "msilb"

publishMavenStyle := true

licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT"))

import xerial.sbt.Sonatype._
sonatypeProjectHosting := Some(GitHubHosting("msilb", "scalanda-v20", "me@msilb.com"))