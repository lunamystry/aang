# aang

Getting avatar information from Google

[Design](DESIGN.md)

[TODO](TODO.md)

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

You then need to export the following variables: 

    export GOOGLE_CLIENT_ID=
    export GOOGLE_CLIENT_SECRET=
    export CLOUDINARY_URL=

You then type `~run` to run the application
This will run migrations to create the user table and place the sqlite database in `target/aang.db`


Despite what the google docs say, to test sign in you can't use localhost:8080, 
you need to have and `https` domain, a simple way to do this is to use something like:

    ssh -p 443 -R0:localhost:8080 a.pinggy.io  

and then adding the resulting https pinggy url to "Authorized JavaScript origins" on the google console

