# umbrellone-maven-plugin

Provides plenty of shading without almost any configurability at all.

More seriously, provides automatic shading + relocating for all your project runtime 
dependencies (direct and transitive), in a maven java 8 build.

## Maven goals

1.  `generate-conf`: inspects the dependency tree and prints to stdout
    a detailed xml fragment that can be used to configure maven-shade-plugin.
    
2.  `shade-all`: generate a configuration for the shade plugin and calls the `shade` goal
    of the shade plugin.

## How the shade+relocating configuration is generated

1.  All project dependency jar artifacts are considered (test and non-jar dependencies are skipped)
2.  All jars are inspected to look for actual java packages
3.  Within every jar's packages we reduce to the set of minimum packages to be relocated
    E.g. a jar providing (a.b.c, a.b.d, b.c.d) will yield only (a.b, b.c.d) to be relocated. 
4.  All packages to be relocated are then cross-checked to avoid relocating packages twice.

## Examples

1.  Auto-configure and run the shade plugin with relocations:

    ```
    <plugin>
      <groupId>dev.dimlight</groupId>
      <artifactId>umbrellone-maven-plugin</artifactId>
      <version>0.1.0-SNAPSHOT</version>
      <executions>
        <execution>
          <id>shade-all</id>
          <!-- similarly to the shade plugin the phase needs to 'package' -->
          <phase>package</phase>
          <goals>
            <goal>shade-all</goal>
          </goals>
          <configuration>
            <!-- prefix for the relocated packages (needs to yield a valid java package name) -->
            <relocationPrefix>SHADE.</relocationPrefix>
            <!-- version of the shade plugin to use, default is 3.2.1 -->
            <shadePluginVersion>3.2.1</shadePluginVersion>
          </configuration>
        </execution>
      </executions>
    </plugin>
    ```

2.  Print the generated shade plugin configuration: `mvn umbrellone:generate-conf`

    E.g. for a project that has only io.vertx:vertx-web:3.7.1 as a dependency
    the following configuration will be generated:
    
    ```
    <configuration>
      <artifactSet>
        <includes>
          <include>io.vertx:vertx-web</include>
          <include>io.vertx:vertx-web-common</include>
          <include>io.vertx:vertx-auth-common</include>
          <include>io.vertx:vertx-bridge-common</include>
          <include>io.vertx:vertx-core</include>
          <include>io.netty:netty-common</include>
          <include>io.netty:netty-buffer</include>
          <include>io.netty:netty-transport</include>
          <include>io.netty:netty-handler</include>
          <include>io.netty:netty-codec</include>
          <include>io.netty:netty-handler-proxy</include>
          <include>io.netty:netty-codec-socks</include>
          <include>io.netty:netty-codec-http</include>
          <include>io.netty:netty-codec-http2</include>
          <include>io.netty:netty-resolver</include>
          <include>io.netty:netty-resolver-dns</include>
          <include>io.netty:netty-codec-dns</include>
          <include>com.fasterxml.jackson.core:jackson-core</include>
          <include>com.fasterxml.jackson.core:jackson-databind</include>
          <include>com.fasterxml.jackson.core:jackson-annotations</include>
        </includes>
      </artifactSet>
      <relocations>
        <relocation>
          <pattern>com.fasterxml.jackson.annotation</pattern>
          <shadedPattern>SHADE.com.fasterxml.jackson.annotation</shadedPattern>
        </relocation>
        <relocation>
          <pattern>com.fasterxml.jackson.core</pattern>
          <shadedPattern>SHADE.com.fasterxml.jackson.core</shadedPattern>
        </relocation>
        <relocation>
          <pattern>com.fasterxml.jackson.databind</pattern>
          <shadedPattern>SHADE.com.fasterxml.jackson.databind</shadedPattern>
        </relocation>
        <relocation>
          <pattern>io.netty</pattern>
          <shadedPattern>SHADE.io.netty</shadedPattern>
        </relocation>
        <relocation>
          <pattern>io.vertx.core</pattern>
          <shadedPattern>SHADE.io.vertx.core</shadedPattern>
        </relocation>
        <relocation>
          <pattern>io.vertx.ext.auth.impl.hash</pattern>
          <shadedPattern>SHADE.io.vertx.ext.auth.impl.hash</shadedPattern>
        </relocation>
        <relocation>
          <pattern>io.vertx.ext.bridge</pattern>
          <shadedPattern>SHADE.io.vertx.ext.bridge</shadedPattern>
        </relocation>
        <relocation>
          <pattern>io.vertx.ext.web</pattern>
          <shadedPattern>SHADE.io.vertx.ext.web</shadedPattern>
        </relocation>
      </relocations>
    </configuration>
    ```

## Tips and advices

- Look at the generated configuration before shading away mindlessly: the plugin is young!
