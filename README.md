# aang

Getting avatar information from Google

[Design](DESIGN.md)

## Usage

This is an sbt project. You need to have that installed first,
[coursier](https://get-coursier.io/) is one way to get the scala toolchains

For linux you can use:

```bash
    curl -fL \"https://github.com/coursier/launchers/raw/master/cs-x86_64-pc-linux.gz\" | gzip -d > cs
    chmod +x cs
    ./cs  setup --jvm graalvm-java25 --apps ammonite,bloop,cs,giter8,sbt,scala,scala3-repl,scalafmt
```

You can compile code with `sbt compile`,
run it with `sbt run`,
and `sbt console` will start a Scala 3 REPL.
