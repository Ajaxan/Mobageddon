# Mobageddon
A Minecraft plugin to spawn more agile and capable mobs with abilities and powers. From breaking blocks, to building, to leaping towards players, there is no where to hide. 

**Table of Contents:**
- Plugin Info
  - Modifiers
  - Powers
  - Spawning
- Developer Info
    - Kotlin
    - Gradle
    - Setup
    - Commands
    - Location
    - Debugging
      - Common Issues
      - Hints

## Plugin Info
This section will provide a brief overview of how this plugin works and what you can expect it to do, along with it's limitations.

### Modifiers
TODO

### Powers
TODO

### Spawning
TODO

## Developer Info
This section will provide some insight and information about the parts of the plugin and how they work. This info won't be useful to you if you aren't interested in understanding how the code and build for this plugin works.

### Kotlin
This plugin is written in Kotlin. Why? Because I like Kotlin, it's better than Java, fight me. This plugin being written in Kotlin means the kotlin standard lib needs to be shaded into the plugin. This means the plugin may be bigger than normal if you're keeping track of plugin size. It also means that if you have multiple kotlin plugins this becomes redundant. This is why in the build file there is a non-shaded version as well. However that is not the version automatically grabbed. You'll need to modify the gradle task to use the other version.

### Gradle
This plugin uses gradle kotlin (kotlin has a special flavor of gradle). This means it differs
from building everything locally significantly but only slightly from maven.

You will need to find the maven repo location of the plugin you wish to use and then add it
in dependencies as compile only like the paper api. I've switched to generating the `plugin.yml` 
from the `gradle.build.kts` file using a plugin. You can read up on it here: https://github.com/Minecrell/plugin-yml

Final note about the gradle build. It automatically can export the built jar wherever you want. you'll need to create and set up your `gradle.properties` file

### Setup
Currently, to set up the project you must properly configure your java environment.
Next You need to copy/paste the `sample.gradle.properties` file to `gradle.properties` and configure
the jar copy location to be where your server is.

### Commands
`./gradlew build` - This builds the project and creates the jar file and copies it to the
location in the gradle file

### Debugging

#### Common Issues
None so far

#### Hints
None so far